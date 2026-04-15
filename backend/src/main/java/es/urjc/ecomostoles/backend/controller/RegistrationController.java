package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import es.urjc.ecomostoles.backend.dto.RegistrationDTO;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller provisioning raw access and organizational bootstrapping.
 * 
 * Manages the transition of anonymous prospect traffic into registered platform tenants.
 * Coordinates heavy multipart constraints bridging raw HTTP payloads with the business logic
 * establishing database baseline schemas for new organizations.
 */
@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final CompanyService companyService;
    private final ConfigurationService configurationService;

    public RegistrationController(CompanyService companyService, ConfigurationService configurationService) {
        this.companyService = companyService;
        this.configurationService = configurationService;
    }

    /**
     * Renders the unauthenticated organizational registration funnel.
     * 
     * Intercepts authenticated traffic looping back to structural forms, bouncing them passively
     * into the dashboard to prevent ghost registrations.
     * 
     * @param error URI-mapped query string flag signaling upload-size violations.
     * @param model layout payload binder.
     * @return logical path serving the registration HTML block.
     */
    @GetMapping("/registro")
    public String showRegistrationForm(
            @org.springframework.web.bind.annotation.RequestParam(value = "error", required = false) String error,
            Model model) {
        // Redirect if user is already authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("registrationDTO", new RegistrationDTO());

        if ("size".equals(error)) {
            model.addAttribute("error", true);
            model.addAttribute("errorMsg", "El archivo es demasiado grande. El tamaño máximo permitido es de 5MB.");
        }

        injectDynamicOptions(model);
        return "registro";
    }

    private void injectDynamicOptions(Model model) {
        // Fetch values dynamically from the platform settings
        String categoryListStr = configurationService.getAutoValue("categoryList");
        List<String> categories = Arrays.asList(categoryListStr.split("\\r?\\n"));

        String industrialAreaListStr = configurationService.getAutoValue("industrialAreaList");
        List<String> industrialAreas = Arrays.asList(industrialAreaListStr.split("\\r?\\n"));

        String sectorListStr = configurationService.getAutoValue("sectorList");
        List<String> sectors = Arrays.asList(sectorListStr.split("\\r?\\n"));

        model.addAttribute("categoryList", categories);
        model.addAttribute("areaOptions", industrialAreas);

        // Formatted list for select components
        model.addAttribute("sectorOptions", sectors.stream().map(s -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", s);
            map.put("displayName", s);
            return map;
        }).collect(Collectors.toList()));
    }

    /**
     * Assembles complex registration inputs into persisted domain bounds.
     * 
     * @param dto strongly-typed form binding capturing company configurations.
     * @param bindingResult proxy containing failed validation traces.
     * @param model rendering target view data payload.
     * @param redirectAttributes ephemeral storage for feedback resolution mapping over redirects.
     * @return router string executing contextual UI jumps based on validation success.
     */
    @PostMapping("/registro")
    public String registerCompany(@Valid @ModelAttribute("registrationDTO") RegistrationDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", true);
            bindingResult.getFieldErrors()
                    .forEach(error -> model.addAttribute("error_" + error.getField(), error.getDefaultMessage()));
            // Also global errors (like the password mismatch)
            bindingResult.getGlobalErrors()
                    .forEach(error -> model.addAttribute("error_global", error.getDefaultMessage()));
            injectDynamicOptions(model);
            return "registro";
        }

        try {
            // Mapping DTO to Entity
            Company newCompany = new Company();
            newCompany.setCommercialName(dto.getCommercialName());
            newCompany.setTaxId(dto.getTaxId());

            // Handle dynamic "Other" location logic
            String finalAddress = dto.getAddress();
            if (("Otro".equals(finalAddress) || "Otro (Especificar)".equals(finalAddress))
                    && dto.getOtherAddress() != null && !dto.getOtherAddress().isBlank()) {
                finalAddress = dto.getOtherAddress();
            }
            newCompany.setAddress(finalAddress);

            newCompany.setIndustrialSector(dto.getSector());
            newCompany.setPhone(dto.getPhone());
            newCompany.setDescription(dto.getDescription());
            newCompany.setContactEmail(dto.getContactEmail());

            byte[] logoBytes = null;
            if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
                // Enterprise Plus: Security Validation for Assets
                String contentType = dto.getLogoFile().getContentType();
                long size = dto.getLogoFile().getSize();

                if (size > 5 * 1024 * 1024) { // 5MB Limit
                    model.addAttribute("error", true);
                    model.addAttribute("errorMessage", "El logo es demasiado pesado. El límite máximo es 5MB.");
                    injectDynamicOptions(model);
                    return "registro";
                }

                if (contentType == null || (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
                    model.addAttribute("error", true);
                    model.addAttribute("errorMessage", "Formato de imagen no soportado. Usa JPG, PNG o WebP.");
                    injectDynamicOptions(model);
                    return "registro";
                }

                logoBytes = dto.getLogoFile().getBytes();
            }

            companyService.registerNewCompany(newCompany, dto.getPassword(), logoBytes);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", e.getMessage());
            injectDynamicOptions(model);
            return "registro";
        } catch (Exception e) {
            log.error("⚠️ General error in company registration", e);
            model.addAttribute("error", true);
            injectDynamicOptions(model);
            return "registro";
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "¡Cuenta creada con éxito! Ya puedes acceder con tus credenciales.");
        return "redirect:/login";
    }
}
