package es.urjc.ecomostoles.backend.controller.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Centralised exception handler scoped EXCLUSIVELY to {@code @RestController} beans.
 *
 * <h3>Isolation mechanism</h3>
 * The {@code annotations = RestController.class} filter on {@code @RestControllerAdvice}
 * ensures this advice is NEVER invoked for traditional Spring MVC controllers
 * (annotated with {@code @Controller}) that render Mustache HTML views.
 * Without this constraint a caught exception would return JSON to a browser page
 * expecting an HTML redirect — breaking the classic web flow entirely.
 *
 * <h3>Priority</h3>
 * {@code @Order(HIGHEST_PRECEDENCE)} guarantees this advice wins the election
 * before Spring's own {@code DefaultHandlerExceptionResolver}, so our structured
 * {@link ApiErrorDTO} is always returned for REST errors instead of Spring's
 * generic HTML error page.
 *
 * <h3>Response format</h3>
 * All handlers return {@code application/json} via {@link ApiErrorDTO} records.
 * Example:
 * <pre>{@code
 * {
 *   "path":       "/api/v1/offers/99",
 *   "message":    "Offer not found with id: 99",
 *   "statusCode": 404,
 *   "timestamp":  "2026-04-29T22:54:01.123"
 * }
 * }</pre>
 */
@RestControllerAdvice(annotations = RestController.class)   // ← CRITICAL isolation filter
@Order(Ordered.HIGHEST_PRECEDENCE)                          // ← wins before Spring defaults
public class GlobalRestControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestControllerAdvice.class);

    // ── 404 — Resource not found ───────────────────────────────────────────────

    /**
     * Handles {@link NoSuchElementException}, typically thrown by
     * {@code Optional.orElseThrow()} in service methods when an entity
     * with the requested ID does not exist in the database.
     *
     * @param ex      the raised exception carrying the error message.
     * @param request the inbound HTTP request, used to extract the URI path.
     * @return {@code 404 NOT FOUND} with a structured {@link ApiErrorDTO} body.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(
            NoSuchElementException ex, HttpServletRequest request) {

        log.warn("[REST API] 404 NOT FOUND — path: '{}', reason: '{}'",
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorDTO.of(
                        request.getRequestURI(),
                        ex.getMessage() != null ? ex.getMessage() : "Resource not found",
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    // ── 400 — Validation failure ───────────────────────────────────────────────

    /**
     * Handles {@link MethodArgumentNotValidException}, raised when a
     * {@code @RequestBody} annotated with {@code @Valid} fails Bean Validation
     * (JSR-380) constraints.
     *
     * <p>All individual field errors are collected and joined into a single,
     * comma-separated message for API consumers (avoids returning a wall of JSON
     * nested inside the error body).</p>
     *
     * @param ex      the validation exception containing all field-level errors.
     * @param request the inbound HTTP request, used to extract the URI path.
     * @return {@code 400 BAD REQUEST} with a structured {@link ApiErrorDTO} body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Collect all field errors into "field: message, field: message, …"
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[REST API] 400 BAD REQUEST — path: '{}', violations: '{}'",
                request.getRequestURI(), details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.of(
                        request.getRequestURI(),
                        "Validation failed — " + details,
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    // ── 401 — Authentication failure ───────────────────────────────────────────────

    /**
     * Handles AuthenticationException, raised when login credentials (like passwords) are incorrect.
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiErrorDTO> handleAuthentication(
            org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {

        log.warn("[REST API] 401 UNAUTHORIZED — path: '{}', reason: '{}'",
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorDTO.of(
                        request.getRequestURI(),
                        "Credenciales incorrectas",
                        HttpStatus.UNAUTHORIZED.value()
                ));
    }

    // ── 500 — Unexpected server error (fallback) ───────────────────────────────

    /**
     * Catch-all fallback for any unhandled exception that escapes service or
     * controller layers. Logs the full stack trace for operational visibility
     * while returning a generic message to the API consumer (no internal
     * details leaked).
     *
     * @param ex      any unhandled {@link Exception}.
     * @param request the inbound HTTP request, used to extract the URI path.
     * @return {@code 500 INTERNAL SERVER ERROR} with a structured {@link ApiErrorDTO} body.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("[REST API] 500 INTERNAL SERVER ERROR — path: '{}', exception: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorDTO.of(
                        request.getRequestURI(),
                        "An unexpected internal error occurred. Please contact support.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}
