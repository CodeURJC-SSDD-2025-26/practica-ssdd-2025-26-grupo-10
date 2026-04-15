package es.urjc.ecomostoles.backend.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for the EcoMóstoles platform.
 * Captures specific exceptions and provides user-friendly feedback.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepts when a user uploads a file that exceeds the size limit.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, 
                                         RedirectAttributes redirectAttributes, 
                                         HttpServletRequest request) {
        
        redirectAttributes.addFlashAttribute("error", "El archivo es demasiado grande. El límite máximo es 2MB.");
        
        // Attempt to redirect back to the form
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/dashboard"; // Safe fallback
        }
        
        return "redirect:" + referer;
    }

    /**
     * Catches any other exception and redirects to a friendly error page.
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception exc, Model model) {
        model.addAttribute("error", "Ha ocurrido un error inesperado. Por favor, contacte con el administrador.");
        return "error";
    }
}
