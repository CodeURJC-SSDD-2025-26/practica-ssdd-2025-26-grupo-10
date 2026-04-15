package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.MessageService;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CompanyService companyService;
    private final MessageService messageService;
    private final ConfigurationService configurationService;

    public GlobalControllerAdvice(CompanyService companyService, MessageService messageService,
            ConfigurationService configurationService) {
        this.companyService = companyService;
        this.messageService = messageService;
        this.configurationService = configurationService;
    }

    @ModelAttribute("company")
    public es.urjc.ecomostoles.backend.dto.CompanyDTO getCompany(Principal principal) {
        if (principal == null) {
            return null;
        }
        return companyService.findByEmail(principal.getName())
                .map(es.urjc.ecomostoles.backend.dto.CompanyDTO::new)
                .orElse(null);
    }

    @ModelAttribute("totalMessages")
    public int totalMessages(Principal principal) {
        if (principal == null) {
            return 0;
        }
        Optional<Company> userOpt = companyService.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            // Optimized: Atomic count from DB instead of loading all messages to memory
            return (int) messageService.countByRecipient(userOpt.get());
        }
        return 0;
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return java.time.LocalDate.now().getYear();
    }

    @ModelAttribute("supportEmail")
    public String supportEmail() {
        return configurationService.getConfigurationValue("contactEmail", "soporte@ecomostoles.com");
    }

    @ModelAttribute("currentDate")
    public String currentDate() {
        return java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES"))
                .format(java.time.LocalDate.now());
    }

    @ModelAttribute("unitList")
    public java.util.List<String> unitList() {
        return configurationService.getSanitizedList("unitList");
    }

    @ModelAttribute("availabilityList")
    public java.util.List<String> availabilityList() {
        return configurationService.getSanitizedList("availabilityList");
    }

    @ModelAttribute("categoryList")
    public java.util.List<String> categoryList() {
        return configurationService.getSanitizedList("categoryList");
    }

    @ModelAttribute("sectorList")
    public java.util.List<String> sectorList() {
        return configurationService.getSanitizedList("sectorList");
    }

    @ModelAttribute("mainUnit")
    public String mainUnit() {
        java.util.List<String> units = configurationService.getSanitizedList("unitList");
        return units.isEmpty() ? "kg" : units.get(0);
    }

    @ModelAttribute("platformName")
    public String getPlatformName() {
        return "EcoMóstoles";
    }

    @ModelAttribute("platformCity")
    public String platformCity() {
        return configurationService.getAutoValue("platformCity");
    }

    @ModelAttribute("industrialAreaList")
    public java.util.List<String> industrialAreaList() {
        return configurationService.getSanitizedList("industrialAreaList");
    }

    @ModelAttribute("platformLocation")
    public String platformLocation() {
        return configurationService.getAutoValue("industrialAreaList").split("\\r?\\n")[0];
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }

    @ModelAttribute("socialLinkedin")
    public String socialLinkedin() {
        return configurationService.getAutoValue("social_linkedin");
    }

    @ModelAttribute("socialTwitter")
    public String socialTwitter() {
        return configurationService.getAutoValue("social_twitter");
    }

    @ModelAttribute("socialFacebook")
    public String socialFacebook() {
        return configurationService.getAutoValue("social_facebook");
    }

    @ModelAttribute("platformStatus")
    public String platformStatus() {
        return configurationService.getAutoValue("platformStatus");
    }

    @ModelAttribute("isCompany")
    public boolean isCompany(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("EMPRESA"));
    }

    @ModelAttribute("isAdminPanel")
    public boolean isAdminPanel(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/admin");
    }
}
