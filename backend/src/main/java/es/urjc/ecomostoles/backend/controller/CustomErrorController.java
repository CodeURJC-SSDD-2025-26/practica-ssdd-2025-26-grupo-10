package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Custom error controller.
 *
 * Replaces Spring Boot's DefaultErrorController to ensure that
 * error.html is rendered with all variables required by the layout (header/footer).
 * Without it, Mustache throws an exception when attempting to resolve
 * {{empresa.nombreComercial}} → blank page.
 *
 * Provides user-friendly messages distinguished by HTTP code:
 *   403 → "No tienes permiso para realizar esta acción."
 *   404 → "La página que buscas no existe."
 *   500 → "Error interno del servidor."
 */
@Controller
public class CustomErrorController implements ErrorController {

    private final EmpresaService empresaService;

    public CustomErrorController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {

        // ── 1. HTTP Status Code ──────────────────────────────────────────────
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = 500;
        if (statusObj != null) {
            status = Integer.parseInt(statusObj.toString());
        }
        model.addAttribute("status", status);

        // ── 2. Title and message according to status code ────────────────────
        String errorTitle;
        String errorMessage;

        if (status == HttpStatus.FORBIDDEN.value()) {              // 403
            errorTitle   = "Acceso Denegado";
            errorMessage = "No tienes permiso para realizar esta acción. "
                         + "Si crees que es un error, contacta con el administrador.";
        } else if (status == HttpStatus.NOT_FOUND.value()) {       // 404
            errorTitle   = "Página No Encontrada";
            errorMessage = "La página que buscas no existe, ha sido movida "
                         + "o el recurso ya fue intercambiado con éxito.";
        } else if (status == HttpStatus.UNAUTHORIZED.value()) {    // 401
            errorTitle   = "No Autenticado";
            errorMessage = "Debes iniciar sesión para acceder a esta sección.";
        } else {                                                   // 500 y otros
            errorTitle   = "Error Interno";
            errorMessage = "Ha ocurrido un error inesperado en el servidor. "
                         + "Por favor, inténtalo de nuevo más tarde.";
        }

        model.addAttribute("error", errorTitle);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("is403", status == 403);

        // ── 3. Company for the header (navbar) ───────────────────────────────
        // The partial {{> header}} requires {{empresa.nombreComercial}} for the
        // avatar and dropdown. It should only be added if the user is authenticated.
        if (principal != null) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(e -> model.addAttribute("empresa", new EmpresaDTO(e)));
        }

        return "error";
    }
}
