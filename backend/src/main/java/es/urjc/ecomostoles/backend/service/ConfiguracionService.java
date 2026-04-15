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

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional
    public void guardarOActualizarConfiguracion(String clave, String valor) {
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
            case "platformName" -> DEFAULT_PLATFORM_NAME;
            case "platformCity" -> DEFAULT_PLATFORM_CITY;
            case "platformLocation" -> DEFAULT_PLATFORM_LOCATION;
            case "modoMantenimiento" -> "false";
            default -> "";
        };
        return obtenerValorConfiguracion(clave, fallback);
    }
}
