package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.GlobalConfiguration;
import es.urjc.ecomostoles.backend.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    // Standard Platform Defaults
    public static final String DEFAULT_EMAIL = "soporte@ecomostoles.com";
    public static final String DEFAULT_COMMISSION = "2.5";
    public static final String DEFAULT_CATEGORIES = "Residuo Metálico\nResiduo Peligroso\nResiduo Madera\nReciclaje Plástico\nResiduo Electrónico (RAEE)\nResiduo Textil\nExcedentes de Obra";
    public static final String DEFAULT_PLATFORM_NAME = "EcoMóstoles";
    public static final String DEFAULT_PLATFORM_CITY = "Móstoles";
    public static final String DEFAULT_PLATFORM_LOCATION = "Polígono Regordoño";
    public static final String DEFAULT_UNITS_LIST = "kg\nuds\ntoneladas\nm2\nlitros";
    public static final String DEFAULT_AVAILABILITY_LIST = "Inmediata\nEn 1 semana\nConsultar";
    public static final String DEFAULT_SECTORS_LIST = "Construcción\nEnergía\nAlimentación\nMetalurgia\nAutomoción\nTextil\nQuímica";

    public ConfigurationService(ConfigurationRepository configuracionRepository) {
        this.configurationRepository = configuracionRepository;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationService.class);

    @Transactional
    public void saveOrUpdateConfiguration(String key, String value) {
        log.debug("Persisting configuration -> Key: {}, Value: {}", key, value);
        GlobalConfiguration config = configurationRepository.findByKey(key)
                .orElse(new GlobalConfiguration(key, value));
        config.setValue(value);
        configurationRepository.save(config);
    }

    @Transactional(readOnly = true)
    public String getConfigurationValue(String key, String defaultValue) {
        return configurationRepository.findByKey(key)
                .map(GlobalConfiguration::getValue)
                .orElse(defaultValue);
    }

    /**
     * Specialized lookup that automatically injects the official platform defaults
     * if the key is missing from the database.
     */
    public String getAutoValue(String key) {
        String fallback = switch (key) {
            case "contactEmail" -> DEFAULT_EMAIL;
            case "platformCommission" -> DEFAULT_COMMISSION;
            case "categoryList" -> DEFAULT_CATEGORIES;
            case "unitList" -> DEFAULT_UNITS_LIST;
            case "availabilityList" -> DEFAULT_AVAILABILITY_LIST;
            case "sectorList" -> DEFAULT_SECTORS_LIST;
            case "platformName" -> DEFAULT_PLATFORM_NAME;
            case "platformCity" -> DEFAULT_PLATFORM_CITY;
            case "platformLocation" -> DEFAULT_PLATFORM_LOCATION;
            case "industrialAreaList" -> "Polígono Regordoño\nPolígono Las Nieves\nMóstoles Tecnológico\nOtro (Especificar)";
            case "maintenanceMode" -> "false";
            case "social_linkedin" -> "https://linkedin.com/company/ecomostoles";
            case "social_twitter" -> "https://x.com/ecomostoles";
            case "social_facebook" -> "https://facebook.com/ecomostoles";
            case "platformStatus" -> "SISTEMA ONLINE";
            default -> "";
        };
        return getConfigurationValue(key, fallback);
    }

    /**
     * Retrieves a configuration value as a sanitized list of strings.
     * Splitting by newline, trimming, and filtering empty strings.
     */
    public java.util.List<String> getSanitizedList(String key) {
        String data = getAutoValue(key);
        if (data == null || data.isEmpty())
            return java.util.Collections.emptyList();

        return java.util.Arrays.stream(data.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
