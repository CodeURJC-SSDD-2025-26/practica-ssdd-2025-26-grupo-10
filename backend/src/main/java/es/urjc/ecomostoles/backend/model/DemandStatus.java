package es.urjc.ecomostoles.backend.model;

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
