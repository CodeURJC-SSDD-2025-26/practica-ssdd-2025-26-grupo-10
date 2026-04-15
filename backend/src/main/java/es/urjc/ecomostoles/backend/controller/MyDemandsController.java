package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.DemandStatus;
import es.urjc.ecomostoles.backend.model.WasteCategory;

import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.CompanyService;
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
 * Controller to handle CRUD operations for demands specific to the logged-in
 * company.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandService and CompanyService.
 */
@Controller
public class MyDemandsController {

    private final CompanyService companyService;
    private final DemandService demandService;
    private final es.urjc.ecomostoles.backend.service.ConfigurationService configurationService;

    public MyDemandsController(CompanyService companyService, DemandService demandService,
            es.urjc.ecomostoles.backend.service.ConfigurationService configurationService) {
        this.companyService = companyService;
        this.demandService = demandService;
        this.configurationService = configurationService;
    }

    // -------------------------------------------------------------------------
    // Ownership helper: returns the demand if the user is the author or ADMIN.
    // -------------------------------------------------------------------------
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this demand.");
        }
        return demand;
    }

    // -------------------------------------------------------------------------
    // GET /dashboard/mis-demandas
    // -------------------------------------------------------------------------
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
            model.addAttribute("countActivas", demandService.countActiveByCompany(company));
            model.addAttribute("countPausadas", demandService.countPausedByCompany(company));
            model.addAttribute("countFinalizadas", demandService.countClosedByCompany(company));
            model.addAttribute("countVisitasTotales", demandService.sumVisitsByCompany(company));

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
        model.addAttribute("wasteCategories", WasteCategory.values());
        model.addAttribute("unitList", configurationService.getSanitizedList("unitList"));
        model.addAttribute("availabilityList",
                configurationService.getSanitizedList("availabilityList"));
    }

    // -------------------------------------------------------------------------
    // POST /demanda/nueva — Create new demand with Bean Validation
    // -------------------------------------------------------------------------
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
            redirectAttributes.addFlashAttribute("successMessage", "¡Demanda publicada con éxito!");
            return "redirect:/dashboard/mis-demandas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /demandas/{id}/eliminar — Delete demand (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/eliminar")
    public String deleteDemand(@PathVariable Long id, Principal principal) {
        verifyDemandOwnership(id, principal);
        demandService.delete(id);
        return "redirect:/dashboard/mis-demandas";
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    private void loadSelectOptions(Model model, Demand demand) {
        // Dynamic Categories with selection logic
        List<SelectOption> categoryOptions = new ArrayList<>();
        for (WasteCategory cat : WasteCategory.values()) {
            categoryOptions.add(new SelectOption(cat.name(), cat.getDisplayName(), cat.equals(demand.getWasteCategory())));
        }
        model.addAttribute("wasteCategories", categoryOptions);

        // Dynamic Units
        List<String> unitsList = configurationService.getSanitizedList("unitList");
        List<SelectOption> unitOptions = new ArrayList<>();
        for (String u : unitsList) {
            unitOptions.add(new SelectOption(u, u, u.equals(demand.getUnit())));
        }
        model.addAttribute("unitOptions", unitOptions);

        // Dynamic Urgency (reusing availability list)
        List<String> availabiltyList = configurationService.getSanitizedList("availabilityList");
        List<SelectOption> availabilityOptions = new ArrayList<>();
        for (String d : availabiltyList) {
            availabilityOptions.add(new SelectOption(d, d, d.equals(demand.getUrgency())));
        }
        model.addAttribute("availabilityOptions", availabilityOptions);

        // Dynamic Select Options for status
        List<SelectOption> statusOptions = new ArrayList<>();
        statusOptions.add(new SelectOption("ACTIVE", "ACTIVA", DemandStatus.ACTIVE.equals(demand.getStatus())));
        statusOptions.add(new SelectOption("PAUSED", "PAUSADA", DemandStatus.PAUSED.equals(demand.getStatus())));
        statusOptions.add(new SelectOption("CLOSED", "FINALIZADA", DemandStatus.CLOSED.equals(demand.getStatus())));
        model.addAttribute("statusOptions", statusOptions);

        // Standard Validity Options
        List<SelectOption> validityOptions = new ArrayList<>();
        validityOptions.add(new SelectOption("7", "7 días", "7".equals(demand.getValidity())));
        validityOptions.add(new SelectOption("15", "15 días", "15".equals(demand.getValidity())));
        validityOptions.add(new SelectOption("30", "30 días", "30".equals(demand.getValidity())));
        validityOptions.add(new SelectOption("90", "90 días", "90".equals(demand.getValidity())));
        validityOptions.add(new SelectOption("0", "Indefinido / Consultar", "0".equals(demand.getValidity())));
        model.addAttribute("validityOptions", validityOptions);
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

    // -------------------------------------------------------------------------
    // POST /demanda/editar/{id} — Save changes with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/editar")
    public String saveEditedDemand(@PathVariable Long id,
            @Valid @ModelAttribute("demand") Demand demandForm,
            BindingResult result,
            Model model,
            Principal principal) {

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Demand existingDemand = verifyDemandOwnership(id, principal);

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            loadSelectOptions(model, demandForm);

            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("demand", demandForm); // FIX: Add missing model attribute
            demandForm.setId(id);
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

        return "redirect:/dashboard/mis-demandas";
    }
}
