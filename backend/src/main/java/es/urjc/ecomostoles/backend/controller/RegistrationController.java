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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final CompanyService companyService;
    private final ConfigurationService configurationService;

    public RegistrationController(CompanyService companyService, ConfigurationService configurationService) {
        this.companyService = companyService;
        this.configurationService = configurationService;
    }

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registroDTO", new RegistrationDTO());
        injectDynamicOptions(model);
        return "registro";
    }

    private void injectDynamicOptions(Model model) {
        // Fetch values dynamically from the platform settings
        String catsStr = configurationService.getAutoValue("listaCategorias");
        List<String> categories = Arrays.asList(catsStr.split("\\r?\\n"));

        String industrialAreasStr = configurationService.getConfigurationValue("listaPoligonos",
                "Polígono Regordoño\nPolígono Las Nieves\nMóstoles Tecnológico\nOtro (Especificar)");
        List<String> industrialAreas = Arrays.asList(industrialAreasStr.split("\\r?\\n"));

        String sectorsStr = configurationService.getAutoValue("listaSectores");
        List<String> sectors = Arrays.asList(sectorsStr.split("\\r?\\n"));

        model.addAttribute("categorias", categories);
        model.addAttribute("sectores", sectors);
        model.addAttribute("poligonos", industrialAreas);

        // Formatted list for select components
        model.addAttribute("listaSectores", sectors.stream().map(s -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", s);
            map.put("display", s);
            return map;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/registro")
    public String registerCompany(@Valid @ModelAttribute("registroDTO") RegistrationDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", true);
            bindingResult.getFieldErrors()
                    .forEach(error -> model.addAttribute("err_" + error.getField(), error.getDefaultMessage()));
            // Also global errors (like the password mismatch)
            bindingResult.getGlobalErrors()
                    .forEach(error -> model.addAttribute("err_global", error.getDefaultMessage()));
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
                    model.addAttribute("errorMsg", "El logo es demasiado pesado. El límite máximo es 5MB.");
                    injectDynamicOptions(model);
                    return "registro";
                }

                if (contentType == null || (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
                    model.addAttribute("error", true);
                    model.addAttribute("errorMsg", "Formato de imagen no soportado. Usa JPG, PNG o WebP.");
                    injectDynamicOptions(model);
                    return "registro";
                }

                logoBytes = dto.getLogoFile().getBytes();
            }

            companyService.registerNewCompany(newCompany, dto.getPassword(), logoBytes);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            model.addAttribute("errorMsg", e.getMessage());
            return "registro";
        } catch (Exception e) {
            log.error("⚠️ Error general en el registro de empresa", e);
            model.addAttribute("error", true);
            return "registro";
        }

        return "redirect:/login?registrado";
    }
}
