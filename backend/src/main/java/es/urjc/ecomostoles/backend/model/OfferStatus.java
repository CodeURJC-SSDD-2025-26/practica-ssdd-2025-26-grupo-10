package es.urjc.ecomostoles.backend.model;

public enum OfferStatus {
    ACTIVE("Activa"),
    RESERVED("Reservada"),
    PAUSED("Pausada"),
    IN_NEGOTIATION("En Negociación"),
    FINISHED("Finalizada"),
    REPORTED("Reportada");

    private final String displayName;

    OfferStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
