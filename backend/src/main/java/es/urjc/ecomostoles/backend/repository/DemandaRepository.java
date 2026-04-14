package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.EstadoDemanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByEmpresa(Empresa empresa);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa WHERE d.estado = :estado AND d.empresa IS NOT NULL AND d.empresa.id != :empresaId AND LOWER(d.empresa.sectorIndustrial) = LOWER(:sector)")
    List<Demanda> findSmartRecommendations(@Param("estado") EstadoDemanda estado, @Param("empresaId") Long empresaId, @Param("sector") String sector, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa WHERE d.estado = :estado")
    List<Demanda> findByEstadoJoinEmpresa(@Param("estado") EstadoDemanda estado);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa ORDER BY d.fechaPublicacion DESC LIMIT 50")
    List<Demanda> findTop50ByOrderByFechaPublicacionDesc();
}
