package es.urjc.ecomostoles.backend.utils;

import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.service.ConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * UI Projection Factory for Form components.
 * 
 * Centralizes the transformation of domain Enums and configuration lists into 
 * standardized SelectOption payloads compatible with Mustache templates. 
 * Implements the DRY (Don't Repeat Yourself) principle for UI metadata generation.
 */
public class FormOptionsHelper {

    /**
     * Builds a list of SelectOptions for agreement units from the configuration.
     */
    public static List<SelectOption> getUnitOptions(ConfigurationService configurationService,
            String selectedValue) {
        List<String> units = configurationService.getSanitizedList("unitList");
        List<SelectOption> options = new ArrayList<>();
        for (String unit : units) {
            options.add(new SelectOption(unit, unit, unit.equals(selectedValue)));
        }
        return options;
    }

    /**
     * Builds a list of SelectOptions for all AgreementStatus enum values with
     * human-friendly labels.
     */
    public static List<SelectOption> getAgreementStatusOptions(AgreementStatus selectedValue) {
        List<SelectOption> options = new ArrayList<>();
        for (AgreementStatus status : AgreementStatus.values()) {
            String label = getHumanLabel(status);
            options.add(new SelectOption(status.name(), label, status.equals(selectedValue)));
        }
        return options;
    }

    /**
     * Builds a list of SelectOptions for urgency/availability from the configuration.
     */
    public static List<SelectOption> getUrgencyOptions(ConfigurationService configurationService,
            String selectedValue) {
        List<String> items = configurationService.getSanitizedList("availabilityList");
        List<SelectOption> options = new ArrayList<>();
        for (String item : items) {
            options.add(new SelectOption(item, item, item.equals(selectedValue)));
        }
        return options;
    }

    /**
     * Builds a list of SelectOptions for demand validity (hardcoded standards).
     */
    public static List<SelectOption> getValidityOptions(String selectedValue) {
        List<SelectOption> options = new ArrayList<>();
        options.add(new SelectOption("7", "7 días", "7".equals(selectedValue)));
        options.add(new SelectOption("15", "15 días", "15".equals(selectedValue)));
        options.add(new SelectOption("30", "30 días", "30".equals(selectedValue)));
        options.add(new SelectOption("90", "90 días", "90".equals(selectedValue)));
        options.add(new SelectOption("0", "Indefinido / Consultar", "0".equals(selectedValue)));
        return options;
    }

    /**
     * Executes advanced taxonomy mapping for material categories.
     * 
     * Dynamically negotiates between literal configuration strings and internal 
     * WasteCategory Enum constants to ensure bidirectional consistency between 
     * the presentation layer and back-end analytical engines.
     * 
     * @param configurationService Provider for dynamic configuration lists.
     * @param selectedValue Current state for pre-selection reconciliation.
     * @return Standardized option array for dropdown rendering.
     */
    public static List<SelectOption> getCategoryOptions(ConfigurationService configurationService,
            Object selectedValue) {
        List<String> labels = configurationService.getSanitizedList("categoryList");
        List<SelectOption> options = new ArrayList<>();

        String selectedStr = null;
        if (selectedValue != null) {
            if (selectedValue instanceof es.urjc.ecomostoles.backend.model.WasteCategory cat) {
                selectedStr = cat.name();
            } else {
                selectedStr = selectedValue.toString();
            }
        }

        for (String label : labels) {
            String value = label; // Default value is the label itself
            
            // Try to find matching Enum constant for technical logic (CO2, etc)
            for (es.urjc.ecomostoles.backend.model.WasteCategory cat : es.urjc.ecomostoles.backend.model.WasteCategory.values()) {
                if (cat.getDisplayName().equalsIgnoreCase(label) || cat.name().equalsIgnoreCase(label)) {
                    value = cat.name();
                    break;
                }
            }
            
            boolean isSelected = false;
            if (selectedStr != null) {
                isSelected = value.equalsIgnoreCase(selectedStr) || label.equalsIgnoreCase(selectedStr);
            }

            options.add(new SelectOption(value, label, isSelected));
        }
        return options;
    }

    private static String getHumanLabel(AgreementStatus status) {
        switch (status) {
            case PENDING:
                return "Pendiente de firma";
            case IN_PROGRESS:
                return "En curso / Procesando";
            case COMPLETED:
                return "Completado / Finalizado";
            case ACCEPTED:
                return "Aceptado";
            case REJECTED:
                return "Rechazado";
            default:
                return status.name();
        }
    }
}
