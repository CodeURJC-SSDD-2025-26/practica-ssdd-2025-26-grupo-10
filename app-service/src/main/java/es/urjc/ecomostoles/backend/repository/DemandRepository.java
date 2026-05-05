package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.DemandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Persistence interface for material demand broadcasts.
 * 
 * Implements complex query logic to identify "Smart Matches" across the B2B Material Graph. 
 * Orchestrates temporal validity filtering to exclude expired requests from public 
 * marketplace view-ports.
 */
public interface DemandRepository extends JpaRepository<Demand, Long> {

    /**
     * Find all demands published by a specific company.
     * 
     * @param company the company entity
     * @return a list of demands for that company
     */
    List<Demand> findByCompany(Company company);

    Page<Demand> findByCompany(Company company, Pageable pageable);

    Page<Demand> findByStatus(DemandStatus status, Pageable pageable);

    @Query("SELECT d FROM Demand d WHERE d.status = :status AND (d.expiryDate IS NULL OR d.expiryDate > :now)")
    Page<Demand> findActiveAndNotExpired(@Param("status") DemandStatus status, @Param("now") java.time.LocalDateTime now, Pageable pageable);

    long countByCompany(Company company);

    long countByCompanyAndStatus(Company company, DemandStatus status);
    
    long countByStatus(DemandStatus status);

    /**
     * Executes the "Smart Matching" algorithm looking for offers that satisfy existing 
     * tenant demands while excluding the tenant's own inventory to avoid self-matches.
     */
    @Query("SELECT o FROM es.urjc.ecomostoles.backend.model.Offer o JOIN FETCH o.company WHERE o.status = es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE AND o.company.id != :companyId AND o.wasteCategory IN (SELECT d.wasteCategory FROM Demand d WHERE d.company.id = :companyId AND d.status = es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE)")
    List<es.urjc.ecomostoles.backend.model.Offer> findOffersMatchingDemand(@Param("companyId") Long companyId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company WHERE d.status = :status")
    List<Demand> findByStatusJoinCompany(@Param("status") DemandStatus status);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company ORDER BY d.publicationDate DESC")
    Page<Demand> findAllPaginated(Pageable pageable);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company ORDER BY d.publicationDate DESC LIMIT 50")
    List<Demand> findTop50ByOrderByPublicationDateDesc();

    @Query("SELECT d FROM Demand d JOIN FETCH d.company WHERE d.status = :status AND " +
           "(:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Demand> searchFiltered(@Param("status") DemandStatus status, @Param("keyword") String keyword, Pageable pageable);
}
