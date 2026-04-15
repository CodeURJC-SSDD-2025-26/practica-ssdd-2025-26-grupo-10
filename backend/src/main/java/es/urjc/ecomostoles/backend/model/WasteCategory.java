package es.urjc.ecomostoles.backend.model;

/**
 * Technical definition of waste categories.
 * Members are in English (Technical Standard), 
 * while display names are in Spanish (User Interface).
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
