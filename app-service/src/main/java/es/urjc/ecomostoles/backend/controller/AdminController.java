package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.service.ReportService;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;
import es.urjc.ecomostoles.backend.utils.NumberFormatter;

/**
 * Master controller for all back-office administration workflows.
 * 
 * Orchestrates entity curation, platform configuration, usage auditing, and
 * report synthesis. Architecturally enforced by strict method-level security
 * interceptors
 * (@PreAuthorize) mapped against the ADMIN role context to guarantee zero-trust
 * boundary protection.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    private final CompanyService companyService;
    private final OfferService offerService;
    private final DemandService demandService;
    private final AgreementService agreementService;
    private final ConfigurationService configurationService;
    private final ReportService reportService;

    public AdminController(CompanyService companyService,
            OfferService offerService,
            DemandService demandService,
            AgreementService agreementService,
            ConfigurationService configurationService,
            ReportService reportService) {
        this.companyService = companyService;
        this.offerService = offerService;
        this.demandService = demandService;
        this.agreementService = agreementService;
        this.configurationService = configurationService;
        this.reportService = reportService;
    }

    // ... (keep private methods)

    // ── Helper: puts company + global KPIs in the model ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        addCommonAttributes(model, principal, null);
    }

    private void addCommonAttributes(Model model, Principal principal, String filter) {
        // Global platform KPIs
        model.addAttribute("totalUsers", companyService.countAll());
        model.addAttribute("totalOffers", offerService.countAll());
        model.addAttribute("totalDemands", demandService.countAll());
        model.addAttribute("totalAgreements", agreementService.countAll(filter));

        // Admin-Specific Stats (Real DB counts)
        model.addAttribute("totalPending", agreementService.countByStatus(AgreementStatus.PENDING, filter));
        model.addAttribute("totalReported", offerService.countByStatus(OfferStatus.REPORTED));
        model.addAttribute("totalCompleted", agreementService.countByStatus(AgreementStatus.COMPLETED, filter));
        model.addAttribute("co2Tons", NumberFormatter.format(agreementService.calculateCO2Saved()));
        model.addAttribute("totalCommission", NumberFormatter.formatCurrency(agreementService.getTotalCommission()));
        model.addAttribute("isAdmin", true);
        model.addAttribute("isDashboard", true);
        model.addAttribute("isAdminView", true);
    }

    /**
     * Root interceptor for legacy or bare /admin requests. Restores proper routing
     * topology.
     * 
     * @return redirection dispatch string to the active administration panel.
     */
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/panel";
    }

    /**
     * Aggregates platform-wide Key Performance Indicators (KPIs).
     * 
     * @param model     MVC model map to embed statistical attributes.
     * @param principal active authenticated security principal.
     * @param filter    optional UI modifier to calculate subsets (e.g., this
     *                  month's stats).
     * @return mapping pointing to the main administrator hub template.
     */
    @GetMapping("/panel")
    public String panel(Model model, Principal principal, @RequestParam(required = false) String filter) {
        addCommonAttributes(model, principal, filter);
        model.addAttribute("activePanel", true);
        model.addAttribute("currentFilter", filter);

        // Dynamic dates and labels for the view
        model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("pendingLabel", "Pendientes");
        model.addAttribute("completedLabel", "Completadas");

        return "admin_panel";
    }

    /**
     * Returns a paginated dataset modeling the entire registered entity directory.
     * 
     * @param model     template layout schema block.
     * @param principal active session identifier.
     * @param search    conditional string targeting fuzzy substring matching across
     *                  domain identities.
     * @param pageable  resolved offset/limit rules managed directly by Spring Data
     *                  capabilities.
     * @return fully rendered user-list fragment or view.
     */
    @GetMapping("/usuarios")
    public String users(Model model, Principal principal,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeUsers", true);

        Page<Company> companiesPage;
        if (search != null && !search.isEmpty()) {
            companiesPage = companyService.searchClientsPaginated(search, pageable);
            model.addAttribute("search", search);
            model.addAttribute("isSearch", true);
            log.info("[Admin] Directory -> Executing client search for keyword: '{}'", search);
        } else {
            companiesPage = companyService.getClientsPaginated(pageable);
            model.addAttribute("isSearch", false);
            log.debug("[Admin] Directory -> Loading paginated client directory (Page: {})", pageable.getPageNumber());
        }

        // Security: Inject current user for frontend validations
        if (principal != null) {
            companyService.findByEmail(principal.getName()).ifPresent(c -> model.addAttribute("currentUser", c));
        }

        model.addAttribute("companies", companiesPage.getContent());

        // Pagination metadata
        model.addAttribute("currentPage", companiesPage.getNumber() + 1);
        model.addAttribute("totalPages", companiesPage.getTotalPages() == 0 ? 1 : companiesPage.getTotalPages());
        model.addAttribute("hasPrevious", companiesPage.hasPrevious());
        model.addAttribute("hasNext", companiesPage.hasNext());
        model.addAttribute("prevPage", companiesPage.getNumber() - 1);
        model.addAttribute("nextPage", companiesPage.getNumber() + 1);
        model.addAttribute("totalItems", companiesPage.getTotalElements());

        // Base URL and query string for pagination
        model.addAttribute("paginationBaseUrl", "/admin/usuarios");
        String qs = (search != null && !search.isEmpty()) ? "&search=" + search : "";
        model.addAttribute("paginationQueryString", qs);

        return "admin_usuarios";
    }

    /**
     * Invokes destructive operational routines on targeted company objects.
     * Includes defensive checks
     * against self-deletion or altering equivalent security scopes (other admins).
     * 
     * @param id                 remote unique sequence corresponding to the
     *                           scheduled company record.
     * @param principal          current authority verifying authorization
     *                           boundaries are not exceeded.
     * @param redirectAttributes decoupled carrier for post-redirect flash feedback
     *                           interactions.
     * @return logical redirect URI enforcing the post/redirect/get paradigm.
     */
    @PostMapping("/usuarios/eliminar/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Optional<Company> userToDelete = companyService.findById(id);

        if (userToDelete.isPresent()) {
            Company target = userToDelete.get();

            // Safety: Prevent self-deletion or deletion of other admins
            if (target.getRoles().contains("ADMIN")) {
                log.warn("[Admin] Security -> Blocked attempt by '{}' to delete another admin ID: {}", principal.getName(), id);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Operación denegada: No puedes eliminar a un administrador.");
                return "redirect:/admin/usuarios";
            }

            if (principal != null && target.getContactEmail().equals(principal.getName())) {
                log.warn("[Admin] Security -> Blocked self-deletion attempt for ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Operación denegada: No puedes eliminar tu propia cuenta.");
                return "redirect:/admin/usuarios";
            }

            companyService.delete(id);
            log.info("[Admin] Success -> Identity ID: {} removed by admin '{}'", id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Empresa eliminada con éxito.");
        } else {
            log.warn("[Admin] Failed -> Attempted to delete non-existing company ID: {}", id);
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editUser(@PathVariable Long id) {
        // We reuse the existing profile view with ID path, which we recently refactored
        // to support admin inspections/edits
        return "redirect:/perfil/" + id;
    }

    // ── GET /admin/offers ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String offers(Model model, Principal principal,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "publicationDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeOffers", true);

        Page<OfferSummary> offersPage;
        if (status != null && !status.isEmpty()) {
            try {
                OfferStatus enumStatus = OfferStatus.valueOf(status.toUpperCase());
                offersPage = offerService.getByStatusPaginated(enumStatus, pageable);
                model.addAttribute("selectedFilter", status);
            } catch (IllegalArgumentException e) {
                offersPage = offerService.getAllPaginated(pageable);
            }
        } else {
            offersPage = offerService.getAllPaginated(pageable);
        }

        model.addAttribute("offers", offersPage.getContent());

        // Pagination metadata
        model.addAttribute("currentPage", offersPage.getNumber() + 1);
        model.addAttribute("totalPages", offersPage.getTotalPages() == 0 ? 1 : offersPage.getTotalPages());
        model.addAttribute("hasPrevious", offersPage.hasPrevious());
        model.addAttribute("hasNext", offersPage.hasNext());
        model.addAttribute("prevPage", offersPage.getNumber() - 1);
        model.addAttribute("nextPage", offersPage.getNumber() + 1);
        model.addAttribute("totalItems", offersPage.getTotalElements());

        // Fix: Persist state filter in pagination
        model.addAttribute("paginationBaseUrl", "/admin/ofertas");
        String qs = (status != null && !status.isEmpty()) ? "&status=" + status : "";
        model.addAttribute("paginationQueryString", qs);

        // Real metrics for KPI cards
        model.addAttribute("countTotal", offerService.countAll());
        model.addAttribute("countActive", offerService.countByStatus(OfferStatus.ACTIVE));
        model.addAttribute("countPaused", offerService.countByStatus(OfferStatus.PAUSED));
        model.addAttribute("countFinished", offerService.countByStatus(OfferStatus.FINISHED));

        return "admin_ofertas";
    }

    // ── GET /admin/reportes ────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reports(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeReports", true);

        // Enterprise Plus: Batch-fetch all CO2 stats to avoid N+1 query pattern
        Map<Long, Double> co2Map = agreementService.getCO2Ranking();

        // Fetch companies and enrich with pre-calculated CO2 stats
        List<CompanyDTO> topCompanies = companyService.getAll().stream().map(e -> {
            CompanyDTO dto = new CompanyDTO(e);
            dto.setCo2Saved(co2Map.getOrDefault(e.getId(), 0.0));
            // Ensure sector is meaningful
            dto.setSector(e.getIndustrialSector() != null ? e.getIndustrialSector() : "Industria");
            return dto;
        }).sorted((a, b) -> b.getCo2Saved().compareTo(a.getCo2Saved())) // Order by CO2 descending
                .collect(Collectors.toList());

        // Pre-calculating indices for Mustache rendering
        for (int i = 0; i < topCompanies.size(); i++) {
            topCompanies.get(i).setRanking(i + 1);
        }

        model.addAttribute("topCompanies", topCompanies);

        // EXTRA: Data for the Admin Chart (Top 5 companies by impact)
        List<String> labels = topCompanies.stream().limit(5)
                .map(es.urjc.ecomostoles.backend.dto.CompanyDTO::getCommercialName)
                .collect(java.util.stream.Collectors.toList());
        List<Double> data = topCompanies.stream().limit(5)
                .map(es.urjc.ecomostoles.backend.dto.CompanyDTO::getCo2Saved)
                .collect(java.util.stream.Collectors.toList());

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            model.addAttribute("chartLabels", mapper.writeValueAsString(labels));
            model.addAttribute("chartData", mapper.writeValueAsString(data));
        } catch (Exception e) {
            model.addAttribute("chartLabels", "[]");
            model.addAttribute("chartData", "[]");
        }

        // Consolidate metrics into a single object as requested
        Map<String, Object> reportMetrics = new HashMap<>();
        reportMetrics.put("co2Saved", model.getAttribute("co2Tons"));
        reportMetrics.put("transactions", model.getAttribute("totalAgreements"));
        reportMetrics.put("activeCompanies", model.getAttribute("totalUsers"));

        model.addAttribute("reportMetrics", reportMetrics);

        return "admin_reportes";
    }

    @GetMapping("/demandas")
    public String adminDemands(Model model, Principal principal,
            @PageableDefault(size = 10, sort = "publicationDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeDemands", true);

        Page<Demand> demandsPage = demandService.getAllPaginated(pageable);

        model.addAttribute("demands", demandsPage.getContent());

        // Pagination metadata
        model.addAttribute("currentPage", demandsPage.getNumber() + 1);
        model.addAttribute("totalPages", demandsPage.getTotalPages() == 0 ? 1 : demandsPage.getTotalPages());
        model.addAttribute("hasPrevious", demandsPage.hasPrevious());
        model.addAttribute("hasNext", demandsPage.hasNext());
        model.addAttribute("prevPage", demandsPage.getNumber() - 1);
        model.addAttribute("nextPage", demandsPage.getNumber() + 1);
        model.addAttribute("totalItems", demandsPage.getTotalElements());

        // Dynamic stats for admin_demandas cards
        model.addAttribute("totalDemands", demandService.countAll());
        model.addAttribute("totalActiveDemands",
                demandService.countByStatus(es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE));
        model.addAttribute("totalPausedDemands",
                demandService.countByStatus(es.urjc.ecomostoles.backend.model.DemandStatus.PAUSED));
        model.addAttribute("totalClosedDemands",
                demandService.countByStatus(es.urjc.ecomostoles.backend.model.DemandStatus.CLOSED));

        // Pagination meta
        model.addAttribute("paginationBaseUrl", "/admin/demandas");
        model.addAttribute("paginationQueryString", "");

        return "admin_demandas";
    }

    @GetMapping("/acuerdos")
    public String adminAgreements(Model model, Principal principal,
            @PageableDefault(size = 10, sort = "registrationDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeAgreements", true);

        Page<Agreement> agreementsPage = agreementService.getAllPaginated(pageable);

        model.addAttribute("agreements", agreementsPage.getContent());

        // Pagination metadata
        model.addAttribute("currentPage", agreementsPage.getNumber() + 1);
        model.addAttribute("totalPages", agreementsPage.getTotalPages() == 0 ? 1 : agreementsPage.getTotalPages());
        model.addAttribute("hasPrevious", agreementsPage.hasPrevious());
        model.addAttribute("hasNext", agreementsPage.hasNext());
        model.addAttribute("prevPage", agreementsPage.getNumber() - 1);
        model.addAttribute("nextPage", agreementsPage.getNumber() + 1);
        model.addAttribute("totalItems", agreementsPage.getTotalElements());

        // Dynamic stats for admin_acuerdos cards
        model.addAttribute("totalAgreements", agreementService.countAll());
        model.addAttribute("totalCompleted",
                agreementService.countByStatus(es.urjc.ecomostoles.backend.model.AgreementStatus.COMPLETED));

        // Pagination meta
        model.addAttribute("paginationBaseUrl", "/admin/acuerdos");
        model.addAttribute("paginationQueryString", "");

        return "admin_acuerdos";
    }

    @GetMapping("/plataforma")
    public String adminPlatform() {
        return "redirect:/admin/configuracion";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuration(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeConfig", true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("contactEmail", configurationService.getAutoValue("contactEmail"));
        configMap.put("platformCommission", configurationService.getAutoValue("platformCommission"));
        configMap.put("categoryList", configurationService.getAutoValue("categoryList"));
        configMap.put("unitList", configurationService.getAutoValue("unitList"));
        configMap.put("availabilityList", configurationService.getAutoValue("availabilityList"));
        configMap.put("sectorList", configurationService.getAutoValue("sectorList"));

        model.addAttribute("config", configMap);
        return "admin_configuracion";
    }

    /**
     * Deletes an offer from the administrative panel.
     */
    @PostMapping("/ofertas/{id}/eliminar")
    public String deleteOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("[Admin] Action -> Deleting offer ID: {} via administrative override.", id);
        offerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "La oferta ha sido eliminada correctamente por el administrador.");
        return "redirect:/admin/ofertas";
    }

    @GetMapping("/ofertas/ver/{id}")
    public String viewOfferAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Offer offer = offerService.findById(id).orElse(null);
        if (offer == null) {
            return "redirect:/admin/ofertas";
        }
        model.addAttribute("offer", offer);
        return "detalle_activo";
    }

    @GetMapping("/ofertas/editar/{id}")
    public String editOfferAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Offer offer = offerService.findById(id).orElse(null);
        if (offer == null)
            return "redirect:/admin/ofertas";

        model.addAttribute("offer", offer);

        // Inject select options for the form using helper
        injectFormOptions(model, offer.getUnit(), offer.getAvailability(), null, offer.getWasteCategory(),
                offer.getStatus());

        return "editar_activo";
    }

    @PostMapping("/demandas/eliminar/{id}")
    public String deleteDemandAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("[Admin] Action -> Deleting demand ID: {} via administrative override.", id);
        demandService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Demanda eliminada con éxito por el administrador.");
        return "redirect:/admin/demandas";
    }

    @GetMapping("/demandas/ver/{id}")
    public String viewDemandAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Demand demand = demandService.findById(id).orElse(null);
        if (demand == null) {
            return "redirect:/admin/demandas";
        }
        model.addAttribute("demand", demand);
        return "detalle_solicitud";
    }

    @GetMapping("/demandas/editar/{id}")
    public String editDemandAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Demand demand = demandService.findById(id).orElse(null);
        if (demand == null)
            return "redirect:/admin/demandas";

        model.addAttribute("demand", demand);

        // Inject select options for the form using helper
        injectFormOptions(model, demand.getUnit(), demand.getUrgency(), demand.getValidity(), demand.getWasteCategory(),
                demand.getStatus());

        return "editar_solicitud";
    }

    @PostMapping("/acuerdos/eliminar/{id}")
    public String deleteAgreementAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("[Admin] Action -> Deleting agreement ID: {} via administrative override.", id);
        agreementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Acuerdo eliminado con éxito por el administrador.");
        return "redirect:/admin/acuerdos";
    }

    @GetMapping("/acuerdos/ver/{id}")
    public String viewAgreementAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Agreement agreement = agreementService.findById(id).orElse(null);
        if (agreement == null) {
            return "redirect:/admin/acuerdos";
        }
        model.addAttribute("agreement", agreement);
        model.addAttribute("isAdminView", true);
        return "detalle_acuerdo";
    }

    @GetMapping("/acuerdos/editar/{id}")
    public String editAgreementAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Agreement agreement = agreementService.findById(id).orElse(null);
        if (agreement == null)
            return "redirect:/admin/acuerdos";

        model.addAttribute("agreement", agreement);

        // Centralized utility for form options (DRY)
        model.addAttribute("unitOptions",
                FormOptionsHelper.getUnitOptions(configurationService, agreement.getUnit()));
        model.addAttribute("statusOptions", FormOptionsHelper.getAgreementStatusOptions(agreement.getStatus()));

        return "editar_acuerdo";
    }

    @GetMapping("/usuarios/ver/{id}")
    public String viewUserAdmin(@PathVariable Long id) {
        return "redirect:/perfil/" + id;
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editUserAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Optional<Company> companyOpt = companyService.findById(id);

        if (companyOpt.isPresent()) {
            model.addAttribute("company", companyOpt.get());
        }

        model.addAttribute("supportEmail", "soporte@ecomostoles.es");

        // Prepare sectors list with 'selected' status for UI
        String currentSector = companyOpt.isPresent() ? companyOpt.get().getIndustrialSector() : "";
        List<String> sectors = configurationService.getSanitizedList("sectorList");

        List<Map<String, Object>> sectorsList = sectors.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", s);
            map.put("displayName", s);
            map.put("selected", s.equals(currentSector));
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("sectorList", sectorsList);

        return "perfil_empresa";
    }

    /**
     * Saves global platform configuration.
     */
    @PostMapping("/configuracion")
    public String saveConfiguration(@RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) Double platformCommission,
            @RequestParam(required = false) String categoryList,
            @RequestParam(required = false) String unitList,
            @RequestParam(required = false) String availabilityList,
            @RequestParam(required = false) String sectorList,
            RedirectAttributes redirectAttributes) {

        log.info("[Admin] Configuration -> Saving global platform preferences. Commission: {}%", platformCommission);

        // Validation: range 0-100% for business commissions (UX/Integrity)
        if (platformCommission != null && (platformCommission < 0 || platformCommission > 100)) {
            redirectAttributes.addFlashAttribute("error", "La comisión debe estar entre 0 y 100%.");
            return "redirect:/admin/configuracion";
        }

        configurationService.saveOrUpdateConfiguration("contactEmail", contactEmail);
        configurationService.saveOrUpdateConfiguration("platformCommission", String.valueOf(platformCommission));
        configurationService.saveOrUpdateConfiguration("categoryList", categoryList);
        configurationService.saveOrUpdateConfiguration("unitList", unitList);
        configurationService.saveOrUpdateConfiguration("availabilityList", availabilityList);
        configurationService.saveOrUpdateConfiguration("sectorList", sectorList);

        redirectAttributes.addFlashAttribute("message",
                "La configuración de la plataforma se ha actualizado correctamente en la base de datos.");
        return "redirect:/admin/configuracion";
    }

    /**
     * Exports registered companies as a CSV file.
     * Note: PDF generation for agreements has been migrated to utility-service.
     * This endpoint now returns CSV, which requires no external library.
     */
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        log.info("[Admin] Report -> Generating CSV Export for all companies.");
        byte[] csv = reportService.generateUsersCsv(companyService.getAll());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * Exports all offers as a CSV file.
     */
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csv = reportService.generateOffersCsv(offerService.getAll());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_ofertas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * Helper to inject common select options into the model for administrative
     * forms.
     */
    private void injectFormOptions(Model model, String unit, String availability, String validity, Object cat,
            Enum<?> state) {
        model.addAttribute("unitOptions", FormOptionsHelper.getUnitOptions(configurationService, unit));
        model.addAttribute("availabilityOptions",
                FormOptionsHelper.getUrgencyOptions(configurationService, availability));

        if (validity != null || "".equals(validity)) {
            model.addAttribute("validityOptions", FormOptionsHelper.getValidityOptions(validity));
        }

        model.addAttribute("wasteCategories", FormOptionsHelper.getCategoryOptions(configurationService, cat));

        if (state != null) {
            List<SelectOption> options = new ArrayList<>();
            for (Object obj : state.getClass().getEnumConstants()) {
                Enum<?> constant = (Enum<?>) obj;
                String label = constant.name();
                try {
                    // Use reflection to get localized name if available (OfferStatus, DemandStatus,
                    // etc)
                    label = (String) constant.getClass().getMethod("getDisplayName").invoke(constant);
                } catch (Exception e) {
                    // Fallback to name
                }
                options.add(new SelectOption(constant.name(), label, constant.equals(state)));
            }
            model.addAttribute("statusOptions", options);
        }
    }

}
