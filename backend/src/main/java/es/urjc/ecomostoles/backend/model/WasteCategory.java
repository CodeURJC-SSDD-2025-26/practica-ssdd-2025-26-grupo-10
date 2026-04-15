package es.urjc.ecomostoles.backend.model;

/**
 * Taxonomy schema strictly cataloging domain material clusters.
 * 
 * Enforces robust English technical namespaces while embedding localized Spanish display
 * aliases. Optimizes query-binding and distributes configurations natively across the
 * platform ecosystem.
 */
public enum WasteCategory {
    METAL_WASTE("Residuo Metálico"),
    HAZARDOUS_WASTE("Residuo Peligroso"),
    WOOD_WASTE("Residuo Madera"),
    PLASTIC_WASTE("Reciclaje Plástico"),
    E_WASTE("Residuo Electrónico (RAEE)"),
    TEXTILE_WASTE("Residuo Textil"),
    CONSTRUCTION_WASTE("Excedentes de Obra"),
    PAPER_WASTE("Papel y Cartón"),
    GLASS_WASTE("Residuo de Vidrio");

    private final String displayName;

    WasteCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name();
    }
}
