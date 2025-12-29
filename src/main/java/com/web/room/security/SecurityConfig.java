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
                .csrf (AbstractHttpConfigurer::disable)
                .cors (cors -> cors.configurationSource (corsConfiguration ()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/findRoom").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Pre-flight allow karein
                        .requestMatchers("/api/rooms/add").permitAll() // Temporary test ke liye
//                        .requestMatchers("/api/rooms/add").hasRole("OWNER") // Ensure DB has ROLE_OWNER
                        .requestMatchers("/api/admin/**").permitAll()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration cors = new CorsConfiguration ();
        cors.setAllowedOrigins (List.of ("http://localhost:5173"));
        cors.setAllowCredentials (true);
        cors.setAllowedHeaders (List.of ("*"));
        cors.setAllowedMethods (List.of ("POST", "PUT", "DELETE", "GET", "OPTIONS"));
        UrlBasedCorsConfigurationSource url = new UrlBasedCorsConfigurationSource ();
        url.registerCorsConfiguration ("/**", cors);
        return url;
    }
}