package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Oferta entity.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    /**
     * Finds a list of offers associated with a specific company.
     *
     * @param empresa the company entity
     * @return a list of offers belonging to the given company
     */
    List<Oferta> findByEmpresa(Empresa empresa);
}
