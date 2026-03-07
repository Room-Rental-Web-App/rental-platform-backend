package com.web.room.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder (); // Password ko encode karne ke liye
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfiguration()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/sitemap.xml","/robots.txt").permitAll()
                        // 1. Auth Endpoints (Sabke liye open)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/").permitAll()

                        // 2. Room Endpoints - Public Access (Search, Featured, etc.)
                        .requestMatchers(HttpMethod.GET, "/api/rooms/search", "/api/rooms/featured", "/api/rooms/cities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/{id}", "/api/rooms/roomDetails/{id}", "/api/reviews/top","/api/reviews/room/{roomId}").permitAll()
                        .requestMatchers (HttpMethod.POST, "/api/reviews/add").hasRole ("USER")


                        // 3. Room Endpoints - Owner Only (apiConfig ke ADD_ROOM, UPDATE_ROOM, MY_LISTINGS etc.)
                        .requestMatchers("/api/rooms/add", "/api/rooms/my-listings").hasRole("OWNER")
                        .requestMatchers("/api/rooms/update/**", "/api/rooms/update-status/**", "/api/rooms/delete/**").hasAnyRole("OWNER" ,"ADMIN")

                        // 4. Admin Endpoints (apiConfig ke ADMIN_ALL_USERS, APPROVE_OWNER, etc.)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 5. Wishlist Endpoints (Authenticated Users)
                        .requestMatchers("/api/wishlist/**").authenticated()

                        // Pre-flight requests (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Baki sab kuch lock
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfiguration() {
        UrlBasedCorsConfigurationSource url = new UrlBasedCorsConfigurationSource ();
        CorsConfiguration cors = new CorsConfiguration ();
        cors.setAllowCredentials (true);
        cors.addAllowedOriginPattern("*");
        cors.setAllowedHeaders (List.of ("*"));
        cors.setAllowedMethods (List.of ("*"));
        url.registerCorsConfiguration ("/**", cors);
        return url;
    }
}