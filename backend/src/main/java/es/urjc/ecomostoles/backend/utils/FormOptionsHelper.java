package es.urjc.ecomostoles.backend.utils;

import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;

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
    public static List<SelectOption> getOpcionesUnidad(ConfiguracionService configuracionService, String selectedValue) {
        List<String> units = configuracionService.obtenerListaSanitizada("listaUnidades");
        List<SelectOption> options = new ArrayList<>();
        for (String unit : units) {
            options.add(new SelectOption(unit, unit, unit.equals(selectedValue)));
        }
        return options;
    }

    /**
     * Builds a list of SelectOptions for all EstadoAcuerdo enum values with human-friendly labels.
     */
    public static List<SelectOption> getOpcionesEstadoAcuerdo(EstadoAcuerdo selectedValue) {
        List<SelectOption> options = new ArrayList<>();
        for (EstadoAcuerdo estado : EstadoAcuerdo.values()) {
            String label = getHumanLabel(estado);
            options.add(new SelectOption(estado.name(), label, estado.equals(selectedValue)));
        }
        return options;
    }

    private static String getHumanLabel(EstadoAcuerdo estado) {
        switch (estado) {
            case PENDIENTE: return "Pendiente de firma";
            case EN_CURSO: return "En curso / Procesando";
            case COMPLETADO: return "Completado / Finalizado";
            case ACEPTADO: return "Aceptado";
            case RECHAZADO: return "Rechazado";
            default: return estado.name();
        }
    }
}
