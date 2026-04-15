package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.GlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for volatile system-wide configurations.
 * 
 * Facilitates the "Hot Reload" pattern for platform parameters (Commissions, Service Slugs) 
 * allowing operational tuning without necessitating a JVM restart or deployment cycle.
 */
public interface ConfigurationRepository extends JpaRepository<GlobalConfiguration, Long> {
    Optional<GlobalConfiguration> findByKey(String key);
}
