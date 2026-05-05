package es.urjc.ecomostoles.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security interception layer for validating REST API JWTs.
 * 
 * Extracts Bearer tokens from incoming HTTP requests, validates cryptographic integrity,
 * and hydrates the Spring SecurityContext if the token represents a valid, active Principal.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Fast fail if token is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract token payload
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            // 4. If identity is resolved and context is empty (not yet authenticated this request)
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Fetch the principal details from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 5. Enforce cryptographic validation
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    log.debug("[Security] JWT authenticated successfully for user: {}", userEmail);

                    // Hydrate Spring Security Auth Token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed since token is already validated
                            userDetails.getAuthorities()
                    );
                    
                    // Attach original request metadata (IP, Session ID, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establish the authenticated context for downstream controllers
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If the token is expired, malformed, or tampered with, log it securely without breaking the chain
            log.warn("[Security] JWT Validation failed: {}", e.getMessage());
            // SecurityContext remains null; Spring Security will block access if endpoint requires it
        }

        // 6. Guarantee execution flow continues towards the endpoint or subsequent filters
        filterChain.doFilter(request, response);
    }
}
