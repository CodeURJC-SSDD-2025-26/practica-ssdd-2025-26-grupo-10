package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;

/**
 * Controller responsible for displaying and registering new commercial
 * agreements.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to AgreementService, CompanyService and
 * OfferService.
 */
@Controller
public class AgreementController {

    private final AgreementService agreementService;
    private final CompanyService companyService;
    private final OfferService offerService;
    private final es.urjc.ecomostoles.backend.service.ConfigurationService configurationService;

    public AgreementController(AgreementService agreementService,
            CompanyService companyService,
            OfferService offerService,
            es.urjc.ecomostoles.backend.service.ConfigurationService configurationService) {
        this.agreementService = agreementService;
        this.companyService = companyService;
        this.offerService = offerService;
        this.configurationService = configurationService;
    }

    /** Shows the form to register a new agreement. */
    @GetMapping("/acuerdo/nuevo")
    public String showAgreementForm(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeNewAgreement", true);
            model.addAttribute("isDashboard", true);
            
            // Business Logic: Only show offers belonging to the current company (Material Owner)
            List<OfferSummary> myOffers = offerService.getActiveByCompany(company);
            model.addAttribute("offers", myOffers);

            List<Company> otherCompanies = companyService.getAll();
            otherCompanies.removeIf(e -> e.getId().equals(company.getId()));
            model.addAttribute("companies", otherCompanies);

            return "crear_acuerdo";
        }

