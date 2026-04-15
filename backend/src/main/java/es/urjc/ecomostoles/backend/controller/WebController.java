package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import java.util.List;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

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

    @GetMapping("/")
    public String index(Model model) {
        List<OfferSummary> recentOffers = offerService.getActiveRecent();
        model.addAttribute("recentOffers", recentOffers);

        // Injects real metrics from the database
        model.addAttribute("totalCompanies", companyService.countAll());
        model.addAttribute("totalOffers", offerService.countAll());
        model.addAttribute("totalCo2", agreementService.calculateCO2Saved());
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
