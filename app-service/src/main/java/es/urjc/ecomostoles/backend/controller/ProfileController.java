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
import es.urjc.ecomostoles.backend.mapper.CompanyMapper;
import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller orchestrating dynamic profile manipulation.
 * 
 * Supports both self-service tenant administration and global administrative overrides 
 * via role-based access constraints. Enforces payload mapping and security auditing across updates 
 * safely isolating entity mutations from unauthorized lateral edits.
 */
@Controller
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public ProfileController(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
        this.companyService = companyService;
    }

    @GetMapping("/perfil")
    public String showProfile(Model model, @RequestParam(required = false) boolean success, Principal principal) {
        // Integrity check: Ensure the authenticated principal still exists in the database
        companyService.findByEmail(principal.getName())
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

    /**
     * Executes entity modifications bridging incoming generic forms to persistent models.
     * 
     * @param companyDTO marshaled domain transfer object containing unsanitized modifications.
     * @param bindingResult constraints violation tracker.
     * @param logoFile raw BLOB multipart buffer.
     * @param request base HTTP servlet extracting administrative contexts securely.
     * @param model MVC rendering frame.
     * @param principal authenticated command issuer.
     * @param redirectAttributes decoupled PRG carrier parameters.
     * @return logical redirect router dependent on the active role structure.
     */
    @PostMapping("/perfil/guardar")
    public String saveProfile(@Valid @ModelAttribute("company") CompanyDTO companyDTO,
            BindingResult bindingResult,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            HttpServletRequest request,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (logoFile != null && !logoFile.isEmpty()) {
            log.info("[Profile] Success -> Multipart logo received: '{}' ({} bytes)", logoFile.getOriginalFilename(), logoFile.getSize());
        } else {
            log.info("[Profile] INFO -> Profile update request without logo modification.");
        }

        Long targetId = companyDTO.getId();
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        if (bindingResult.hasErrors()) {
            log.warn("[Profile] Failed -> Validation constraints violated during profile update for ID: {}. Errors: {}", targetId, bindingResult.getFieldErrors().size());
            
            // Map errors as boolean flags for Mustache UI
            bindingResult.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            
            // Re-populate required model attributes to keep context
            model.addAttribute("isDashboard", true);
            if (isAdmin && targetId != null) {
                model.addAttribute("isAdminView", true);
                model.addAttribute("inspectedCompany", companyDTO);
            } else {
                model.addAttribute("company", companyDTO);
            }
            
            return "perfil_empresa";
        }

        // Logic: Evaluate authorization clearance dynamically. Admin roles break isolation barriers to assist users.
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
                log.info("[Storage] Persistence -> Synchronizing new binary logo for company ID: {} ({} bytes)", company.getId(), logoFile.getSize());
                company.setLogo(logoFile.getBytes());
            } catch (Exception e) {
                log.error("[Storage] ERROR -> IO exception during logo binary synchronization for company ID: {}", company.getId(), e);
            }
        }

        companyService.save(company);
        redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente.");

        if (isAdmin && targetId != null) {
            return "redirect:/admin/usuarios";
        }
        return "redirect:/perfil";
    }
}
