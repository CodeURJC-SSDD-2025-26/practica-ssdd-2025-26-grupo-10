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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        CompanyDTO safeCompany = new CompanyDTO(company);
        model.addAttribute("isDashboard", true);
        if (success) {
            model.addAttribute("exito", true);
        }
        return "perfil_empresa";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/perfil/{id}")
    public String showProfileById(@PathVariable Long id, Model model) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        CompanyDTO companyDTO = new CompanyDTO(company);
        model.addAttribute("empresaInspeccionada", companyDTO);
        model.addAttribute("esVistaAdmin", true);
        model.addAttribute("isDashboard", true);

        return "perfil_empresa";
    }

    @PostMapping("/perfil/guardar")
    public String saveProfile(@Valid @ModelAttribute("empresa") CompanyDTO companyDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile logoFile,
            HttpServletRequest request,
            Model model,
            Principal principal) {

        if (bindingResult.hasErrors()) {
            return "perfil_empresa";
        }

        // Logic: Admin can edit anyone by ID, others only themselves
        Company company;
        Long targetId = companyDTO.getId();
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN");

        if (isAdmin && targetId != null) {
            company = companyService.findById(targetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
        } else {
            company = companyService.findByEmail(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado"));
        }

        company.setCommercialName(companyDTO.getCommercialName());
        company.setPhone(companyDTO.getPhone());
        company.setAddress(companyDTO.getAddress());
        company.setIndustrialSector(companyDTO.getIndustrialSector());
        company.setDescription(companyDTO.getDescription());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                company.setLogo(logoFile.getBytes());
            } catch (Exception e) {
                log.error("⚠️ Error al leer el logo", e);
            }
        }

        companyService.save(company);

        if (isAdmin && targetId != null) {
            return "redirect:/perfil/" + targetId + "?exito=true";
        }
        return "redirect:/perfil?exito=true";
    }
}
