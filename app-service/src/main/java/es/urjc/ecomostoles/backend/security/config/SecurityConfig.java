package es.urjc.ecomostoles.backend.security.config;

import es.urjc.ecomostoles.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Modern Security Posture enforcing stateless JWT-based interactions.
 * 
 * Replaces the legacy Session-based SecurityConfig. Maps route authorizations, 
 * disables CSRF for API requests, and injects the custom JWT filter before standard filters.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Since we use JWT (Stateless), CSRF protection is not needed
            .csrf(AbstractHttpConfigurer::disable)
            
            // Route-level Authorization Matrix
            .authorizeHttpRequests(auth -> auth
                // 1. Whitelist authentication & API documentation
                .requestMatchers("/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // 2. Whitelist static assets and traditional web endpoints (transition phase)
                .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/images/**").permitAll()
                
                // 3. Strictly require valid JWT for any business API endpoints
                .requestMatchers("/api/v1/**").authenticated()
                
                // 4. Default policy for other routes (permitting for MVC compatibility during migration)
                .anyRequest().permitAll()
            )
            
            // Enforce Stateless Architecture (No HttpSession)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Register Data Access Provider
            .authenticationProvider(authenticationProvider)
            
            // Insert custom JWT Interceptor
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
