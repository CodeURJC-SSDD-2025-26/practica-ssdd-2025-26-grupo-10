package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.ImpactFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for ImpactFactor entity.
 * Provides lookup capabilities by waste category.
 */
public interface ImpactFactorRepository extends JpaRepository<ImpactFactor, Long> {

    /**
     * Finds an impact factor by its category string (case-insensitive).
     *
     * @param category the material/waste category (e.g., 'metal', 'plastico')
     * @return an Optional containing the ImpactFactor if found
     */
    Optional<ImpactFactor> findByCategoryIgnoreCase(String category);
}
