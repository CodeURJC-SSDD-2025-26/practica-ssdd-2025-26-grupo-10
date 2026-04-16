package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.DemandStatus;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import es.urjc.ecomostoles.backend.dto.SelectOption;

/**
 * Controller managing the lifecycle of Demand entities spawned by the authenticated tenant.
 * 
 * Enforces strict multi-tenant boundary checks. All destructive actions (Edit/Delete) explicitly
 * verify that the requesting principal legally owns the database record or harbors overriding 
 * ADMIN privileges before executing modifications via the DemandService.
 */
@Controller
public class MyDemandsController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyDemandsController.class);

    private final CompanyService companyService;
    private final DemandService demandService;
    private final es.urjc.ecomostoles.backend.service.ConfigurationService configurationService;

    public MyDemandsController(CompanyService companyService, DemandService demandService,
            es.urjc.ecomostoles.backend.service.ConfigurationService configurationService) {
        this.companyService = companyService;
        this.demandService = demandService;
        this.configurationService = configurationService;
    }

    /**
     * Secures object-level authorization by validating identity claims against entity schemas.
     * 
     * @param demandId target asset identifier sequence.
     * @param principal authenticated executor containing the role context map.
     * @return securely fetched demand entity safe for subsequent alterations.
     * @throws ResponseStatusException HTTP 403 if ownership chains do not match or user lacks ADMIN role.
     */
    private Demand verifyDemandOwnership(Long demandId, Principal principal) {
        Demand demand = demandService.findById(demandId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demand not found: " + demandId));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        boolean isAdmin = loggedCompany.getRoles() != null
                && loggedCompany.getRoles().contains("ADMIN");
        boolean isOwner = demand.getCompany() != null
                && demand.getCompany().getId().equals(loggedCompany.getId());

        if (!isAdmin && !isOwner) {
            log.warn("[Marketplace] Security -> Unauthorized access attempt to demand ID: {} by user: {}", demandId, principal.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this demand.");
        }
        return demand;
    }

    /**
     * Binds the private demand registry matrix, constrained strictly to the active user's DB projection.
     * 
     * @param model UI payload mapping dictionary.
     * @param principal proxy wrapping the current auth session identifier.
     * @param pageable resolved spring-data offset instructions mitigating heavy loads.
     * @return logical routing mapping rendering the private asset grid.
     */
    @GetMapping("/dashboard/mis-demandas")
    public String showMyDemands(Model model, Principal principal,
            @PageableDefault(size = 6) Pageable pageable) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeDemands", true);
            model.addAttribute("isDashboard", true);

            Page<Demand> demandsPage = demandService.getByCompanyPaginated(company, pageable);
            model.addAttribute("demands", demandsPage.getContent());
            model.addAttribute("hasDemands", !demandsPage.isEmpty());

            // Pagination metadata
            model.addAttribute("currentPage", demandsPage.getNumber() + 1);
            model.addAttribute("totalPages", demandsPage.getTotalPages() == 0 ? 1 : demandsPage.getTotalPages());
            model.addAttribute("hasNext", demandsPage.hasNext());
            model.addAttribute("hasPrevious", demandsPage.hasPrevious());
            model.addAttribute("prevPage", demandsPage.getNumber() - 1);
            model.addAttribute("nextPage", demandsPage.getNumber() + 1);
            model.addAttribute("totalItems", demandsPage.getTotalElements());

            // Pagination metadata
            model.addAttribute("paginationBaseUrl", "/dashboard/mis-demandas");
            model.addAttribute("paginationQueryString", "");

            // Summary counters
            model.addAttribute("activeCount", demandService.countActiveByCompany(company));
            model.addAttribute("pausedCount", demandService.countPausedByCompany(company));
            model.addAttribute("closedCount", demandService.countClosedByCompany(company));
            model.addAttribute("totalViews", demandService.sumVisitsByCompany(company));

            return "mis_demandas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // GET /demanda/nueva — Show new demand form
    // -------------------------------------------------------------------------
    @GetMapping("/demanda/nueva")
    public String showNewDemandForm(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            model.addAttribute("activeNewDemand", true);
            model.addAttribute("isDashboard", true);
            model.addAttribute("demand", new Demand());
            injectDynamicOptions(model);
            return "crear_solicitud";
        }
        return "redirect:/";
    }

    private void injectDynamicOptions(Model model) {
        model.addAttribute("wasteCategories",
                FormOptionsHelper.getCategoryOptions(configurationService, null));
        model.addAttribute("availabilityOptions",
                FormOptionsHelper.getUrgencyOptions(configurationService, null));
        model.addAttribute("validityOptions",
                FormOptionsHelper.getValidityOptions(null));
    }

    /**
     * Orchestrates the persistence workflow mapping HTTP inputs into secure domain models.
     * Relies on JSR 380 standards triggering automatic pre-flight data inspections.
     * 
     * @param demand marshaled transient DTO holding unverified form inputs.
     * @param result interceptor carrying field constraint violations for iterative UI feedback.
     * @param model layout binder context.
     * @param principal authoritative session triggering the creation logic.
     * @param redirectAttributes parameter carrier resolving post-redirect loops gracefully.
     * @return conditional redirection routing context.
     */
    @PostMapping("/demanda/nueva")
    public String saveNewDemand(@Valid @ModelAttribute("demand") Demand demand,
            BindingResult result,
            Model model,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("demand", demand);
            injectDynamicOptions(model);
            return "crear_solicitud";
        }

        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            demand.setCompany(companyOpt.get());
            demand.setPublicationDate(LocalDateTime.now());
            demand.setStatus(DemandStatus.ACTIVE);
            demandService.save(demand);
            log.info("[Marketplace] Success -> New demand published by '{}': '{}'", principal.getName(), demand.getTitle());
            redirectAttributes.addFlashAttribute("successMessage", "¡Demanda publicada con éxito!");
            
            // FIX: Redirect based on role to avoid 403
            if (companyOpt.get().getRoles().contains("ADMIN")) {
                return "redirect:/admin/demandas";
            }
            return "redirect:/dashboard/mis-demandas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /demandas/{id}/eliminar — Delete demand (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/eliminar")
    public String deleteDemand(@PathVariable Long id, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        verifyDemandOwnership(id, principal);
        demandService.delete(id);
        log.info("[Marketplace] Success -> Demand ID: {} deleted by owner/admin: {}", id, principal.getName());
        
        redirectAttributes.addFlashAttribute("successMessage", "Demanda eliminada con éxito.");

        // FIX: Redirect based on role to avoid 403
        Company loggedUser = companyService.findByEmail(principal.getName()).orElseThrow();
        if (loggedUser.getRoles().contains("ADMIN")) {
            return "redirect:/admin/demandas";
        }
        return "redirect:/dashboard/mis-demandas";
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    private void loadSelectOptions(Model model, Demand demand) {
        model.addAttribute("wasteCategories",
                FormOptionsHelper.getCategoryOptions(configurationService, demand.getWasteCategory()));
        model.addAttribute("unitOptions", FormOptionsHelper.getUnitOptions(configurationService, demand.getUnit()));
        model.addAttribute("availabilityOptions",
                FormOptionsHelper.getUrgencyOptions(configurationService, demand.getUrgency()));
        model.addAttribute("validityOptions", FormOptionsHelper.getValidityOptions(demand.getValidity()));

        // Dynamic Select Options for status (Spanish labels from Enum)
        List<SelectOption> statusOptions = new ArrayList<>();
        for (DemandStatus status : DemandStatus.values()) {
            statusOptions.add(new SelectOption(status.name(), status.getDisplayName(), status.equals(demand.getStatus())));
        }
        model.addAttribute("statusOptions", statusOptions);
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/demandas/{id}/editar")
    public String showEditDemandForm(@PathVariable Long id, Model model, Principal principal) {
        Demand demand = verifyDemandOwnership(id, principal);
        model.addAttribute("demand", demand);
        model.addAttribute("isDashboard", true);

        loadSelectOptions(model, demand);

        return "editar_solicitud";
    }

    /**
     * Modifies existing demand properties through tightly controlled data-binding.
     * 
     * @param id verified target identifier belonging to the requester.
     * @param demandForm transient detached object encapsulating state changes.
     * @param result internal error flag collector avoiding SQL-level constraint blasts.
     * @param model HTTP response properties payload.
     * @param principal active actor executing the update command.
     * @param redirectAttributes proxy facilitating UX transitions safely via the PRG pattern.
     * @return path command to revert back into the administrative table view.
     */
    @PostMapping("/demandas/{id}/editar")
    public String saveEditedDemand(@PathVariable Long id,
            @Valid @ModelAttribute("demand") Demand demandForm,
            BindingResult result,
            Model model,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Company loggedUser = companyService.findByEmail(principal.getName()).orElseThrow();
        Demand existingDemand = verifyDemandOwnership(id, principal);

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            loadSelectOptions(model, demandForm);

            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("demand", demandForm); // FIX: Add missing model attribute
            demandForm.setId(id);
            
            // SECURITY: Ensure sidebar knows user role on validation fail
            if (loggedUser.getRoles().contains("ADMIN")) {
                model.addAttribute("isAdmin", true);
            }
            
            return "editar_solicitud";
        }

        existingDemand.setTitle(demandForm.getTitle());
        existingDemand.setDescription(demandForm.getDescription());
        existingDemand.setWasteCategory(demandForm.getWasteCategory());
        existingDemand.setQuantity(demandForm.getQuantity());
        existingDemand.setUnit(demandForm.getUnit());
        existingDemand.setUrgency(demandForm.getUrgency());
        existingDemand.setMaxBudget(demandForm.getMaxBudget());
        existingDemand.setMaxBudget(demandForm.getMaxBudget());
        existingDemand.setPickupZone(demandForm.getPickupZone());
        existingDemand.setValidity(demandForm.getValidity());
        existingDemand.setStatus(demandForm.getStatus());

        demandService.save(existingDemand);
        log.info("[Marketplace] Success -> Demand ID: {} updated by: {}", id, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Demanda actualizada con éxito.");

        // FIX: Redirect based on role to avoid 403
        if (loggedUser.getRoles().contains("ADMIN")) {
            return "redirect:/admin/demandas";
        }
        return "redirect:/dashboard/mis-demandas";
    }
}
