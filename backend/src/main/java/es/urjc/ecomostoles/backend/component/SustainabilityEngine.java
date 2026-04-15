package es.urjc.ecomostoles.backend.component;

import org.springframework.stereotype.Component;

/**
 * Centralized engine for calculating environmental impact and sustainability metrics.
 * Follows the DRY principle to avoid scattered mathematical factors across services.
 */
@Component
public class SustainabilityEngine {

    private static final double FACTOR_EMISION_CO2 = 0.45;

    private final es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService;

    public SustainabilityEngine(es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    /**
     * Calculates the CO2 savings based on the amount of material recycled or re-introduced.
     * Fetches the factor from the database dynamically.
     * 
     * @param kilosMaterial The amount of material in kilograms or units.
     * @return The calculated CO2 impact in tons.
     */
    public double calcularImpactoCO2(double kilosMaterial) {
        String factorStr = configuracionService.obtenerValorConfiguracion("CO2_FACTOR", String.valueOf(FACTOR_EMISION_CO2));
        double factor;
        try {
            factor = Double.parseDouble(factorStr);
        } catch (Exception e) {
            factor = FACTOR_EMISION_CO2;
        }
        return kilosMaterial * factor;
    }
}
