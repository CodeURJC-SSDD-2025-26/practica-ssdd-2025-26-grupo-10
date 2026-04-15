package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByContactEmail(String contactEmail);

    Optional<Company> findByCommercialName(String commercialName);

    /**
     * Search by tax Id (unique field): prevents DataIntegrityViolationException during
     * upsert.
     */
    Optional<Company> findByTaxId(String taxId);

    List<Company> findTop50ByOrderByIdDesc();

    Page<Company> findByCommercialNameContainingIgnoreCaseOrContactEmailContainingIgnoreCaseOrTaxIdContainingIgnoreCase(
            String q1, String q2, String q3, Pageable pageable);
}
