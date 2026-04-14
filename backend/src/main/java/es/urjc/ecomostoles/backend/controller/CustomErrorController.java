package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

/**
 * Controlador personalizado de errores.
 *
 * Sustituye al DefaultErrorController de Spring Boot para garantizar que
 * error.html se renderice con todas las variables que el layout (header/footer)
 * necesita. Sin él, Mustache lanza una excepción al intentar resolver
 * {{empresa.nombreComercial}} → página en blanco.
 *
 * Proporciona mensajes amigables diferenciados por código HTTP:
 *   403 → "No tienes permiso para realizar esta acción."
 *   404 → "La página que buscas no existe."
 *   500 → "Error interno del servidor."
 */
@Controller
public class CustomErrorController implements ErrorController {

    private final EmpresaRepository empresaRepository;

    public CustomErrorController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {

        // ── 1. Código de estado HTTP ───────────────────────────────────────────
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = 500;
        if (statusObj != null) {
            status = Integer.parseInt(statusObj.toString());
        }
        model.addAttribute("status", status);

        // ── 2. Título y mensaje según el código ────────────────────────────────
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

        // ── 3. Empresa para el header (navbar) ─────────────────────────────────
        // El partial {{> header}} requiere {{empresa.nombreComercial}} para el
        // avatar y el dropdown. Si el usuario está autenticado lo cargamos;
        // si no, creamos una empresa vacía para evitar NullPointerException en Mustache.
        if (principal != null) {
            Optional<Empresa> opt = empresaRepository.findByEmailContacto(principal.getName());
            model.addAttribute("empresa", opt.orElseGet(Empresa::new));
        } else {
            // Usuario no autenticado: empresa vacía → Mustache renderiza cadenas vacías
            model.addAttribute("empresa", new Empresa());
        }

        return "error";
    }
}
