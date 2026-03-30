package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Acuerdo entity.
 * Provides methods to retrieve agreements based on source or destination companies.
 */
public interface AcuerdoRepository extends JpaRepository<Acuerdo, Long> {

    /**
     * Retrieves a list of agreements where the given company is the originator (source).
     *
     * @param empresaOrigen the source company
     * @return a list of agreements
     */
    List<Acuerdo> findByEmpresaOrigen(Empresa empresaOrigen);

    /**
     * Retrieves a list of agreements where the given company is the recipient (destination).
     *
     * @param empresaDestino the destination company
     * @return a list of agreements
     */
    List<Acuerdo> findByEmpresaDestino(Empresa empresaDestino);
}
