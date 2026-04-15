package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.MessageService;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

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
            return (int) messageService.countByRecipient(userOpt.get());
        }
        return 0;
    }

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        if (principal == null) return 0;
        return companyService.findByEmail(principal.getName())
            .map(messageService::countUnreadByRecipient)
            .orElse(0L);
    }

    @ModelAttribute("hasUnreadMessages")
    public boolean hasUnreadMessages(Principal principal) {
        if (principal == null) return false;
        return companyService.findByEmail(principal.getName())
            .map(c -> messageService.countUnreadByRecipient(c) > 0)
            .orElse(false);
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
    public java.util.List<java.util.Map<String, Object>> sectorList(Principal principal) {
        String currentSector = "";
        if (principal != null) {
            java.util.Optional<es.urjc.ecomostoles.backend.model.Company> company = companyService.findByEmail(principal.getName());
            if (company.isPresent()) {
                currentSector = company.get().getIndustrialSector();
            }
        }

        final String selected = currentSector;
        return configurationService.getSanitizedList("sectorList").stream().map(s -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", s);
            map.put("displayName", s);
            map.put("selected", s.equals(selected));
            return map;
        }).collect(java.util.stream.Collectors.toList());
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

    @ModelAttribute("cacheBuster")
    public long getCacheBuster() {
        return System.currentTimeMillis();
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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException() {
        log.warn("⚠️ Max upload size exceeded. Redirecting with query param.");
        return "redirect:/registro?error=size";
    }
}
