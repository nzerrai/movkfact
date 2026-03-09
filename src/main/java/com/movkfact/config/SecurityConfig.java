package com.movkfact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration for authentication and authorization.
 * 
 * Configures the security filter chain to:
 * - Allow public access to health check endpoint
 * - Allow public access to H2 console (dev only)
 * - Require authentication for all other endpoints
 * - Use stateless session management (JWT-based)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     * 
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS configuration for frontend access
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // CSRF protection disabled for stateless API
                .csrf(csrf -> csrf.disable())
                
                // Use stateless session management (no session cookies)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Domain API endpoints - public for MVP (will add auth in S1.5+)
                        .requestMatchers("/api/domains/**").permitAll()
                        
                        // Data Sets API endpoints - public for MVP
                        .requestMatchers("/api/data-sets/**").permitAll()

                        // Preview endpoint - public for MVP (S7.1) — path exact (CORS OPTIONS + POST)
                        .requestMatchers("/api/datasets/preview").permitAll()

                        // Lexicon API endpoints - public for MVP
                        .requestMatchers("/api/lexicon/**").permitAll()

                        // Batch Generation API endpoints - public for MVP
                        .requestMatchers("/api/batch/**").permitAll()

                        // Anonymization API endpoints - public for MVP
                        .requestMatchers("/api/anonymize/**").permitAll()

                        // Settings API endpoints - public for MVP (auth admin in JWT phase 2)
                        .requestMatchers("/api/settings/**").permitAll()

                        // WebSocket endpoint - public for MVP (S3.3)
                        .requestMatchers("/ws/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        
                        // All other requests require authentication (future endpoints)
                        .anyRequest().authenticated()
                )
                
                // Allow H2 console iframe (development only)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        return http.build();
    }

    /**
     * Provides a password encoder bean for Spring Security.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for frontend access.
     * 
     * Allows React frontend (running on localhost:3000) to make requests to the backend.
     * 
     * @return CorsConfigurationSource with CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow frontend origins
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",        // React development server
            "http://localhost:3001",        // Alternative port
            "http://localhost:8080",        // Same-origin
            "http://127.0.0.1:3000",        // Alternative localhost
            "http://127.0.0.1:3001"         // Alternative localhost alt port
        ));
        
        // Allow HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow request headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        // Allow specific response headers to be accessed by frontend
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Cache CORS preflight responses
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
