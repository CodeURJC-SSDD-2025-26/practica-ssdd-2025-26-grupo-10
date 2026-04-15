package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.DashboardStatsDTO;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.stereotype.Service;
import java.util.List;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;

/**
 * Service to handle business logic for the Dashboard.
 * Extracts logic from the controller to follow the Controller-Service pattern.
 */
@Service
public class DashboardService {

    private final OfferService offerService;
    private final DemandService demandService;
    private final AgreementService agreementService;
    private final SustainabilityEngine sustainabilityEngine;

    public DashboardService(OfferService offerService,
            DemandService demandService,
            AgreementService agreementService,
            SustainabilityEngine sustainabilityEngine) {
        this.offerService = offerService;
        this.demandService = demandService;
        this.agreementService = agreementService;
        this.sustainabilityEngine = sustainabilityEngine;
    }

    /**
     * Calculates all KPIs, charts data and recommendations for a given company (or
     * admin).
     * 
     * @param company The company for which stats are calculated.
     * @return A DTO containing all dashboard attributes.
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
            List<?> recommendedOffers = demandService.getSmartRecommendations(company);
            stats.setSmartRecommendations(recommendedOffers);
            stats.setHasRecommendations(!recommendedOffers.isEmpty());

            stats.setChartData(List.of(myOffers, myDemands, myAgreements));
        }

        return stats;
    }
}
