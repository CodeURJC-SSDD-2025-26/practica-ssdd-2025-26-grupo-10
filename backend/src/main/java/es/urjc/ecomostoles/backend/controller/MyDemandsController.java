package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.DemandStatus;

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
                        HttpStatus.NOT_FOUND, "Demanda no encontrada: " + demandId));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean isAdmin = loggedCompany.getRoles() != null
                && loggedCompany.getRoles().contains("ADMIN");
        boolean isOwner = demand.getCompany() != null
                && demand.getCompany().getId().equals(loggedCompany.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta demanda.");
        }
        return demand;
    }

    // -------------------------------------------------------------------------
    // GET /dashboard/mis-demandas
    // -------------------------------------------------------------------------
    @GetMapping("/dashboard/mis-demandas")
    public String showMyDemands(Model model, Principal principal,
            @PageableDefault(size = 5) Pageable pageable) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeDemandas", true);
            model.addAttribute("isDashboard", true);

            Page<Demand> demandsPage = demandService.getByCompanyPaginated(company, pageable);
            model.addAttribute("demandas", demandsPage.getContent());
            model.addAttribute("hasDemandas", !demandsPage.isEmpty());

            // Pagination metadata
            model.addAttribute("currentPage", demandsPage.getNumber() + 1);
            model.addAttribute("totalPages", demandsPage.getTotalPages());
            model.addAttribute("hasNext", demandsPage.hasNext());
            model.addAttribute("hasPrev", demandsPage.hasPrevious());
            model.addAttribute("prevPage", demandsPage.getNumber() - 1);
            model.addAttribute("nextPage", demandsPage.getNumber() + 1);
            model.addAttribute("totalItems", demandsPage.getTotalElements());

            // Dynamic base URL for pagination partial
            model.addAttribute("pagBaseUrl", "/dashboard/mis-demandas");
            model.addAttribute("pagQueryString", "");

            model.addAttribute("totalDemandasActivas", demandService.countActiveByCompany(company));
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
            model.addAttribute("activeNuevaDemanda", true);
            model.addAttribute("isDashboard", true);
            model.addAttribute("demanda", new Demand());
            injectDynamicOptions(model);
            return "crear_solicitud";
        }
        return "redirect:/";
    }

    private void injectDynamicOptions(Model model) {
        model.addAttribute("listaCategorias", configurationService.getSanitizedList("listaCategorias"));
        model.addAttribute("listaUnidades", configurationService.getSanitizedList("listaUnidades"));
        model.addAttribute("listaDisponibilidades",
                configurationService.getSanitizedList("listaDisponibilidades"));
    }

    // -------------------------------------------------------------------------
    // POST /demanda/nueva — Create new demand with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/demanda/nueva")
    public String saveNewDemand(@Valid @ModelAttribute("demanda") Demand demand,
            BindingResult result,
            Model model,
            Principal principal) {

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("errores", result.getAllErrors());
            model.addAttribute("demanda", demand);
            injectDynamicOptions(model);
            return "crear_solicitud";
        }

        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            demand.setCompany(companyOpt.get());
            demand.setPublicationDate(LocalDateTime.now());
            demand.setStatus(DemandStatus.ACTIVE);
            demandService.save(demand);
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
        // Dynamic Categories
        List<String> categories = configurationService.getSanitizedList("listaCategorias");
        List<SelectOption> categoryOptions = new ArrayList<>();
        for (String cat : categories) {
            categoryOptions.add(new SelectOption(cat, cat, cat.equals(demand.getMaterialCategory())));
        }
        model.addAttribute("opcionesCategoria", categoryOptions);

        // Dynamic Units
        List<String> unitsList = configurationService.getSanitizedList("listaUnidades");
        List<SelectOption> unitOptions = new ArrayList<>();
        for (String u : unitsList) {
            unitOptions.add(new SelectOption(u, u, u.equals(demand.getUnit())));
        }
        model.addAttribute("opcionesUnidad", unitOptions);

        // Dynamic Urgency (reusing availability list)
        List<String> availabiltyList = configurationService.getSanitizedList("listaDisponibilidades");
        List<SelectOption> urgencyOptions = new ArrayList<>();
        for (String d : availabiltyList) {
            urgencyOptions.add(new SelectOption(d, d, d.equals(demand.getUrgency())));
        }
        model.addAttribute("opcionesUrgencia", urgencyOptions);

        // Dynamic Select Options for status
        List<SelectOption> statusOptions = new ArrayList<>();
        statusOptions.add(new SelectOption("ACTIVA", "ACTIVA", DemandStatus.ACTIVE.equals(demand.getStatus())));
        statusOptions.add(new SelectOption("CERRADA", "CERRADA", DemandStatus.CLOSED.equals(demand.getStatus())));
        model.addAttribute("opcionesEstado", statusOptions);
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/demandas/{id}/editar")
    public String showEditDemandForm(@PathVariable Long id, Model model, Principal principal) {
        Demand demand = verifyDemandOwnership(id, principal);
        model.addAttribute("demanda", demand);
        model.addAttribute("isDashboard", true);

        loadSelectOptions(model, demand);

        return "editar_solicitud";
    }

    // -------------------------------------------------------------------------
    // POST /demanda/editar/{id} — Save changes with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/editar")
    public String saveEditedDemand(@PathVariable Long id,
            @Valid @ModelAttribute("demanda") Demand demandForm,
            BindingResult result,
            Model model,
            Principal principal) {

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Demand existingDemand = verifyDemandOwnership(id, principal);

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            loadSelectOptions(model, demandForm);

            model.addAttribute("errores", result.getAllErrors());
            demandForm.setId(id);
            return "editar_solicitud";
        }

        existingDemand.setTitle(demandForm.getTitle());
        existingDemand.setDescription(demandForm.getDescription());
        existingDemand.setMaterialCategory(demandForm.getMaterialCategory());
        existingDemand.setQuantity(demandForm.getQuantity());
        existingDemand.setUnit(demandForm.getUnit());
        existingDemand.setUrgency(demandForm.getUrgency());
        existingDemand.setMaxBudget(demandForm.getMaxBudget());
        existingDemand.setValidity(demandForm.getValidity());
        existingDemand.setPickupZone(demandForm.getPickupZone());
        existingDemand.setStatus(demandForm.getStatus());

        demandService.save(existingDemand);

        return "redirect:/dashboard/mis-demandas";
    }
}
