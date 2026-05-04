package es.urjc.ecomostoles.backend.exception;

/**
 * Logical violation boundary halting internal asset contracts.
 * 
 * Custom RuntimeException forcibly aborting contextual transactions where an authenticated 
 * Tenant attempts to map commercial treaties targeting their proprietary Offers.
 * Architecturally prevents structural data-poisoning via self-contracting loops.
 */
public class SelfAgreementException extends RuntimeException {
    public SelfAgreementException(String message) {
        super(message);
    }
}
