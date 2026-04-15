package es.urjc.ecomostoles.backend.controller;

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
     * This is kept separate as it requires a specific redirect-back-to-form logic.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("errorMessage", "El archivo es demasiado grande. El límite obligatorio es de 5MB.");

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/dashboard";
        }

        return "redirect:" + referer;
    }
}
