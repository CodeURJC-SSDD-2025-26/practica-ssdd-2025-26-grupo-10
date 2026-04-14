package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for Oferta entity.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @Modifying
    @Query("UPDATE Oferta o SET o.visitas = o.visitas + 1 WHERE o.id = :id")
    void incrementarVisitas(@Param("id") Long id);
    
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

    @Query("SELECT o FROM Oferta o JOIN FETCH o.empresa " +
           "WHERE o.estado = :estado " +
           "AND (:kw IS NULL OR :kw = '' OR LOWER(o.titulo) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(o.descripcion) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:tipo IS NULL OR :tipo = '' OR o.tipoResiduo = :tipo) " +
           "AND (:poligono IS NULL OR :poligono = '' OR LOWER(o.empresa.direccion) LIKE LOWER(CONCAT('%', :poligono, '%')))")
    List<OfertaResumen> buscarFiltrado(@Param("estado") EstadoOferta estado, @Param("kw") String kw, @Param("tipo") String tipo, @Param("poligono") String poligono);
}
