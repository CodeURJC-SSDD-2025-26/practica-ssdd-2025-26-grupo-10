package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Repository interface for Offer entity.
 */
public interface OfferRepository extends JpaRepository<Offer, Long> {

        @Modifying
        @Query("UPDATE Offer o SET o.visits = o.visits + 1 WHERE o.id = :id")
        void incrementVisits(@Param("id") Long id);

        long countByStatus(OfferStatus status);

        /** Paginated retrieval of all offers with projection */
        <T> Page<T> findAllProjectedBy(Pageable pageable, Class<T> type);

        /** Paginated retrieval of offers by state with projection */
        <T> Page<T> findByStatus(OfferStatus status, Pageable pageable, Class<T> type);

        /**
         * Finds a paginated list of offers associated with a specific company.
         */
        <T> Page<T> findByCompany(Company company, Pageable pageable, Class<T> type);

        /**
         * Finds a list of offers associated with a specific company.
         */
        <T> List<T> findByCompany(Company company, Class<T> type);

        /** Finds offers of a company with a specific status. */
        <T> List<T> findByCompanyAndStatus(Company company, OfferStatus status, Class<T> type);

        long countByCompany(Company company);

        @Query("SELECT o FROM Offer o JOIN FETCH o.company WHERE o.status = :status ORDER BY o.publicationDate DESC LIMIT 3")
        <T> List<T> findTop3ByStatusOrderByPublicationDateDesc(@Param("status") OfferStatus status, Class<T> type);

        @Query("SELECT o FROM Offer o JOIN FETCH o.company WHERE o.status = :status")
        <T> List<T> findByStatusJoinCompany(@Param("status") OfferStatus status, Class<T> type);

        @Query("SELECT o FROM Offer o JOIN FETCH o.company ORDER BY o.publicationDate DESC LIMIT 50")
        <T> List<T> findTop50ByOrderByPublicationDateDesc(Class<T> type);

        @Query("SELECT o FROM Offer o JOIN FETCH o.company " +
                        "WHERE o.status = :status " +
                        "AND (:kw IS NULL OR :kw = '' OR LOWER(o.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(CAST(o.description AS string)) LIKE LOWER(CONCAT('%', :kw, '%'))) "
                        +
                        "AND (:type IS NULL OR o.wasteCategory = :type) " +
                        "AND (:industrialPark IS NULL OR :industrialPark = '' OR LOWER(o.company.address) LIKE LOWER(CONCAT('%', :industrialPark, '%')))")
        Page<OfferSummary> searchFiltered(@Param("status") OfferStatus status, @Param("kw") String kw,
                        @Param("type") es.urjc.ecomostoles.backend.model.WasteCategory type, @Param("industrialPark") String industrialPark, Pageable pageable);

        @Query("SELECT o FROM Offer o JOIN FETCH o.company WHERE o.status = :status AND o.company.id != :companyId AND o.wasteCategory IN :categories")
        List<Offer> findSmartMatches(@Param("status") OfferStatus status, @Param("companyId") Long companyId,
                        @Param("categories") List<es.urjc.ecomostoles.backend.model.WasteCategory> categories, Pageable pageable);

        /** Finds active offers NOT belonging to the given company using an explicit query to avoid derivation issues. */
        @Query("SELECT o FROM Offer o WHERE o.status = :status AND o.company != :company")
        <T> List<T> findAllExternalOffers(@Param("status") OfferStatus status, @Param("company") Company company, Class<T> type);
}
