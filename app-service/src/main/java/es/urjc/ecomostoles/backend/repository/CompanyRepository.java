package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for tenant identity records.
 * 
 * Manages the canonical repository of authenticated Companies. Enforces unique constraint 
 * lookup patterns for multi-tenant isolation and credentials verification during the 
 * Spring Security authentication handshake.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByContactEmail(String contactEmail);

    Optional<Company> findByCommercialName(String commercialName);

    /**
     * Search by tax Id (unique field): prevents DataIntegrityViolationException during
     * upsert.
     */
    Optional<Company> findByTaxId(String taxId);

    List<Company> findTop50ByOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Company c WHERE 'ADMIN' NOT MEMBER OF c.roles")
    Page<Company> findAllClients(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Company c WHERE 'ADMIN' NOT MEMBER OF c.roles AND (LOWER(c.commercialName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.taxId) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Company> searchClients(String q, Pageable pageable);

    Page<Company> findByCommercialNameContainingIgnoreCaseOrContactEmailContainingIgnoreCaseOrTaxIdContainingIgnoreCase(
            String q1, String q2, String q3, Pageable pageable);
}
