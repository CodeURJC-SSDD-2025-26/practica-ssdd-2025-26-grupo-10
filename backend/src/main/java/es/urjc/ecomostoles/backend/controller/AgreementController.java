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
            List<OfferSummary> myOffers = offerService.getByCompany(company);
            model.addAttribute("offers", myOffers);

            List<Company> allCompanies = companyService.getAll();
            allCompanies.removeIf(e -> e.getId().equals(company.getId()));
            model.addAttribute("companies", allCompanies);

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
            Principal principal) {

        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        Company loggedCompany = companyOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (result.hasErrors()) {
            model.addAttribute("offers", offerService.getByCompany(loggedCompany));

            List<Company> allCompanies = companyService.getAll();
            allCompanies.removeIf(e -> e.getId().equals(loggedCompany.getId()));
            model.addAttribute("companies", allCompanies);

            model.addAttribute("agreement", agreement);
            model.addAttribute("errors", result.getAllErrors());
            return "crear_acuerdo";
        }

        // ── Business Logic delegated to the Service
        // ────────────────────────────────────
        try {
            if (offerId != null && agreement != null) {
                agreementService.registerNewAgreement(agreement, principal.getName(), offerId, destinationCompanyId);
            }
        } catch (SelfAgreementException e) {
            return "redirect:/mercado?error=AutoAcuerdo";
        }

        return "redirect:/acuerdos";
    }

    /**
     * Shows detail of a specific agreement (with IDOR protection).
     */
    @GetMapping("/acuerdo/{id}")
    public String showAgreementDetail(@PathVariable Long id, Model model, Principal principal) {
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

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
            model.addAttribute("errors", result.getAllErrors());
            return "editar_acuerdo";
        }

        Agreement existingAgreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!hasPermissionOverAgreement(existingAgreement, loggedCompany, isAdminCompany(loggedCompany))) {
            return "redirect:/acuerdos?error=forbidden";
        }

        agreementService.updateAgreement(id, updatedAgreement);

        return "redirect:/acuerdos/" + id;
    }

    /**
     * Delete an agreement as a user (Cancellation).
     * Security: Verifies that the logged-in company is either the origin or
     * destination.
     */
    @PostMapping("/mis_acuerdos/eliminar/{id}")
    public String deleteAgreementAsUser(@PathVariable Long id, Principal principal) {
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!hasPermissionOverAgreement(agreement, loggedCompany, isAdminCompany(loggedCompany))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este acuerdo.");
        }

        if (AgreementStatus.COMPLETED.equals(agreement.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pueden eliminar acuerdos ya completados para preservar el historial de impacto.");
        }

        agreementService.delete(id);
        return "redirect:/acuerdos?success=eliminado";
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
