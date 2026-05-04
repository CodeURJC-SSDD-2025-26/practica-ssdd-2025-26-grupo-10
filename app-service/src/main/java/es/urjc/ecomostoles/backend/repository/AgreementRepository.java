package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Persistence gateway for Agreement domain assets.
 * 
 * Orchestrates localized database transactions via Spring Data JPA. Leverages advanced 
 * JPQL "JOIN FETCH" optimizations to mitigate N+1 retrieval anomalies during high-concurrency 
 * dashboard reconciliation and marketplace audit sweeps.
 */
public interface AgreementRepository extends JpaRepository<Agreement, Long> {

        /**
         * Counts agreements by their status.
         * 
         * @param status the status to count
         * @return count of agreements
         */
        long countByStatus(AgreementStatus status);

        /**
         * Retrieves a list of agreements where the given company is the originator
         * (source).
         *
         * @param originCompany the source company
         * @return a list of agreements
         */
        @org.springframework.data.jpa.repository.Query("SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany LEFT JOIN FETCH a.offer WHERE a.originCompany = :originCompany")
        List<Agreement> findByOriginCompany(
                        @org.springframework.data.repository.query.Param("originCompany") Company originCompany);

        /**
         * Retrieves a list of agreements where the given company is the recipient
         * (destination).
         *
         * @param destinationCompany the destination company
         * @return a list of agreements
         */
        @org.springframework.data.jpa.repository.Query("SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany LEFT JOIN FETCH a.offer WHERE a.destinationCompany = :destinationCompany")
        List<Agreement> findByDestinationCompany(
                        @org.springframework.data.repository.query.Param("destinationCompany") Company destinationCompany);

        /**
         * Retrieves a list of agreements where the given company is involved as source
         * or destination.
         *
         * @param company the involved company
         * @return a list of agreements
         */
        @org.springframework.data.jpa.repository.Query("SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany LEFT JOIN FETCH a.offer LEFT JOIN FETCH a.demand WHERE a.originCompany = :company OR a.destinationCompany = :company")
        List<Agreement> findByCompany(@org.springframework.data.repository.query.Param("company") Company company);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Agreement a WHERE a.originCompany = :company OR a.destinationCompany = :company")
        long countByCompany(@org.springframework.data.repository.query.Param("company") Company company);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(a.quantity) FROM Agreement a WHERE (a.originCompany = :company OR a.destinationCompany = :company) AND a.status = :status")
        Double sumQuantityByCompanyAndStatus(
                        @org.springframework.data.repository.query.Param("company") Company company,
                        @org.springframework.data.repository.query.Param("status") AgreementStatus status);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Agreement a WHERE (a.originCompany = :company OR a.destinationCompany = :company) AND a.status = :status")
        long countByCompanyAndStatus(@org.springframework.data.repository.query.Param("company") Company company,
                        @org.springframework.data.repository.query.Param("status") es.urjc.ecomostoles.backend.model.AgreementStatus status);

        @org.springframework.data.jpa.repository.Query(value = "SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany", countQuery = "SELECT COUNT(a) FROM Agreement a")
        Page<Agreement> findAllPaginated(Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany ORDER BY a.registrationDate DESC LIMIT 50")
        List<Agreement> findTop50ByOrderByRegistrationDateDesc();

        @org.springframework.data.jpa.repository.Query("SELECT a FROM Agreement a JOIN FETCH a.originCompany JOIN FETCH a.destinationCompany LEFT JOIN FETCH a.offer WHERE a.status = :status")
        List<Agreement> findAllByStatus(
                        @org.springframework.data.repository.query.Param("status") AgreementStatus status);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(a.quantity) FROM Agreement a WHERE a.status = :status")
        Double sumTotalQuantityByStatus(
                        @org.springframework.data.repository.query.Param("status") AgreementStatus status);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(a.co2Impact) FROM Agreement a WHERE a.status = 'COMPLETED'")
        Double sumTotalCO2ImpactCompleted();

        @org.springframework.data.jpa.repository.Query("SELECT SUM(a.co2Impact) FROM Agreement a WHERE (a.originCompany = :company OR a.destinationCompany = :company) AND a.status = 'COMPLETED'")
        Double sumCO2ImpactByCompanyCompleted(
                        @org.springframework.data.repository.query.Param("company") Company company);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(a.platformCommission) FROM Agreement a WHERE a.status = 'COMPLETED'")
        Double sumTotalCommissionCompleted();

        long countByRegistrationDateAfter(java.time.LocalDateTime date);

        long countByStatusAndRegistrationDateAfter(AgreementStatus status, java.time.LocalDateTime date);

        long countByOfferId(Long id);

        long countByDemandId(Long id);
}
