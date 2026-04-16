package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.repository.AgreementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business Orchestrator for Commercial Agreements and environmental contracts.
 * 
 * Centralizes the transactional logic for finalized B2B material transfers. 
 * Manages complex state transitions, coordinates CO2 impact calculations via the 
 * Sustainability Engine, and enforces platform commission invariants. All operations 
 * are wrapped in @Transactional contexts to ensure ACID compliance during dual 
 * repository mutations.
 */
@Service
@Transactional
public class AgreementService {
    
    private static final Logger log = LoggerFactory.getLogger(AgreementService.class);

    private final AgreementRepository agreementRepository;
    private final CompanyService companyService;
    private final OfferService offerService;
    private final SustainabilityEngine sustainabilityEngine;
    private final ConfigurationService configurationService;

    public AgreementService(AgreementRepository agreementRepository,
            CompanyService companyService,
            OfferService offerService,
            SustainabilityEngine sustainabilityEngine,
            ConfigurationService configurationService) {
        this.agreementRepository = agreementRepository;
        this.companyService = companyService;
        this.offerService = offerService;
        this.sustainabilityEngine = sustainabilityEngine;
        this.configurationService = configurationService;
    }

    @Transactional(readOnly = true)
    public List<Agreement> getAll() {
        return agreementRepository.findTop50ByOrderByRegistrationDateDesc();
    }

    @Transactional(readOnly = true)
    public Page<Agreement> getAllPaginated(Pageable pageable) {
        return agreementRepository.findAllPaginated(pageable);
    }

    @Transactional(readOnly = true)
    public List<Agreement> getByCompany(Company company) {
        return agreementRepository.findByCompany(company);
    }

    @Transactional(readOnly = true)
    public List<Agreement> getByOriginCompany(Company company) {
        return agreementRepository.findByOriginCompany(company);
    }

    @Transactional(readOnly = true)
    public List<Agreement> getByDestinationCompany(Company company) {
        return agreementRepository.findByDestinationCompany(company);
    }

    @Transactional(readOnly = true)
    public Optional<Agreement> findById(Long id) {
        return agreementRepository.findById(id);
    }

    public Agreement save(Agreement agreement) {
        return agreementRepository.save(agreement);
    }

    public void delete(Long id) {
        Optional<Agreement> agreementOpt = agreementRepository.findById(id);
        if (agreementOpt.isPresent()) {
            Agreement agreement = agreementOpt.get();
            if (agreement.getOffer() != null) {
                Offer offer = agreement.getOffer();
                offer.setStatus(OfferStatus.ACTIVE);
                offerService.save(offer);
            }
            agreementRepository.deleteById(id);
            log.info("[Agreement] Success -> Removed agreement ID: {} and restored offer status.", id);
        } else {
            log.warn("[Agreement] Failed -> Attempted to delete non-existing agreement ID: {}", id);
        }
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return countAll(null);
    }

