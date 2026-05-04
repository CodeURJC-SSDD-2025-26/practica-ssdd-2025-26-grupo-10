package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marketplace Facilitator governing material provision lifecycles.
 * 
 * Orchestrates the high-throughput discovery ecosystem. Leverages lightweight 
 * DTO projections (OfferSummary) for performant list rendering. Centralizes 
 * complex multi-criteria material search algorithms and atomic visit auditing.
 */
@Service
@Transactional
public class OfferService {
    
    private static final Logger log = LoggerFactory.getLogger(OfferService.class);

    private final OfferRepository offerRepository;
    private final es.urjc.ecomostoles.backend.repository.AgreementRepository agreementRepository;

    public OfferService(OfferRepository offerRepository,
            es.urjc.ecomostoles.backend.repository.AgreementRepository agreementRepository) {
        this.offerRepository = offerRepository;
        this.agreementRepository = agreementRepository;
    }

    /** Returns all offers in the system as projections. */
    @Transactional(readOnly = true)
    public List<OfferSummary> getAll() {
        return offerRepository.findTop50ByOrderByPublicationDateDesc(OfferSummary.class);
    }

    /** Returns all offers in the system with pagination (Projected). */
    @Transactional(readOnly = true)
    public Page<OfferSummary> getAllPaginated(Pageable pageable) {
        return offerRepository.findAllProjectedBy(pageable, OfferSummary.class);
    }

    /** Returns all offers filtered by state (Projected) */
    @Transactional(readOnly = true)
    public List<OfferSummary> getByStatus(es.urjc.ecomostoles.backend.model.OfferStatus status) {
        return offerRepository.findByStatusJoinCompany(status, OfferSummary.class);
    }

    /** Returns all offers filtered by state with pagination (Projected). */
    @Transactional(readOnly = true)
    public Page<OfferSummary> getByStatusPaginated(es.urjc.ecomostoles.backend.model.OfferStatus status,
            Pageable pageable) {
        return offerRepository.findByStatus(status, pageable, OfferSummary.class);
    }

    /** Returns top 3 recent active offers directly from DB (Projected) */
    @Transactional(readOnly = true)
    public List<OfferSummary> getActiveRecent() {
        return offerRepository.findTop3ByStatusOrderByPublicationDateDesc(
                es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE, OfferSummary.class);
    }

    /** Returns all offers belonging to a specific company (Projected). */
    @Transactional(readOnly = true)
    public List<OfferSummary> getByCompany(Company company) {
        return offerRepository.findByCompany(company, OfferSummary.class);
    }

    /**
     * Returns all offers belonging to a specific company with pagination
     * (Projected).
     */
    @Transactional(readOnly = true)
    public Page<OfferSummary> getByCompanyPaginated(Company company, Pageable pageable) {
        return offerRepository.findByCompany(company, pageable, OfferSummary.class);
    }

    /** Returns all ACTIVE offers belonging to a specific company. */
    public List<OfferSummary> getActiveByCompany(Company company) {
        return offerRepository.findByCompanyAndStatus(company,
                es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE, OfferSummary.class);
    }

    /** Optimized count (does not fetch entire list) */
    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return offerRepository.countByCompany(company);
    }

    /** Returns an offer by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Offer> findById(Long id) {
        return offerRepository.findById(id);
    }

    /** Persists a new or updated offer. */
    public Offer save(Offer offer) {
        Offer saved = offerRepository.save(offer);
        log.info("[Marketplace] Success -> Persisted offer ID: '{}' (Title: '{}', Status: {})", saved.getId(), saved.getTitle(), saved.getStatus());
        return saved;
    }

    /**
     * Executes the secure deletion of a material asset.
     * 
     * Asserts that no active commercial agreements (contracts) are pinned to this 
     * record before delegating to the persistence layer, ensuring data structure 
     * integrity across the platform.
     * 
     * @param id Identifier of the offer to be pruned.
     * @throws ResponseStatusException 400 if contract invariants are violated.
     */
    public void delete(Long id) {
        if (agreementRepository.countByOfferId(id) > 0) {
            log.warn("[Marketplace] Denied -> Attempted to delete protected offer ID: {} (Active agreements found)", id);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Cannot be deleted because it has associated agreements");
        }
        offerRepository.deleteById(id);
        log.info("[Marketplace] Success -> Removed offer ID: {} from system.", id);
    }

    /** Returns the total count of offers. */
    @Transactional(readOnly = true)
    public long countAll() {
        return offerRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(es.urjc.ecomostoles.backend.model.OfferStatus status) {
        return offerRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<OfferSummary> searchFilteredOffers(String keyword,
            es.urjc.ecomostoles.backend.model.WasteCategory wasteType, String industrialPark,
            Pageable pageable) {
        return offerRepository.searchFiltered(es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE, keyword,
                wasteType, industrialPark, pageable);
    }

    /** Atomic increment of visits */
    @Transactional
    public void registerVisit(Long id) {
        offerRepository.incrementVisits(id);
    }

    /** Returns all active offers EXCEPT those belonging to the specified company. */
    @Transactional(readOnly = true)
    public List<OfferSummary> getActiveByOtherCompanies(Company company) {
        return offerRepository.findAllExternalOffers(
                es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE, company, OfferSummary.class);
    }
}
