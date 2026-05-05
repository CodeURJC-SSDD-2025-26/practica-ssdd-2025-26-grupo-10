package es.urjc.ecomostoles.backend.controller.api.exception;

import java.time.LocalDateTime;

/**
 * Standardised error payload returned by the REST API on all failure paths.
 *
 * <p>All fields are intentionally plain types to ensure clean JSON serialisation
 * by Jackson without any custom configuration:
 * <ul>
 *   <li>{@code path}       — the request URI that triggered the error (e.g. {@code /api/v1/offers/99})</li>
 *   <li>{@code message}    — human-readable description of what went wrong</li>
 *   <li>{@code statusCode} — the numeric HTTP status (400, 404, 500 …)</li>
 *   <li>{@code timestamp}  — ISO-8601 instant when the error was captured</li>
 * </ul>
 *
 * <p>Example JSON response:
 * <pre>{@code
 * {
 *   "path":       "/api/v1/offers/99",
 *   "message":    "Offer not found with id: 99",
 *   "statusCode": 404,
 *   "timestamp":  "2026-04-29T22:54:01.123"
 * }
 * }</pre>
 */
public record ApiErrorDTO(
        String        path,
        String        message,
        int           statusCode,
        LocalDateTime timestamp
) {
    /**
     * Convenience factory — captures the current instant automatically.
     */
    public static ApiErrorDTO of(String path, String message, int statusCode) {
        return new ApiErrorDTO(path, message, statusCode, LocalDateTime.now());
    }
}
