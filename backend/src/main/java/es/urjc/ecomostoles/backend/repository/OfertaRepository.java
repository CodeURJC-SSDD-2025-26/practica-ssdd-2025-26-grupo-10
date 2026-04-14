package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for Oferta entity.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {
    
    long countByEstado(EstadoOferta estado);

    /**
     * Finds a list of offers associated with a specific company.
     * Use second parameter to choose between Oferta entity or OfertaResumen projection.
     */
    <T> List<T> findByEmpresa(Empresa empresa, Class<T> type);

    long countByEmpresa(Empresa empresa);

    @Query("SELECT o FROM Oferta o JOIN FETCH o.empresa WHERE o.estado = :estado ORDER BY o.fechaPublicacion DESC LIMIT 3")
    <T> List<T> findTop3ByEstadoOrderByFechaPublicacionDesc(@Param("estado") EstadoOferta estado, Class<T> type);

    @Query("SELECT o FROM Oferta o JOIN FETCH o.empresa WHERE o.estado = :estado")
    <T> List<T> findByEstadoJoinEmpresa(@Param("estado") EstadoOferta estado, Class<T> type);

    @Query("SELECT o FROM Oferta o JOIN FETCH o.empresa ORDER BY o.fechaPublicacion DESC LIMIT 50")
    <T> List<T> findTop50ByOrderByFechaPublicacionDesc(Class<T> type);
}
