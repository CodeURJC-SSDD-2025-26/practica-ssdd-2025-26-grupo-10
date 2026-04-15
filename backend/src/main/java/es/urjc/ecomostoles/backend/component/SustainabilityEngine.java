package es.urjc.ecomostoles.backend.component;

import org.springframework.stereotype.Component;

/**
 * Centralized engine for calculating environmental impact and sustainability metrics.
 * Follows the DRY principle to avoid scattered mathematical factors across services.
 */
@Component
public class SustainabilityEngine {

    /**
     * Emission factor: 0.45 tons of CO2 avoided for each unit (kg/ton) of material successfully re-introduced.
     */
    private static final double FACTOR_EMISION_CO2 = 0.45;

    /**
     * Calculates the CO2 savings based on the amount of material recycled or re-introduced.
     * 
     * @param kilosMaterial The amount of material in kilograms or units.
     * @return The calculated CO2 impact in tons.
     */
    public double calcularImpactoCO2(double kilosMaterial) {
        return kilosMaterial * FACTOR_EMISION_CO2;
    }
}
