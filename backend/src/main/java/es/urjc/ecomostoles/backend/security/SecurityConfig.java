package es.urjc.ecomostoles.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

/**
 * Spring Security configuration with RBAC (Role-Based Access Control).
 *
 * System roles:
 *  - ROLE_ADMIN   → platform administrator → redirects to /admin/panel
 *  - ROLE_EMPRESA → registered company (normal user) → redirects to /dashboard
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Login success handler that redirects according to role:
     *   ROLE_ADMIN   → /admin/panel
     *   ROLE_EMPRESA → /dashboard
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String targetUrl = "/dashboard";            // default destination (company)
            for (GrantedAuthority authority : authorities) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    targetUrl = "/admin/panel";         // admin → admin control panel
                    break;
                }
            }
            response.sendRedirect(request.getContextPath() + targetUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            .authorizeHttpRequests(auth -> auth

                // ── Public resources and pages ──────────────────────────────
                .requestMatchers(
                    "/", "/index.html",
                    "/login", "/registro", "/recuperar_password",
                    "/privacidad", "/terminos",
                    "/error",                               // custom error page
                    "/mercado", "/mercado/**",              // public offer listing
                    "/oferta/{id:\\d+}",                    // public offer detail
                    "/solicitudes", "/solicitudes/**",
                    "/css/**", "/js/**", "/img/**", "/images/**"
                ).permitAll()

                // ── Administration panel: ADMIN only ──────────────────────
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/admin/configuracion").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                // ── Creation and edition: any authenticated user ────────
                .requestMatchers(
                    "/oferta/nueva", "/ofertas/*/editar",
                    "/demanda/nueva", "/demandas/*/editar",
                    "/dashboard/**", "/perfil/**",
                    "/mensajes/**", "/acuerdos/**", "/acuerdo/**"
                ).authenticated()

                // ── The rest requires authentication ─────────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())  // ← redirect by role
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
