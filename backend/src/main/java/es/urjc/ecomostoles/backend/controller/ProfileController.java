package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

/**
 * Controller for handling company profile view.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to CompanyService.
 */
@Controller
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final CompanyService companyService;

    public ProfileController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/perfil")
    public String showProfile(Model model, @RequestParam(required = false) boolean success, Principal principal) {
        Company company = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        model.addAttribute("isDashboard", true);
        if (success) {
            model.addAttribute("success", true);
        }
        return "perfil_empresa";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/perfil/{id}")
    public String showProfileById(@PathVariable Long id, Model model) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        CompanyDTO companyDTO = new CompanyDTO(company);
        model.addAttribute("inspectedCompany", companyDTO);
        model.addAttribute("isAdminView", true);
        model.addAttribute("isDashboard", true);

        return "perfil_empresa";
    }

    @PostMapping("/perfil/guardar")
    public String saveProfile(@Valid @ModelAttribute("company") CompanyDTO companyDTO,
            BindingResult bindingResult,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            HttpServletRequest request,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        log.info("--- START LOGO UPLOAD DEBUG ---");
        if (logoFile == null) {
            log.error("CRITICAL ERROR: 'logoFile' is NULL from the frontend.");
        } else if (logoFile.isEmpty()) {
            log.warn("WARNING: File received but it is EMPTY (0 bytes).");
        } else {
            log.info("FRONTEND SUCCESS: File received. Name: {}, Size: {} bytes",
                    logoFile.getOriginalFilename(), logoFile.getSize());
        }
        log.info("--- END LOGO UPLOAD DEBUG ---");

        Long targetId = companyDTO.getId();
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        if (bindingResult.hasErrors()) {
            log.error("⚠️ Validation errors updating profile: {}", bindingResult.getAllErrors());
            
            // Map errors for Mustache consumption in the next request
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
            
            redirectAttributes.addFlashAttribute("errors", errors);
            
            if (isAdmin && targetId != null) {
                return "redirect:/perfil/" + targetId;
            }
            return "redirect:/perfil";
        }

        // Logic: Admin can edit anyone by ID, others only themselves
        Company company;

        if (isAdmin && targetId != null) {
            company = companyService.findById(targetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        } else {
            company = companyService.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        }

        company.setCommercialName(companyDTO.getCommercialName());
        company.setPhone(companyDTO.getPhone());
        company.setAddress(companyDTO.getAddress());
        company.setIndustrialSector(companyDTO.getIndustrialSector());
        company.setDescription(companyDTO.getDescription());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                log.info("💾 Persistence: Updating logo for company {}: {} bytes", company.getId(), logoFile.getSize());
                company.setLogo(logoFile.getBytes());
            } catch (Exception e) {
                log.error("⚠️ Error reading the logo", e);
            }
        }

        companyService.save(company);

        if (isAdmin && targetId != null) {
            return "redirect:/perfil/" + targetId + "?success=true";
        }
        return "redirect:/perfil?success=true";
    }
}
