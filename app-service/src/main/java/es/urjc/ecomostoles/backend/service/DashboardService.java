package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.DashboardStatsDTO;
import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.stereotype.Service;
import es.urjc.ecomostoles.backend.mapper.OfferMapper;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Intelligence Aggregator for the Platform Dashboard.
 * 
 * Orchestrates multi-dimensional data retrieval across the service ecosystem. 
 * Implements role-segregated KPI logic:
 * - ADMIN: Global platform trajectory (Total impact, total revenue).
 * - COMPANY: Individual performance metrics and Smart Recommendation discovery.
 */
@Service
public class DashboardService {

    private final OfferService offerService;
    private final DemandService demandService;
    private final AgreementService agreementService;
    private final OfferMapper offerMapper;

    public DashboardService(OfferService offerService,
            DemandService demandService,
            AgreementService agreementService,
            OfferMapper offerMapper) {
        this.offerService = offerService;
        this.demandService = demandService;
        this.agreementService = agreementService;
        this.offerMapper = offerMapper;
    }

    /**
     * Consolidates platform-wide or tenant-specific analytical indices.
     * 
     * Dynamically branches query vectors based on Principal roles to ensure 
     * security-compliant statistical snapshots. Populates the unified DashboardStatsDTO 
     * schema for presentation.
     * 
     * @param company The Subject for whom indices are being aggregated.
     * @return A consolidated payload for UI rendering.
     */
    public DashboardStatsDTO getStats(Company company) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        boolean isAdmin = company.getRoles() != null && company.getRoles().contains("ADMIN");
        stats.setAdmin(isAdmin);

        if (isAdmin) {
            // ── Admin: Global KPIs ──────────────────────────────────────────
            int totalOffers = (int) offerService.countAll();
            int totalDemands = (int) demandService.countAll();
            int activeAgreements = (int) agreementService.countAll();

            stats.setTotalOffers(totalOffers);
            stats.setTotalDemands(totalDemands);
            stats.setActiveAgreements(activeAgreements);

            stats.setChartData(List.of(totalOffers, totalDemands, activeAgreements));

            // Admin Global Impact Stats
            stats.setReintroducedMaterial(agreementService.sumTotalReintroducedMaterial());
            stats.setCo2Impact(agreementService.getRawCO2Impact());
        } else {
            // ── Company: Personal KPIs ──────────────────────────────────────
            int myOffers = (int) offerService.countByCompany(company);
            int myDemands = (int) demandService.countByCompany(company);
            int myAgreements = (int) agreementService.countByCompany(company);

            stats.setTotalOffers(myOffers);
            stats.setTotalDemands(myDemands);
            stats.setActiveAgreements(myAgreements);

            double reintroduced = agreementService.sumReintroducedMaterial(company);
            stats.setReintroducedMaterial(reintroduced);
            stats.setCo2Impact(agreementService.calculateCO2SavedByCompany(company.getId()));

            // Smart Matching
            List<?> recommendedOffers = demandService.getSmartRecommendations(company).stream()
                    .map(offerMapper::toDto)
                    .collect(Collectors.toList());
            stats.setSmartRecommendations(recommendedOffers);
            stats.setHasRecommendations(!recommendedOffers.isEmpty());

            stats.setChartData(List.of(myOffers, myDemands, myAgreements));
        }

        return stats;
    }
}
