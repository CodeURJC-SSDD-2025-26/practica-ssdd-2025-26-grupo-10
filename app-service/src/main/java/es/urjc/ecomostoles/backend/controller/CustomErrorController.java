package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global error routing interceptor.
 * 
 * Safely wraps HTTP error codes (404, 500, 403) and maps them to a uniform templated view. 
 * Guarantees that essential layout variables (navbar globals, auth states) remain present, 
 * shielding end-users from the raw Spring Whitelabel Error Page.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);

    private final ConfigurationService configurationService;
    private final CompanyService companyService;

    public CustomErrorController(ConfigurationService configurationService, CompanyService companyService) {
        this.configurationService = configurationService;
        this.companyService = companyService;
    }

    /**
     * Intercepts container-level routing exceptions and populates the model with safe error contexts.
     * 
     * @param request the inbound HTTP request containing the original error attributes.
     * @param model the current Spring MVC view model context.
     * @param principal the authenticated user context, crucial for rendering logged-in navigation.
     * @return the string reference to the custom mapped template.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {
        log.debug("CustomErrorController intercepting request to /error");

        // 1. Recover status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String statusCode = status != null ? status.toString() : "Unknown";
        log.debug("HTTP status code detected: {}", statusCode);

        // Inject mandatory global attributes for Navbar/Footer
        model.addAttribute("platformName", configurationService.getAutoValue("platformName"));
        model.addAttribute("supportEmail", configurationService.getAutoValue("contactEmail"));
        model.addAttribute("currentYear", java.time.LocalDate.now().getYear());
        model.addAttribute("cacheBuster", System.currentTimeMillis());

        // Inject session info (for Navbar state)
        if (principal != null) {
            companyService.findByEmail(principal.getName()).ifPresent(c -> {
                model.addAttribute("company", new es.urjc.ecomostoles.backend.dto.CompanyDTO(c));
                model.addAttribute("isAdmin", c.getRoles().contains("ADMIN"));
            });
        }

        // Define dynamic error message based on status code
        String message = "Ha ocurrido un error inesperado.";
        String title = "¡Vaya! Algo ha fallado";

        if (status != null) {
            int code = Integer.parseInt(statusCode);
            title = "Error " + statusCode;
            message = switch (code) {
                case 403 -> {
                    title = "Acceso Denegado";
                    yield "No tienes permiso para ver o modificar este recurso.";
                }
                case 404 -> {
                    title = "Página no encontrada";
                    yield "Lo sentimos, la página que buscas no existe o ha sido movida.";
                }
                case 500 -> {
                    title = "Error del Sistema";
                    yield "Error interno del servidor. Nuestro equipo técnico ha sido notificado.";
                }
                default -> "Se ha producido un error (" + statusCode + ") al procesar tu solicitud.";
            };
        }

        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        model.addAttribute("statusCode", statusCode);

        log.debug("Rendering custom_error with title: {}", title);

        // Return the custom error template view
        return "custom_error";
    }
}
