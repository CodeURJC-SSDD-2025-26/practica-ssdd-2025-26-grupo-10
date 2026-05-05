package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.repository.DemandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business Manager for material demand lifecycles.
 * 
 * Orchestrates the lifecycle of Tenant-published requests. Governs matching 
 * discovery logic and enforces relational integrity constraints to prevent 
 * premature deletion of records linked to active commercial agreements.
 */
@Service
@Transactional
public class DemandService {
    
    private static final Logger log = LoggerFactory.getLogger(DemandService.class);

    private final DemandRepository demandRepository;
    private final es.urjc.ecomostoles.backend.repository.AgreementRepository agreementRepository;

    public DemandService(DemandRepository demandRepository,
            es.urjc.ecomostoles.backend.repository.AgreementRepository agreementRepository) {
        this.demandRepository = demandRepository;
        this.agreementRepository = agreementRepository;
    }

    /** Returns all demands in the system. */
    @Transactional(readOnly = true)
    public List<Demand> getAll() {
        return demandRepository.findTop50ByOrderByPublicationDateDesc();
    }

    /** Returns all demands in the system with pagination. */
    @Transactional(readOnly = true)
    public Page<Demand> getAllPaginated(Pageable pageable) {
        return demandRepository.findAllPaginated(pageable);
    }

    /** Returns all demands filtered by state */
    @Transactional(readOnly = true)
    public List<Demand> getByStatus(es.urjc.ecomostoles.backend.model.DemandStatus status) {
        return demandRepository.findByStatusJoinCompany(status);
    }

    /** Returns all demands filtered by state with pagination. */
    @Transactional(readOnly = true)
    public Page<Demand> getByStatusPaginated(es.urjc.ecomostoles.backend.model.DemandStatus status,
            Pageable pageable) {
        return demandRepository.findActiveAndNotExpired(status, java.time.LocalDateTime.now(), pageable);
    }

    /**
     * Returns top 3 smart offer recommendations for this company based on their
     * demands
     */
    @Transactional(readOnly = true)
    public List<es.urjc.ecomostoles.backend.model.Offer> getSmartRecommendations(Company company) {
        return demandRepository.findOffersMatchingDemand(
                company.getId(),
                org.springframework.data.domain.PageRequest.of(0, 3));
    }

    /** Returns all demands belonging to a specific company. */
    @Transactional(readOnly = true)
    public List<Demand> getByCompany(Company company) {
        return demandRepository.findByCompany(company);
    }

    /** Returns all demands belonging to a specific company with pagination. */
    @Transactional(readOnly = true)
    public Page<Demand> getByCompanyPaginated(Company company, Pageable pageable) {
        return demandRepository.findByCompany(company, pageable);
    }

    /** Optimized count (does not fetch entire list) */
    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return demandRepository.countByCompany(company);
    }

    @Transactional(readOnly = true)
    public long countActiveByCompany(Company company) {
        return demandRepository.countByCompanyAndStatus(company,
                es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countPausedByCompany(Company company) {
        return demandRepository.countByCompanyAndStatus(company,
                es.urjc.ecomostoles.backend.model.DemandStatus.PAUSED);
    }

    @Transactional(readOnly = true)
    public long countClosedByCompany(Company company) {
        return demandRepository.countByCompanyAndStatus(company,
                es.urjc.ecomostoles.backend.model.DemandStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public long sumVisitsByCompany(Company company) {
        List<Demand> demands = demandRepository.findByCompany(company);
        return demands.stream().mapToLong(Demand::getVisits).sum();
    }

    /** Returns a demand by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Demand> findById(Long id) {
        return demandRepository.findById(id);
    }

    /** Persists a new or updated demand. */
    public Demand save(Demand demand) {
        Demand saved = demandRepository.save(demand);
        log.info("[Marketplace] Success -> Persisted demand ID: '{}' (Title: '{}', Status: {})", saved.getId(), saved.getTitle(), saved.getStatus());
        return saved;
    }

    /**
     * Executes the secure deletion of a material demand.
     * 
     * Asserts that no active agreements depend on this record (FK constraint check) 
     * before delegating to the persistence layer.
     * 
     * @param id Identifier of the demand to be pruned.
     * @throws ResponseStatusException 400 if relational invariants are violated.
     */
    public void delete(Long id) {
        if (agreementRepository.countByDemandId(id) > 0) {
            log.warn("[Marketplace] Denied -> Attempted to delete protected demand ID: {} (Active agreements found)", id);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Cannot be deleted because it has associated agreements");
        }
        demandRepository.deleteById(id);
        log.info("[Marketplace] Success -> Removed demand ID: {} from system.", id);
    }

    /** Increments the visit counter for a demand. */
    public void registerVisit(Long id) {
        demandRepository.findById(id).ifPresent(demand -> {
            demand.setVisits(demand.getVisits() + 1);
            demandRepository.save(demand);
        });
    }

    /** Returns the total count of demands. */
    @Transactional(readOnly = true)
    public long countAll() {
        return demandRepository.count();
    }

    /** Returns count of demands by status. */
    @Transactional(readOnly = true)
    public long countByStatus(es.urjc.ecomostoles.backend.model.DemandStatus status) {
        return demandRepository.countByStatus(status);
    }

    /** Returns paginated demands matching the keyword, filtered by status */
    @Transactional(readOnly = true)
    public Page<Demand> searchFilteredDemands(String keyword, es.urjc.ecomostoles.backend.model.DemandStatus status, Pageable pageable) {
        return demandRepository.searchFiltered(status, keyword, pageable);
    }
}
