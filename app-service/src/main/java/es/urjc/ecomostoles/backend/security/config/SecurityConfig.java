package es.urjc.ecomostoles.backend.security.config;
import es.urjc.ecomostoles.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    // --- 1. REST API CONFIGURATION (STATELESS + JWT) ---
    @Bean
    @Order(1) // Highest priority: Catches REST requests first
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // This chain ONLY affects the API
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- 2. TRADITIONAL WEB CONFIGURATION (STATEFUL + FORM LOGIN) ---
    @Bean
    @Order(2) // Secondary priority: Catches normal web traffic
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow public access to static resources, login, registration, and Swagger
                .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/img/**", "/images/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // All other web pages require being logged in
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")               // Our custom HTML template
                .loginProcessingUrl("/login")      // The URL where the HTML performs the POST
                .defaultSuccessUrl("/", true)      // Where to go on success
                .failureUrl("/login?error=true")   // Where to go on failure
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    // --- CORS CONFIGURATION FOR THE API FRONTEND ---
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:4200", "http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}