        return "redirect:/";
    }

    /** Shows the agreement history for the active company. */
    @GetMapping("/acuerdos")
    public String showMyAgreements(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeAgreements", true);
            model.addAttribute("isDashboard", true);
            List<Agreement> myAgreements = agreementService.getByCompany(company);
            model.addAttribute("agreements", myAgreements);

            // Dynamic KPI counts for status section
            model.addAttribute("totalCompleted",
                    agreementService.countByCompanyAndStatus(company, AgreementStatus.COMPLETED));
            model.addAttribute("totalPending",
                    agreementService.countByCompanyAndStatus(company, AgreementStatus.PENDING));

            return "mis_acuerdos";
        }

        return "redirect:/";
    }

    /**
     * Processes the creation of a new agreement with Bean Validation.
     */
    @PostMapping("/acuerdo/nuevo")
    public String registerAgreement(@Valid @ModelAttribute Agreement agreement,
            BindingResult result,
            @RequestParam(name = "offerId", required = false) Long offerId,
            @RequestParam(name = "destinationCompanyId", required = false) Long destinationCompanyId,
            Model model,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        Company loggedCompany = companyOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (result.hasErrors() || offerId == null || destinationCompanyId == null) {
            System.out.println("❌ Error de validación al registrar acuerdo:");
            result.getAllErrors().forEach(System.out::println);
            if (offerId == null) System.out.println("- Falta offerId");
            if (destinationCompanyId == null) System.out.println("- Falta destinationCompanyId");

            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            if (offerId == null) model.addAttribute("error_offerId", true);
            if (destinationCompanyId == null) model.addAttribute("error_destinationCompanyId", true);

            // Re-populate with OWN offers and other companies
            model.addAttribute("offers", offerService.getActiveByCompany(loggedCompany));
            List<Company> allCompanies = companyService.getAll();
            allCompanies.removeIf(e -> e.getId().equals(loggedCompany.getId()));
            model.addAttribute("companies", allCompanies);

            model.addAttribute("agreement", agreement);
            model.addAttribute("errors", result.getAllErrors());
            return "crear_acuerdo";
        }

        try {
            agreementService.registerNewAgreement(agreement, principal.getName(), offerId, destinationCompanyId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Acuerdo registrado con éxito! El material ha sido reservado.");
        } catch (SelfAgreementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/acuerdo/nuevo";
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al guardar acuerdo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error interno al procesar el acuerdo.");
            return "redirect:/acuerdo/nuevo";
        }

        return "redirect:/acuerdos";
    }

    /**
     * Shows detail of a specific agreement (with IDOR protection).
     */
    @GetMapping("/acuerdo/{id}")
    public String showAgreementDetail(@PathVariable Long id, Model model, Principal principal) {
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agreement not found"));

        String userEmail = principal.getName();
        Company loggedCompany = companyService.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean isAdminView = isAdminCompany(loggedCompany);
        if (!hasPermissionOverAgreement(agreement, loggedCompany, isAdminView)) {
            return "redirect:/acuerdos?error=forbidden";
        }

        model.addAttribute("agreement", agreement);
        model.addAttribute("isDashboard", true);
        model.addAttribute("isAdminView", isAdminView);
        model.addAttribute("supportEmail", configurationService.getAutoValue("contactEmail"));

        return "detalle_acuerdo";
    }

    /**
     * Shows the form to edit an existing agreement.
     */
    @GetMapping("/acuerdos/{id}/editar")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agreement not found"));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!hasPermissionOverAgreement(agreement, loggedCompany, isAdminCompany(loggedCompany))) {
            return "redirect:/acuerdos?error=forbidden";
        }

        model.addAttribute("agreement", agreement);
        model.addAttribute("isDashboard", true);

        // Centralized utility for form options (DRY)
        model.addAttribute("unitOptions",
                FormOptionsHelper.getUnitOptions(configurationService, agreement.getUnit()));
        model.addAttribute("statusOptions", FormOptionsHelper.getAgreementStatusOptions(agreement.getStatus()));

        return "editar_acuerdo";
    }

    /**
     * Processes the update of an existing agreement.
     */
    @PostMapping("/acuerdos/{id}/editar")
    public String updateAgreement(@PathVariable Long id, @Valid @ModelAttribute Agreement updatedAgreement,
            BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("agreement", updatedAgreement);
            updatedAgreement.setId(id);
            // Repopulate dynamic options (DRY with showEditForm)
            model.addAttribute("unitOptions", 
                es.urjc.ecomostoles.backend.utils.FormOptionsHelper.getUnitOptions(configurationService, updatedAgreement.getUnit()));
            model.addAttribute("statusOptions", 
                es.urjc.ecomostoles.backend.utils.FormOptionsHelper.getAgreementStatusOptions(updatedAgreement.getStatus()));
            model.addAttribute("errors", result.getAllErrors());
            return "editar_acuerdo";
        }

        Agreement existingAgreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agreement not found"));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!hasPermissionOverAgreement(existingAgreement, loggedCompany, isAdminCompany(loggedCompany))) {
            return "redirect:/acuerdos?error=forbidden";
        }

        agreementService.updateAgreement(id, updatedAgreement);

        return "redirect:/acuerdo/" + id;
    }

    /**
     * Delete an agreement as a user (Cancellation).
     * Security: Verifies that the logged-in company is either the origin or
     * destination.
     */
    @PostMapping("/mis_acuerdos/eliminar/{id}")
    public String deleteAgreementAsUser(@PathVariable Long id, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agreement not found"));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!hasPermissionOverAgreement(agreement, loggedCompany, isAdminCompany(loggedCompany))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this agreement.");
        }

        if (AgreementStatus.COMPLETED.equals(agreement.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se puede eliminar un acuerdo ya finalizado para no perder el histórico de impacto de CO2.");
            return "redirect:/acuerdos";
        }

        agreementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Acuerdo cancelado correctamente.");
        return "redirect:/acuerdos";
    }

    private boolean hasPermissionOverAgreement(Agreement agreement, Company loggedCompany, boolean isAdmin) {
        if (isAdmin)
            return true;
        String email = loggedCompany.getContactEmail();
        boolean isOrigin = agreement.getOriginCompany() != null
                && agreement.getOriginCompany().getContactEmail().equals(email);
        boolean isDestination = agreement.getDestinationCompany() != null
                && agreement.getDestinationCompany().getContactEmail().equals(email);
        return isOrigin || isDestination;
    }

    private boolean isAdminCompany(Company company) {
        return company.getRoles() != null && company.getRoles().contains("ADMIN");
    }
}
