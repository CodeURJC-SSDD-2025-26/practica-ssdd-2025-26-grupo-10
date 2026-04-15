package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.GlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<GlobalConfiguration, Long> {
    Optional<GlobalConfiguration> findByKey(String key);
}
