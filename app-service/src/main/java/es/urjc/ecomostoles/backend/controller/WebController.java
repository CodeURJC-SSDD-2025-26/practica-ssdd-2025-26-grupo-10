package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.utils.NumberFormatter;
import java.util.List;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

/**
 * Core perimeter controller managing unauthenticated routing footprints.
 * 
 * Hosts the initial marketing funnels, legal disclosure pages, and resolves routing
 * redirects pushing authenticated organic traffic seamlessly towards their segregated Dashboards.
 */
@Controller
public class WebController {

    private final OfferService offerService;
    private final CompanyService companyService;
    private final AgreementService agreementService;

    public WebController(OfferService offerService,
            CompanyService companyService,
            AgreementService agreementService) {
        this.offerService = offerService;
        this.companyService = companyService;
        this.agreementService = agreementService;
    }

    /**
     * Resolves the primary perimeter landing view, populating dynamic platform KPIs.
     * 
     * @param model MVC data dictionary targeting the "index" template.
     * @return resolved path linking to the front-facing sales facade.
     */
    @GetMapping("/")
    public String index(Model model) {
        List<OfferSummary> recentOffers = offerService.getActiveRecent();
        model.addAttribute("recentOffers", recentOffers);

        // Injects real metrics from the database
        model.addAttribute("totalCompanies", companyService.countAll());
        model.addAttribute("totalOffers", offerService.countAll());
        model.addAttribute("totalCo2", NumberFormatter.format(agreementService.calculateCO2Saved()));
        model.addAttribute("isHome", true);

        return "index";
    }

    @GetMapping("/terminos")
    public String terms() {
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacy() {
        return "privacidad";
    }

    /**
     * Gateway router capturing organic authentication transitions.
     * 
     * Applies defensive fallback evaluating role structures via the raw servlet request, 
     * bouncing active traces precisely towards their highest clearance landing zones.
     * 
     * @param auth active security principal state container.
     * @param request base servlet footprint mapping the protocol execution.
     * @return 302 Redirection directive towards restricted panels.
     */
    @GetMapping("/home")
    public String homeRouter(Authentication auth, HttpServletRequest request) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/";
        }
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }
}
