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
 * Controller for handling the Dashboard view.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all domain data access to the four services.
 * Now uses MessageService instead of direct repository access.
 *
 * - ADMIN: sees global KPIs for the entire platform.
 * - COMPANY: sees its own KPIs (offers, demands, agreements, messages).
 */
@Controller
public class DashboardController {

    private final CompanyService companyService;
    private final DashboardService dashboardService;

    public DashboardController(CompanyService companyService, DashboardService dashboardService) {
        this.companyService = companyService;
        this.dashboardService = dashboardService;
    }

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
            model.addAttribute("esAdmin", stats.isAdmin());
            model.addAttribute("totalOfertas", stats.getTotalOffers());
            model.addAttribute("totalDemandas", stats.getTotalDemands());
            model.addAttribute("acuerdosActivos", stats.getActiveAgreements());
            model.addAttribute("chartData", stats.getChartData());
            model.addAttribute("materialReintroducido", stats.getReintroducedMaterial());
            model.addAttribute("impactoCO2", stats.getCo2Impact());
            model.addAttribute("smartRecommendations", stats.getSmartRecommendations());
            model.addAttribute("hasRecommendations", stats.isHasRecommendations());

            return "dashboard";
        }

        return "redirect:/";
    }
}
