package es.urjc.ecomostoles.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Inyecta automáticamente el token CSRF de Spring Security en el modelo
 * de TODAS las vistas Mustache bajo el nombre "_csrf".
 *
 * Sin este advice, {{#_csrf}}...{{/_csrf}} nunca se renderiza porque
 * Mustache no accede directamente a los atributos del HttpServletRequest.
 *
 * Con él, el bloque {{#_csrf}}<input ... value="{{token}}">{{/_csrf}}
 * funciona en todas las plantillas y es null-safe: si por cualquier razón
 * el token fuera null, el bloque simplemente no se renderiza.
 */
@ControllerAdvice
public class CsrfModelAdvice {

    /**
     * Añade el objeto CsrfToken al modelo con la clave "_csrf".
     * Spring Security lo resuelve de forma lazy desde el HttpServletRequest,
     * por lo que nunca bloquea el hilo si CSRF no está activo.
     *
     * @param request la petición HTTP actual
     * @return el CsrfToken, o null si CSRF está desactivado (bloque Mustache lo omite)
     */
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
