package es.urjc.ecomostoles.backend.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler logic.
 * 
 * Centralizes error catch blocks, specifically targeted at translating low-level
 * Spring framework exceptions (e.g., Multipart payload size limits) into user-friendly
 * UI feedback without crashing the application context.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepts payload size violations thrown by Tomcats embedded container.
     * 
     * @param exc the underlying {@link MaxUploadSizeExceededException}.
     * @param redirectAttributes flash attributes to carry the error message across the redirect.
     * @param request the current HTTP request, used to determine the referer for a graceful fallback.
     * @return the dynamic redirect path back to the origin view.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("errorMessage", "El archivo es demasiado grande. El límite obligatorio es de 5MB.");

        // Extract the referer to guarantee the user returns contextually to their interrupted workflow.
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/dashboard";
        }

        return "redirect:" + referer;
    }
}
