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
 * Repository interface for Demand entity.
 * Handles database operations for requests made by companies.
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

    long countByCompany(Company company);

    long countByCompanyAndStatus(Company company, DemandStatus status);

    @Query("SELECT o FROM es.urjc.ecomostoles.backend.model.Offer o JOIN FETCH o.company WHERE o.status = es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE AND o.company.id != :companyId AND o.wasteType IN (SELECT d.wasteType FROM Demand d WHERE d.company.id = :companyId AND d.status = es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE)")
    List<es.urjc.ecomostoles.backend.model.Offer> findOffersMatchingDemand(@Param("companyId") Long companyId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company WHERE d.status = :status")
    List<Demand> findByStatusJoinCompany(@Param("status") DemandStatus status);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company ORDER BY d.registrationDate DESC")
    Page<Demand> findAllPaginated(Pageable pageable);

    @Query("SELECT d FROM Demand d JOIN FETCH d.company ORDER BY d.registrationDate DESC LIMIT 50")
    List<Demand> findTop50ByOrderByRegistrationDateDesc();
}
