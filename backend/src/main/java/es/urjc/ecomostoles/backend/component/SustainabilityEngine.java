package es.urjc.ecomostoles.backend.component;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;

/**
 * Centralized engine for calculating environmental impact and sustainability metrics.
 * Follows the DRY principle to avoid scattered mathematical factors across services.
 */
@Component
public class SustainabilityEngine {

    private static final Logger log = LoggerFactory.getLogger(SustainabilityEngine.class);

    private static final double FACTOR_EMISION_CO2 = 0.45;

    private final ConfiguracionService configuracionService;

    public SustainabilityEngine(ConfiguracionService configuracionService) {
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
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("⚠️ ERROR CRÍTICO DE CONFIGURACIÓN: El factor 'CO2_FACTOR' en BD ('{}') no es un número válido. Usando valor hardcoded de seguridad: {}. Fallo: {}", factorStr, FACTOR_EMISION_CO2, e.getMessage());
            factor = FACTOR_EMISION_CO2;
        }
        return kilosMaterial * factor;
    }
}
