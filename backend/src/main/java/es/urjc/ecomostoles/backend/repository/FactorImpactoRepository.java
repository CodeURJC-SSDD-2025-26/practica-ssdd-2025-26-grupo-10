package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.FactorImpacto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for FactorImpacto entity.
 * Provides lookup capabilities by waste category.
 */
public interface FactorImpactoRepository extends JpaRepository<FactorImpacto, Long> {
    
    /**
     * Finds an impact factor by its category string (case-insensitive).
     *
     * @param categoria the material/waste category (e.g., 'metal', 'plastico')
     * @return an Optional containing the FactorImpacto if found
     */
    Optional<FactorImpacto> findByCategoriaIgnoreCase(String categoria);
}
