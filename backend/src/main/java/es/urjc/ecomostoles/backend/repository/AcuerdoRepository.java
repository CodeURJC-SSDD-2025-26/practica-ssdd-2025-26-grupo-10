package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Repository interface for Acuerdo entity.
 * Provides methods to retrieve agreements based on source or destination companies.
 */
public interface AcuerdoRepository extends JpaRepository<Acuerdo, Long> {
    
    /**
     * Counts agreements by their status.
     * @param estado the status to count
     * @return count of agreements
     */
    long countByEstado(EstadoAcuerdo estado);

    /**
     * Retrieves a list of agreements where the given company is the originator (source).
     *
     * @param empresaOrigen the source company
     * @return a list of agreements
     */
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino LEFT JOIN FETCH a.oferta WHERE a.empresaOrigen = :empresaOrigen")
    List<Acuerdo> findByEmpresaOrigen(@org.springframework.data.repository.query.Param("empresaOrigen") Empresa empresaOrigen);

    /**
     * Retrieves a list of agreements where the given company is the recipient (destination).
     *
     * @param empresaDestino the destination company
     * @return a list of agreements
     */
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino LEFT JOIN FETCH a.oferta WHERE a.empresaDestino = :empresaDestino")
    List<Acuerdo> findByEmpresaDestino(@org.springframework.data.repository.query.Param("empresaDestino") Empresa empresaDestino);

    /**
     * Retrieves a list of agreements where the given company is involved as source or destination.
     *
     * @param empresa the involved company
     * @return a list of agreements
     */
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino LEFT JOIN FETCH a.oferta LEFT JOIN FETCH a.demanda WHERE a.empresaOrigen = :empresa OR a.empresaDestino = :empresa")
    List<Acuerdo> findByEmpresa(@org.springframework.data.repository.query.Param("empresa") Empresa empresa);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Acuerdo a WHERE a.empresaOrigen = :empresa OR a.empresaDestino = :empresa")
    long countByEmpresa(@org.springframework.data.repository.query.Param("empresa") Empresa empresa);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(a.cantidad) FROM Acuerdo a WHERE (a.empresaOrigen = :empresa OR a.empresaDestino = :empresa) AND a.estado = :estado")
    Double sumCantidadByEmpresaAndEstado(@org.springframework.data.repository.query.Param("empresa") Empresa empresa, @org.springframework.data.repository.query.Param("estado") EstadoAcuerdo estado);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Acuerdo a WHERE (a.empresaOrigen = :empresa OR a.empresaDestino = :empresa) AND a.estado = :estado")
    long countByEmpresaAndEstado(@org.springframework.data.repository.query.Param("empresa") Empresa empresa, @org.springframework.data.repository.query.Param("estado") es.urjc.ecomostoles.backend.model.EstadoAcuerdo estado);

    @org.springframework.data.jpa.repository.Query(value = "SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino",
            countQuery = "SELECT COUNT(a) FROM Acuerdo a")
    Page<Acuerdo> findAllPaginated(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino ORDER BY a.fechaRegistro DESC LIMIT 50")
    List<Acuerdo> findTop50ByOrderByFechaRegistroDesc();
 


    @org.springframework.data.jpa.repository.Query("SELECT a FROM Acuerdo a JOIN FETCH a.empresaOrigen JOIN FETCH a.empresaDestino LEFT JOIN FETCH a.oferta WHERE a.estado = :estado")
    List<Acuerdo> findAllByEstado(@org.springframework.data.repository.query.Param("estado") EstadoAcuerdo estado);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(a.cantidad) FROM Acuerdo a WHERE a.estado = :estado")
    Double sumTotalCantidadByEstado(@org.springframework.data.repository.query.Param("estado") EstadoAcuerdo estado);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(a.impactoCO2) FROM Acuerdo a WHERE a.estado = 'COMPLETADO'")
    Double sumTotalImpactoCO2Completado();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(a.impactoCO2) FROM Acuerdo a WHERE (a.empresaOrigen = :empresa OR a.empresaDestino = :empresa) AND a.estado = 'COMPLETADO'")
    Double sumImpactoCO2ByEmpresaCompletado(@org.springframework.data.repository.query.Param("empresa") Empresa empresa);

    long countByFechaRegistroAfter(java.time.LocalDateTime fecha);

    long countByEstadoAndFechaRegistroAfter(EstadoAcuerdo estado, java.time.LocalDateTime fecha);

    long countByOferta_Id(Long id);

    long countByDemanda_Id(Long id);
}
