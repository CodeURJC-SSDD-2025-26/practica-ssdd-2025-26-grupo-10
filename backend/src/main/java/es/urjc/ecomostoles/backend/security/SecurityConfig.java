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

import java.io.IOException;
import java.util.Collection;

/**
 * Configuración de Spring Security con RBAC (Role-Based Access Control).
 *
 *  Roles del sistema:
 *   - ROLE_ADMIN   → administrador de la plataforma → redirige a /admin/panel
 *   - ROLE_EMPRESA → empresa registrada (usuario normal) → redirige a /dashboard
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // habilita @PreAuthorize en métodos de controlador
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Handler de éxito de login que redirige según el rol:
     *   ROLE_ADMIN   → /admin/panel
     *   ROLE_EMPRESA → /dashboard
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String targetUrl = "/dashboard";            // destino por defecto (empresa)
            for (GrantedAuthority authority : authorities) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    targetUrl = "/admin/panel";         // admin → panel de control admin
                    break;
                }
            }
            response.sendRedirect(request.getContextPath() + targetUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Necesario para que la consola H2 no quede en blanco (iframe)
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )
            .authorizeHttpRequests(auth -> auth

                // ── Recursos y páginas públicas ──────────────────────────────
                .requestMatchers(
                    "/", "/index.html",
                    "/login", "/registro",
                    "/error",                               // página de error personalizada
                    "/mercado", "/mercado/**",              // listado de ofertas, público
                    "/oferta/{id:\\d+}",                    // detalle de oferta, público
                    "/solicitudes", "/solicitudes/**",
                    "/css/**", "/js/**", "/img/**", "/images/**",
                    "/h2-console/**"
                ).permitAll()

                // ── Panel de administración: solo ADMIN ──────────────────────
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                // ── Creación y edición: cualquier usuario autenticado ────────
                .requestMatchers(
                    "/oferta/nueva", "/oferta/editar/**",
                    "/demanda/nueva", "/demanda/editar/**",
                    "/dashboard/**", "/perfil/**",
                    "/mensajes/**", "/acuerdos/**", "/acuerdo/**"
                ).authenticated()

                // ── El resto requiere autenticación ─────────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())  // ← redirige por rol
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
