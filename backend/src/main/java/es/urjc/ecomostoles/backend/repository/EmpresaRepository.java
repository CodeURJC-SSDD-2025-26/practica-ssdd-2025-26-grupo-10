package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByEmailContacto(String emailContacto);
    Optional<Empresa> findByNombreComercial(String nombreComercial);
    /** Search by CIF (unique field): prevents DataIntegrityViolationException during upsert. */
    Optional<Empresa> findByCif(String cif);

    List<Empresa> findTop50ByOrderByIdDesc();

    Page<Empresa> findByNombreComercialContainingIgnoreCaseOrEmailContactoContainingIgnoreCaseOrCifContainingIgnoreCase(String q1, String q2, String q3, Pageable pageable);
}
