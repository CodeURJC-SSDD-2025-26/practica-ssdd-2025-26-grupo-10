package es.urjc.ecomostoles.backend.model;

/**
 * State-machine mapping for Agreement progression.
 * 
 * Dictates discrete lifecycle states (Pending -> Accepted -> Completed). Bound natively 
 * via JPA @Enumerated to ensure strict database conformance and UI synchronization.
 */
public enum AgreementStatus {
    PENDING("Pendiente"),
    IN_PROGRESS("En Progreso"),
    COMPLETED("Completado"),
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado");

    private final String displayName;

    AgreementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
