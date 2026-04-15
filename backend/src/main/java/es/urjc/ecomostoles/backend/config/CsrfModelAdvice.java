package es.urjc.ecomostoles.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global advice controller for injecting Cross-Site Request Forgery (CSRF) tokens.
 * 
 * Ensures the Spring Security CSRF token is pushed into the model globally so that
 * Mustache templates can dynamically render secure hidden inputs across all forms.
 */
@ControllerAdvice
public class CsrfModelAdvice {

    /**
     * Binds the CSRF token to the model attribute "_csrf" lazily.
     * 
     * @param request the current HTTP servlet request containing the security context.
     * @return the {@link CsrfToken} instance, or null if CSRF protection is disabled.
     */
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
