package com.web.room.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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

    // 🔐 Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔒 Security Config
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ Correct CORS config (for new Spring Security)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public files
                        .requestMatchers("/sitemap.xml", "/robots.txt").permitAll()

                        // Auth APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        // Payment APIs
                        .requestMatchers("/payment/**").permitAll()

                        // Home
                        .requestMatchers("/").permitAll()

                        // Public Room APIs
                        .requestMatchers(HttpMethod.GET,
                                "/api/rooms/search",
                                "/api/rooms/featured",
                                "/api/rooms/cities"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/rooms/{id}",
                                "/api/rooms/roomDetails/{id}",
                                "/api/reviews/top",
                                "/api/reviews/room/{roomId}"
                        ).permitAll()

                        // Review
                        .requestMatchers(HttpMethod.POST, "/api/reviews/add").hasRole("USER")

                        // Owner APIs
                        .requestMatchers("/api/rooms/add", "/api/rooms/my-listings").hasRole("OWNER")
                        .requestMatchers("/api/rooms/update/**",
                                "/api/rooms/update-status/**",
                                "/api/rooms/delete/**")
                        .hasAnyRole("OWNER", "ADMIN")

                        // Admin APIs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Wishlist
                        .requestMatchers("/api/wishlist/**").authenticated()

                        // ✅ IMPORTANT (CORS preflight allow)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // बाकी secure
                        .anyRequest().authenticated()
                );

        // 🔥 JWT Filter
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🌍 GLOBAL CORS CONFIG
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(List.of(
                "https://www.roomsdekho.in",
                "https://roomsdekho.in",
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"
        ));

        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}