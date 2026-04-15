package es.urjc.ecomostoles.backend.component;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import es.urjc.ecomostoles.backend.repository.ImpactFactorRepository;
import es.urjc.ecomostoles.backend.model.ImpactFactor;

/**
 * Centralized engine for calculating environmental impact and sustainability
 * metrics.
 * Follows the DRY principle to avoid scattered mathematical factors across
 * services.
 */
@Component
public class SustainabilityEngine {

    private static final Logger log = LoggerFactory.getLogger(SustainabilityEngine.class);

    @Value("${app.sustainability.default-co2-factor:0.45}")
    private double defaultCo2Factor;

    private final ConfigurationService configurationService;
    private final ImpactFactorRepository impactFactorRepository;

    public SustainabilityEngine(ConfigurationService configurationService,
            ImpactFactorRepository impactFactorRepository) {
        this.configurationService = configurationService;
        this.impactFactorRepository = impactFactorRepository;
    }

    /**
     * Calculates the CO2 savings based on the amount of material recycled or
     * re-introduced.
     * Fetches the factor from the database dynamically and applies a
     * material-specific multiplier.
     * 
     * @param materialAmount The amount of material in kilograms or units.
     * @param wasteType      The type of material (Plastic, Metal, Wood, etc.) for
     *                       granular calculation.
     * @return The calculated CO2 impact in tons.
     */
    public double calculateCo2Impact(double materialAmount,
            es.urjc.ecomostoles.backend.model.WasteCategory wasteCategory) {
        String factorStr = configurationService.getConfigurationValue("CO2_FACTOR",
                String.valueOf(defaultCo2Factor));
        double factor;
        try {
            factor = Double.parseDouble(factorStr);
        } catch (NumberFormatException | NullPointerException e) {
            log.warn(
                    "⚠️ CRITICAL CONFIGURATION ERROR: The 'CO2_FACTOR' factor in DB ('{}') is not a valid number. Using safety injected value: {}. Failure: {}",
                    factorStr, defaultCo2Factor, e.getMessage());
            factor = defaultCo2Factor;
        }

        // Granular Multipliers fetched from the dedicated environmental factors table
        double materialMultiplier = 1.0;
        if (wasteCategory != null) {
            materialMultiplier = impactFactorRepository.findByCategoryIgnoreCase(wasteCategory.name())
                    .map(ImpactFactor::getMultiplier)
                    .orElse(1.0);
        }

        return materialAmount * factor * materialMultiplier;
    }
}
