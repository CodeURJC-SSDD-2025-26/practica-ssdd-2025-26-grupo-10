package es.urjc.ecomostoles.backend.model;

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