    @Transactional(readOnly = true)
    public long countAll(String filter) {
        if ("semana".equals(filter)) {
            return agreementRepository.countByRegistrationDateAfter(LocalDateTime.now().minusDays(7));
        }
        return agreementRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(es.urjc.ecomostoles.backend.model.AgreementStatus status) {
        return countByStatus(status, null);
    }

    @Transactional(readOnly = true)
    public long countByStatus(es.urjc.ecomostoles.backend.model.AgreementStatus status, String filter) {
        if ("semana".equals(filter)) {
            return agreementRepository.countByStatusAndRegistrationDateAfter(status, LocalDateTime.now().minusDays(7));
        }
        return agreementRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return agreementRepository.countByCompany(company);
    }

    /**
     * Executes the formal registration of a commercial contract.
     * 
     * Injects critical business events: 
     * 1. Mutates original Offer state to IN_NEGOTIATION to prevent double-booking.
     * 2. Triggers the Sustainability Engine to bind CO2 impact to the agreement record.
     * 3. Aggregates platform commissions based on dynamic Global Configuration tensors.
     * 
     * @param agreement The proposed agreement payload.
     * @param userEmail Principal email for identity resolution.
     * @param offerId Source offer identifier.
     * @param destinationCompanyId Counter-party company identifier.
     * @throws SelfAgreementException If a tenant attempts to contract their own inventory.
     */
    public void registerNewAgreement(Agreement agreement, String userEmail, Long offerId, Long destinationCompanyId) {
        Offer offer = offerService.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        Company originCompany = companyService.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (originCompany.getId().equals(destinationCompanyId)) {
            throw new SelfAgreementException("The origin and destination cannot be the same entity.");
        }

        if (!offer.getCompany().getId().equals(originCompany.getId())) {
            log.warn("[Agreement] Security -> User '{}' attempted to agreement unauthorized offer ID: {}", userEmail, offerId);
            throw new SelfAgreementException("The selected offer must belong to your company (you are the material owner).");
        }
        
        log.info("[Agreement] Processing -> Initiating new contract between '{}' and destination ID: {}", userEmail, destinationCompanyId);

        // --- NEW OFFER LIFECYCLE ---
        offer.setStatus(OfferStatus.IN_NEGOTIATION);
        offerService.save(offer);

        agreement.setOffer(offer);
        agreement.setExchangedMaterial(offer.getTitle() != null ? offer.getTitle() : "Material Acordado");
        agreement.setRegistrationDate(LocalDateTime.now());
        agreement.setStatus(AgreementStatus.PENDING);
        agreement.setOriginCompany(originCompany);

        // --- CALCULATE CO2 IMPACT ---
        double co2 = sustainabilityEngine.calculateCo2Impact(agreement.getQuantity(),
                offer.getWasteCategory());
        agreement.setCo2Impact(co2);
        // ------------------------------

        // --- CALCULATE COMISSION ---
        String platformCommission = configurationService.getAutoValue("platformCommission");
        double percentage = 0.0;
        try {
            percentage = Double.parseDouble(platformCommission);
        } catch (NumberFormatException e) {
            // Log fallback or handle error
        }

        if (agreement.getAgreedPrice() != null) {
            double commission = agreement.getAgreedPrice() * (percentage / 100.0);
            commission = Math.round(commission * 100.0) / 100.0;
            agreement.setPlatformCommission(commission);
        }
        // ---------------------------

        if (destinationCompanyId != null) {
            Company destinationCompany = companyService.findById(destinationCompanyId).orElse(originCompany);
            agreement.setDestinationCompany(destinationCompany);
        } else {
            agreement.setDestinationCompany(originCompany);
        }

        agreementRepository.save(agreement);
        log.info("[Agreement] Success -> New agreement created for material: '{}' (CO2 Impact: {} Kg)", agreement.getExchangedMaterial(), agreement.getCo2Impact());
    }

    @Transactional(readOnly = true)
    public double sumReintroducedMaterial(Company company) {
        Double total = agreementRepository.sumQuantityByCompanyAndStatus(company, AgreementStatus.COMPLETED);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double sumTotalReintroducedMaterial() {
        Double total = agreementRepository.sumTotalQuantityByStatus(AgreementStatus.COMPLETED);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double calculateCO2SavedByCompany(Long companyId) {
        Optional<Company> company = companyService.findById(companyId);
        if (company.isEmpty())
            return 0.0;

        Double total = agreementRepository.sumCO2ImpactByCompanyCompleted(company.get());
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public long countByCompanyAndStatus(Company company, AgreementStatus status) {
        return agreementRepository.countByCompanyAndStatus(company, status);
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> getCO2Ranking() {
        // Correctly filter by state in the query instead of in memory
        List<Agreement> completed = agreementRepository
                .findAllByStatus(es.urjc.ecomostoles.backend.model.AgreementStatus.COMPLETED);

        Map<Long, Double> ranking = new HashMap<>();
        for (Agreement a : completed) {
            double co2 = (a.getCo2Impact() != null) ? a.getCo2Impact() : 0.0;

            if (a.getOriginCompany() != null) {
                ranking.merge(a.getOriginCompany().getId(), co2, (v1, v2) -> v1 + v2);
            }
            if (a.getDestinationCompany() != null) {
                ranking.merge(a.getDestinationCompany().getId(), co2, (v1, v2) -> v1 + v2);
            }
        }
        return ranking;
    }

    @Transactional(readOnly = true)
    public Double getRawCO2Impact() {
        Double total = agreementRepository.sumTotalCO2ImpactCompleted();
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public Double calculateCO2Saved() {
        Double total = agreementRepository.sumTotalCO2ImpactCompleted();
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public Double getTotalCommission() {
        Double total = agreementRepository.sumTotalCommissionCompleted();
        return total != null ? total : 0.0;
    }

    /**
     * Updates an agreement and synchronizes linked asset states.
     * 
     * Implements "Side-Effect" logic where Agreement state transitions (ACCEPTED, COMPLETED)
     * are propagated back to the originating Offer status to maintain marketplace consistency.
     */
    public Agreement updateAgreement(Long id, Agreement updatedData) {
        Agreement existingAgreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agreement not found"));

        // SYNC OFFER STATUS BASED ON AGREEMENT STATE CHANGE
        if (existingAgreement.getOffer() != null && !updatedData.getStatus().equals(existingAgreement.getStatus())) {
            Offer offer = existingAgreement.getOffer();
            
            if (AgreementStatus.COMPLETED.equals(updatedData.getStatus())) {
                offer.setStatus(OfferStatus.FINISHED);
            } 
            else if (AgreementStatus.ACCEPTED.equals(updatedData.getStatus())) {
                offer.setStatus(OfferStatus.RESERVED);
            } 
            else if (AgreementStatus.REJECTED.equals(updatedData.getStatus())) {
                offer.setStatus(OfferStatus.ACTIVE);
            }
            // If it goes back to PENDING for some reason, we could set it to IN_NEGOTIATION
            else if (AgreementStatus.PENDING.equals(updatedData.getStatus())) {
                 offer.setStatus(OfferStatus.IN_NEGOTIATION);
            }

            offerService.save(offer);
        }

        // If transitioning to COMPLETED, freeze the CO2 impact
        if (AgreementStatus.COMPLETED.equals(updatedData.getStatus()) &&
                !AgreementStatus.COMPLETED.equals(existingAgreement.getStatus())) {

            double co2 = sustainabilityEngine.calculateCo2Impact(updatedData.getQuantity(),
                    existingAgreement.getOffer() != null ? existingAgreement.getOffer().getWasteCategory() : null);
            existingAgreement.setCo2Impact(co2);
        }

        // Update allowed fields
        existingAgreement.setExchangedMaterial(updatedData.getExchangedMaterial());
        existingAgreement.setQuantity(updatedData.getQuantity());
        existingAgreement.setUnit(updatedData.getUnit());
        existingAgreement.setPickupDate(updatedData.getPickupDate());
        existingAgreement.setStatus(updatedData.getStatus());

        // Recalculate profit if price changed
        if (updatedData.getAgreedPrice() != null &&
                !updatedData.getAgreedPrice().equals(existingAgreement.getAgreedPrice())) {

            existingAgreement.setAgreedPrice(updatedData.getAgreedPrice());

            String platformCommission = configurationService.getAutoValue("platformCommission");
            double percentage = 0.0;
            try {
                percentage = Double.parseDouble(platformCommission);
            } catch (NumberFormatException e) {
                // Ignore or log
            }

            double commission = existingAgreement.getAgreedPrice() * (percentage / 100.0);
            commission = Math.round(commission * 100.0) / 100.0;
            existingAgreement.setPlatformCommission(commission);
        } else {
            existingAgreement.setAgreedPrice(updatedData.getAgreedPrice());
        }

        Agreement saved = agreementRepository.save(existingAgreement);
        log.info("[Agreement] Success -> Updated agreement ID: {} (New Status: {})", id, saved.getStatus());
        return saved;
    }
}
