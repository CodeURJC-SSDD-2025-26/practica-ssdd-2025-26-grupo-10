package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Demanda entity.
 * Handles database operations for requests made by companies.
 */
public interface DemandaRepository extends JpaRepository<Demanda, Long> {

    /**
     * Find all demands published by a specific company.
     * 
     * @param empresa the company entity
     * @return a list of demands for that company
     */
    List<Demanda> findByEmpresa(Empresa empresa);
}
