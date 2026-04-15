package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.dto.DashboardStatsDTO;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.DashboardService;
import es.urjc.ecomostoles.backend.service.CompanyService;

/**
 * Controller positioning the main authenticated landing area (Dashboard).
 * 
 * Functions as an aggregator facade, orchestrating cross-domain data fetching 
 * (Offers, Demands, Sustainability milestones), and packing it into a cohesive DashboardStatsDTO.
 * Abstracts heavy DB computations behind the DashboardService to keep presentation layer thin.
 */
@Controller
public class DashboardController {

    private final CompanyService companyService;
    private final DashboardService dashboardService;

    public DashboardController(CompanyService companyService, DashboardService dashboardService) {
        this.companyService = companyService;
        this.dashboardService = dashboardService;
    }

    /**
     * Renders the unified operational dashboard tailored exclusively to the connected company.
     * 
     * @param model the template payload carrier context.
     * @param principal represents the current authorized security identity.
     * @return routing string mapping towards the corresponding Mustache view.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeDashboard", true);
            model.addAttribute("isDashboard", true);

            // Strong Typing: Receiving DTO from Service
            DashboardStatsDTO stats = dashboardService.getStats(company);

            // Injecting properties manually to keep the Mustache template flat as expected
            // Statistics and KPIs
            model.addAttribute("totalOffers", stats.getTotalOffers());
            model.addAttribute("totalDemands", stats.getTotalDemands());
            model.addAttribute("totalActiveAgreements", stats.getActiveAgreements());
            model.addAttribute("chartData", stats.getChartData());
            model.addAttribute("reintroducedMaterial", String.format(java.util.Locale.US, "%.2f", stats.getReintroducedMaterial()));
            model.addAttribute("co2Impact", String.format(java.util.Locale.US, "%.2f", stats.getCo2Impact()));

            // Smart Matching logic
            model.addAttribute("userIsAdmin", stats.isAdmin());
            model.addAttribute("matchingOffers", stats.getSmartRecommendations());
            model.addAttribute("showMatching", stats.isHasRecommendations());

            return "dashboard";
        }

        return "redirect:/";
    }
}
