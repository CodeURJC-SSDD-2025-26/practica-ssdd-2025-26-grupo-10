package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.ConfiguracionGlobal;
import es.urjc.ecomostoles.backend.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

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
}
