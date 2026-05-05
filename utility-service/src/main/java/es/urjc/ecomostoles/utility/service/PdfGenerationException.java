package es.urjc.ecomostoles.utility.service;

/**
 * Unchecked exception wrapping OpenPDF failures during document construction.
 *
 * Surfaced as HTTP 500 by the global exception handler in PdfRestController.
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
