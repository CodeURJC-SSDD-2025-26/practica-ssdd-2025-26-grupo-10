package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.MatchResultDTO;
import es.urjc.ecomostoles.backend.mapper.OfferMapper;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import es.urjc.ecomostoles.backend.repository.DemandRepository;
import es.urjc.ecomostoles.backend.repository.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Heuristics Engine for Smart B2B Material Matching.
 * 
 * Computes multi-dimensional compatibility scores (Waste Taxonomy, Volumetric Variance, 
 * Market Resonance) to connect tenant demands with optimal market liquidity.
 */
@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final DemandRepository demandRepository;
    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;

    public MatchingService(DemandRepository demandRepository, OfferRepository offerRepository, OfferMapper offerMapper) {
        this.demandRepository = demandRepository;
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
    }

    /**
     * Executes the matching algorithm for a specific demand, returning the top 5 results.
     */
    public List<MatchResultDTO> findBestMatchesForDemand(Long demandId) {
        log.debug("[Matching] Initializing algorithm for demand ID: {}", demandId);

        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new NoSuchElementException("Demand not found"));

        List<Offer> candidateOffers = offerRepository.findByWasteCategoryAndStatus(
                demand.getWasteCategory(), OfferStatus.ACTIVE);

        List<MatchResultDTO> results = candidateOffers.stream()
                // Security Constraint: Prevent self-dealing
                .filter(offer -> !offer.getCompany().getId().equals(demand.getCompany().getId()))
                .map(offer -> calculateMatchScore(demand, offer))
                .sorted(Comparator.comparingDouble(MatchResultDTO::matchScore).reversed())
                .limit(5)
                .toList();

        log.info("[Matching] Computed {} optimal matches for demand ID: {}", results.size(), demandId);
        return results;
    }

    /**
     * Core Mathematical Model for Compatibility Scoring.
     */
    private MatchResultDTO calculateMatchScore(Demand demand, Offer offer) {
        double score = 50.0; // Base score for exact taxonomy match
        StringBuilder reason = new StringBuilder("Coincidencia en categoría (50 pts)");

        // 1. Volumetric Proximity Vector (Max 30 points)
        if (demand.getQuantity() != null && demand.getQuantity() > 0 && offer.getQuantity() != null) {
            double diffRatio = Math.abs(offer.getQuantity() - demand.getQuantity()) / demand.getQuantity();
            if (diffRatio < 1.0) { // If variance is less than 100%
                double qtyScore = 30.0 * (1.0 - diffRatio);
                score += qtyScore;
                reason.append(String.format(" | Volumen similar: +%.1f pts", qtyScore));
            }
        }

        // 2. Market Resonance / Popularity Vector (Max 20 points)
        // Approximating liquidity based on accumulated view metrics
        double popularityScore = Math.min(20.0, offer.getVisits() * 2.0);
        score += popularityScore;
        if (popularityScore > 0) {
            reason.append(String.format(" | Oferta destacada: +%.1f pts", popularityScore));
        }

        // Normalize to one decimal place
        score = Math.round(score * 10.0) / 10.0;

        return new MatchResultDTO(offerMapper.toDto(offer), score, reason.toString());
    }
}
