package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.EstadoDemanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Demanda> findByEmpresa(Empresa empresa, Pageable pageable);

    Page<Demanda> findByEstado(EstadoDemanda estado, Pageable pageable);

    long countByEmpresa(Empresa empresa);

    long countByEmpresaAndEstado(Empresa empresa, EstadoDemanda estado);

    @Query("SELECT o FROM es.urjc.ecomostoles.backend.model.Oferta o JOIN FETCH o.empresa WHERE o.estado = es.urjc.ecomostoles.backend.model.EstadoOferta.ACTIVA AND o.empresa.id != :empresaId AND o.tipoResiduo IN (SELECT d.categoriaMaterial FROM Demanda d WHERE d.empresa.id = :empresaId AND d.estado = es.urjc.ecomostoles.backend.model.EstadoDemanda.ACTIVA)")
    List<es.urjc.ecomostoles.backend.model.Oferta> findOfertasMatchingDemanda(@Param("empresaId") Long empresaId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa WHERE d.estado = :estado")
    List<Demanda> findByEstadoJoinEmpresa(@Param("estado") EstadoDemanda estado);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa ORDER BY d.fechaPublicacion DESC")
    Page<Demanda> findAllPaginated(Pageable pageable);

    @Query("SELECT d FROM Demanda d JOIN FETCH d.empresa ORDER BY d.fechaPublicacion DESC LIMIT 50")
    List<Demanda> findTop50ByOrderByFechaPublicacionDesc();
}
