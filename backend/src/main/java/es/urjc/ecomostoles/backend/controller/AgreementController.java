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
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary state-machine controller mapping commercial material exchange
 * contracts.
 * 
 * Regulates the negotiation bindings bridging Supply (Offers) and Demand models
 * via
 * rigorous business logic safeguards. Implements strict Controller <-> Service
 * segmentation,
 * explicitly blocking IDOR (Insecure Direct Object Reference) vulnerabilities
 * during modifications.
 */
@Controller
public class AgreementController {

    private static final Logger log = LoggerFactory.getLogger(AgreementController.class);

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

    /**
     * Displays form elements allowing organizations to craft contractual exchanges
     * based on existing offers.
     * Applies tenant-isolation ensuring companies can only allocate their
     * registered assets.
     * 
     * @param model     MVC data map for the view dispatcher.
     * @param principal user context determining entity accessibility parameters.
     * @return relative classpath defining the creation form GUI.
     */
    @GetMapping("/acuerdo/nuevo")
    public String showAgreementForm(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeNewAgreement", true);
            model.addAttribute("isDashboard", true);

            // Business Logic: Only show offers belonging to the current company (Material
            // Owner)
            List<OfferSummary> myOffers = offerService.getActiveByCompany(company);
            model.addAttribute("offers", myOffers);

            List<Company> otherCompanies = companyService.getAll();
            otherCompanies.removeIf(e -> e.getId().equals(company.getId()));
            model.addAttribute("companies", otherCompanies);

            return "crear_acuerdo";
        }

        return "redirect:/";
    }

    /**
     * Yields the persistent, historical timeline of commercial agreements
     * concerning the logged party.
     * 
     * @param model     mutable attribute dictionary pushed into the view resolver.
     * @param principal authenticated connection context mapped to a distinct
     *                  Tenant.
     * @return logical view path executing the agreement table loop layout.
     */
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
     * Executes domain persistence for incoming Agreement forms. Leverages JSR 380
     * standards inside
     * the payload schema via the @Valid annotation layout.
     * 
     * @param agreement            un-marshaled transient HTTP payload capturing
     *                             form inputs.
     * @param result               standardized carrier validating binding defects.
     * @param offerId              external relational mapped Offer entity sequence.
     * @param destinationCompanyId foreign target counter-part executing the pickup.
     * @param model                generic template parameters.
     * @param principal            authenticated executor triggering the command
     *                             sequence.
     * @param redirectAttributes   HTTP session proxy for feedback messages avoiding
     *                             query pollution.
     * @return contextual URL string steering the user away upon error/success
     *         outcomes.
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
            log.warn("Validation error when registering agreement: {} errors", result.getErrorCount());
            result.getAllErrors().forEach(err -> log.debug("  - {}", err.getDefaultMessage()));
            if (offerId == null)
                log.warn("  - Missing offerId");
            if (destinationCompanyId == null)
                log.warn("  - Missing destinationCompanyId");

            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            if (offerId == null)
                model.addAttribute("error_offerId", true);
            if (destinationCompanyId == null)
                model.addAttribute("error_destinationCompanyId", true);

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
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Acuerdo registrado con éxito! El material ha sido reservado.");
        } catch (SelfAgreementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/acuerdo/nuevo";
        } catch (Exception e) {
            log.error("Unexpected error saving agreement", e);
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
                    es.urjc.ecomostoles.backend.utils.FormOptionsHelper.getUnitOptions(configurationService,
                            updatedAgreement.getUnit()));
            model.addAttribute("statusOptions",
                    es.urjc.ecomostoles.backend.utils.FormOptionsHelper
                            .getAgreementStatusOptions(updatedAgreement.getStatus()));
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this agreement.");
        }

        if (AgreementStatus.COMPLETED.equals(agreement.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se puede eliminar un acuerdo ya finalizado para no perder el histórico de impacto de CO2.");
            return "redirect:/acuerdos";
        }

        agreementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Acuerdo cancelado correctamente.");

        // Redirect based on role to avoid 403
        if (isAdminCompany(loggedCompany)) {
            return "redirect:/admin/acuerdos";
        }
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
