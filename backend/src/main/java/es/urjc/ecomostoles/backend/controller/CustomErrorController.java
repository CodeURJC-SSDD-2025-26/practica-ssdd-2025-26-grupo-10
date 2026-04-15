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

/**
 * Custom Error Controller to handle all platform errors (404, 403, 500, etc.).
 * Guarantees that global layout attributes are present to prevent Mustache
 * rendering crashes.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private final ConfigurationService configurationService;
    private final CompanyService companyService;

    public CustomErrorController(ConfigurationService configurationService, CompanyService companyService) {
        this.configurationService = configurationService;
        this.companyService = companyService;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {
        System.out.println("DEBUG: CustomErrorController interceptando petición a /error");

        // 1. Recover status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String statusCode = status != null ? status.toString() : "Unknown";
        System.out.println("DEBUG: Código de estado detectado: " + statusCode);

        // 2. Inject Mandatory Global Attributes for Navbar/Footer
        model.addAttribute("platformName", configurationService.getAutoValue("platformName"));
        model.addAttribute("supportEmail", configurationService.getAutoValue("contactEmail"));
        model.addAttribute("currentYear", java.time.LocalDate.now().getYear());
        model.addAttribute("cacheBuster", System.currentTimeMillis());

        // 3. Inject Session Info (for Navbar state)
        if (principal != null) {
            companyService.findByEmail(principal.getName()).ifPresent(c -> {
                model.addAttribute("company", new es.urjc.ecomostoles.backend.dto.CompanyDTO(c));
                model.addAttribute("isAdmin", c.getRoles().contains("ADMIN"));
            });
        }

        // 4. Define dynamic error message and title
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

        System.out.println("DEBUG: Renderizando custom_error con Title: " + title);

        // 5. Render the renamed template
        return "custom_error";
    }
}
