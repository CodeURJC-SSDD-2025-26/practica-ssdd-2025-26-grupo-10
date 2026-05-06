package es.urjc.ecomostoles.backend.controller.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API.
 * Handles both DTO validation (MethodArgumentNotValidException) and 
 * Entity validation (ConstraintViolationException) to ensure 400 Bad Request
 * is returned instead of 500 Internal Server Error.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalRestControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestControllerAdvice.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("[REST API] 404 NOT FOUND — path: '{}', reason: '{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorDTO.of(request.getRequestURI(), ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Handles @Valid failure on @RequestBody (DTO level)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[REST API] 400 BAD REQUEST (DTO) — path: '{}', violations: '{}'", request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.of(request.getRequestURI(), "Validation failed — " + details, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Handles ConstraintViolationException (Entity/JPA level)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String details = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("[REST API] 400 BAD REQUEST (Constraint) — path: '{}', violations: '{}'", request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.of(request.getRequestURI(), "Constraint violation — " + details, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Handles registration conflicts (Duplicate Email/CIF)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDTO> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("[REST API] 400 BAD REQUEST (Conflict) — path: '{}', reason: '{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.of(request.getRequestURI(), ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Handles low-level database integrity conflicts (Unique constraints)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("[REST API] 400 BAD REQUEST (Integrity) — path: '{}', reason: '{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.of(request.getRequestURI(), "The email or CIF is already registered in the system", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiErrorDTO> handleAuthentication(org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        log.warn("[REST API] 401 UNAUTHORIZED — path: '{}', reason: '{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorDTO.of(request.getRequestURI(), "Invalid credentials", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        log.warn("[REST API] 403 FORBIDDEN — path: '{}', reason: '{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorDTO.of(request.getRequestURI(), "Access denied: You do not have the required permissions for this operation.", HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> handleResponseStatus(org.springframework.web.server.ResponseStatusException ex, HttpServletRequest request) {
        log.warn("[REST API] {} — path: '{}', reason: '{}'", ex.getStatusCode(), request.getRequestURI(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorDTO.of(request.getRequestURI(), ex.getReason(), ex.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("[REST API] 500 INTERNAL SERVER ERROR — path: '{}', exception: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorDTO.of(request.getRequestURI(), "Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
