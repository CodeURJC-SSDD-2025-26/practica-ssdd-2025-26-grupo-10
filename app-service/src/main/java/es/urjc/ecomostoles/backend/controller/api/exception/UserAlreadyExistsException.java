package es.urjc.ecomostoles.backend.controller.api.exception;

/**
 * Exception thrown when a registration attempt is made with an identity (Email or CIF)
 * that already exists in the system.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
