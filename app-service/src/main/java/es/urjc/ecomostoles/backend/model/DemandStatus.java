package es.urjc.ecomostoles.backend.model;

/**
 * State-machine taxonomy for Demand lifecycle workflows.
 * 
 * Exposes fixed translation schemas directly coupled to the View layer 
 * to decouple Mustache rendering conditions from raw database primitives.
 */
public enum DemandStatus {
    ACTIVE("Activa"),
    PAUSED("Pausada"),
    CLOSED("Cerrada");

    private final String displayName;

    DemandStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
