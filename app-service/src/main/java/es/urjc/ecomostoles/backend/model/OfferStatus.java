package es.urjc.ecomostoles.backend.model;

/**
 * State-machine taxonomy for Offer progression logic.
 * 
 * Embeds localized Spanish terms strictly for rendering layers while ensuring native Business
 * Logic acts exclusively upon the absolute English Enum references.
 */
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
