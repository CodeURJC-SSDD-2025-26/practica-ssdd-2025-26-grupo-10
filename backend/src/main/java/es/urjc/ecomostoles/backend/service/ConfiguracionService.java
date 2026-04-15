package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.ConfiguracionGlobal;
import es.urjc.ecomostoles.backend.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    // Standard Platform Defaults
    public static final String DEFAULT_EMAIL    = "soporte@ecomostoles.com";
    public static final String DEFAULT_COMISION = "2.5";
    public static final String DEFAULT_CATEGORIAS = "Residuo Metálico\nResiduo Peligroso\nResiduo Madera\nReciclaje Plástico\nResiduo Electrónico (RAEE)\nResiduo Textil\nExcedentes de Obra";
    public static final String DEFAULT_PLATFORM_NAME = "EcoMóstoles";
    public static final String DEFAULT_PLATFORM_CITY = "Móstoles";
    public static final String DEFAULT_PLATFORM_LOCATION = "Polígono Regordoño";
    public static final String DEFAULT_LISTA_UNIDADES = "kg\nuds\ntoneladas\nm2\nlitros";
    public static final String DEFAULT_LISTA_DISPONIBILIDAD = "Inmediata\nEn 1 semana\nConsultar";
    public static final String DEFAULT_LISTA_SECTORES = "Construcción\nEnergía\nAlimentación\nMetalurgia\nAutomoción\nTextil\nQuímica";

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfiguracionService.class);

    @Transactional
    public void guardarOActualizarConfiguracion(String clave, String valor) {
        log.debug("Persisitiendo configuración -> Clave: {}, Valor: {}", clave, valor);
        ConfiguracionGlobal config = configuracionRepository.findByClave(clave)
                .orElse(new ConfiguracionGlobal(clave, valor));
        config.setValor(valor);
        configuracionRepository.save(config);
    }

    @Transactional(readOnly = true)
    public String obtenerValorConfiguracion(String clave, String valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(ConfiguracionGlobal::getValor)
                .orElse(valorPorDefecto);
    }

    /**
     * Specialized lookup that automatically injects the official platform defaults
     * if the key is missing from the database.
     */
    public String obtenerValorAuto(String clave) {
        String fallback = switch (clave) {
            case "emailContacto" -> DEFAULT_EMAIL;
            case "comisionPlataforma" -> DEFAULT_COMISION;
            case "listaCategorias" -> DEFAULT_CATEGORIAS;
            case "listaUnidades" -> DEFAULT_LISTA_UNIDADES;
            case "listaDisponibilidades" -> DEFAULT_LISTA_DISPONIBILIDAD;
            case "listaSectores" -> DEFAULT_LISTA_SECTORES;
            case "platformName" -> DEFAULT_PLATFORM_NAME;
            case "platformCity" -> DEFAULT_PLATFORM_CITY;
            case "platformLocation" -> DEFAULT_PLATFORM_LOCATION;
            case "modoMantenimiento" -> "false";
            case "social_linkedin" -> "https://linkedin.com/company/ecomostoles";
            case "social_twitter" -> "https://x.com/ecomostoles";
            case "social_facebook" -> "https://facebook.com/ecomostoles";
            case "platformStatus" -> "SISTEMA ONLINE";
            default -> "";
        };
        return obtenerValorConfiguracion(clave, fallback);
    }

    /**
     * Retrieves a configuration value as a sanitized list of strings.
     * Splitting by newline, trimming, and filtering empty strings.
     */
    public java.util.List<String> obtenerListaSanitizada(String clave) {
        String data = obtenerValorAuto(clave);
        if (data == null || data.isEmpty()) return java.util.Collections.emptyList();
        
        return java.util.Arrays.stream(data.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
