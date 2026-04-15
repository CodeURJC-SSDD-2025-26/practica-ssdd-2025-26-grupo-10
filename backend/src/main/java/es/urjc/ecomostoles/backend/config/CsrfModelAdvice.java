package es.urjc.ecomostoles.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Automatically injects the Spring Security CSRF token into the model
 * of ALL Mustache views under the name "_csrf".
 *
 * Without this advice, {{#_csrf}}...{{/_csrf}} never renders because
 * Mustache does not directly access HttpServletRequest attributes.
 *
 * With it, the block {{#_csrf}}<input ... value="{{token}}">{{/_csrf}}
 * works in all templates and is null-safe: if for any reason
 * the token is null, the block simply does not render.
 */
@ControllerAdvice
public class CsrfModelAdvice {

    /**
     * Adds the CsrfToken object to the model with the "_csrf" key.
     * Spring Security resolves it lazily from the HttpServletRequest,
     * so it never blocks the thread if CSRF is not active.
     *
     * @param request the current HTTP request
     * @return the CsrfToken, or null if CSRF is disabled (Mustache block omits it)
     */
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
