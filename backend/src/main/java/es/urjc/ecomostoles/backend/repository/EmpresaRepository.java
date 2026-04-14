package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByEmailContacto(String emailContacto);
    Optional<Empresa> findByNombreComercial(String nombreComercial);
    /** Búsqueda por CIF (campo único): evita DataIntegrityViolationException en el upsert. */
    Optional<Empresa> findByCif(String cif);

    List<Empresa> findTop50ByOrderByIdDesc();

    List<Empresa> findByNombreComercialContainingIgnoreCaseOrEmailContactoContainingIgnoreCaseOrCifContainingIgnoreCase(String q1, String q2, String q3);
}
