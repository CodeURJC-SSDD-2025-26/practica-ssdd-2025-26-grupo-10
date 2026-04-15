package es.urjc.ecomostoles.backend.utils;

import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.service.ConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to eliminate code duplication in form option generation.
 * Follows the DRY principle by centralizing SelectOption list creation.
 */
public class FormOptionsHelper {

    /**
     * Builds a list of SelectOptions for agreement units from the configuration.
     */
    public static List<SelectOption> getUnitOptions(ConfigurationService configurationService,
            String selectedValue) {
        List<String> units = configurationService.getSanitizedList("listaUnidades");
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
