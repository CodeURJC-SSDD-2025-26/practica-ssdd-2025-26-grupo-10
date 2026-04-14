package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import es.urjc.ecomostoles.backend.model.EstadoDemanda;
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

    @Query("SELECT d FROM Demanda d WHERE d.estado = :estado AND d.empresa IS NOT NULL AND d.empresa.id != :empresaId AND LOWER(d.empresa.sectorIndustrial) = LOWER(:sector)")
    List<Demanda> findSmartRecommendations(@Param("estado") EstadoDemanda estado, @Param("empresaId") Long empresaId, @Param("sector") String sector, org.springframework.data.domain.Pageable pageable);

    List<Demanda> findByEstado(EstadoDemanda estado);
}
