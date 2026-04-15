package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
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
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.service.ReportService;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import es.urjc.ecomostoles.backend.service.MensajeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;

/**
 * Administration panel controller.
 *
 * All paths are under /admin/** and protected by @PreAuthorize("hasRole('ADMIN')")
 * (additional reinforcement over the SecurityConfig rule).
 *
 * Used views (existing):
 *   admin_panel, admin_usuarios, admin_ofertas, admin_reportes, admin_configuracion
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmpresaService       empresaService;
    private final OfertaService        ofertaService;
    private final DemandaService       demandaService;
    private final AcuerdoService       acuerdoService;
    private final ConfiguracionService configuracionService;
    private final ReportService       reportService;
    private final MensajeService     mensajeService;

    public AdminController(EmpresaService empresaService,
                           OfertaService ofertaService,
                           DemandaService demandaService,
                           AcuerdoService acuerdoService,
                           ConfiguracionService configuracionService,
                           ReportService reportService,
                           MensajeService mensajeService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
        this.configuracionService = configuracionService;
        this.reportService = reportService;
        this.mensajeService = mensajeService;
    }

    // ... (keep private methods)

    // ── Helper: puts company + global KPIs in the model ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        addCommonAttributes(model, principal, null);
    }

    private void addCommonAttributes(Model model, Principal principal, String filtro) {
        // Global platform KPIs
        model.addAttribute("totalUsuarios",  empresaService.contarTodas());
        model.addAttribute("totalOfertas",   ofertaService.contarTodas());
        model.addAttribute("totalDemandas",  demandaService.contarTodas());
        model.addAttribute("totalAcuerdos",  acuerdoService.contarTodos(filtro));
 
        // Admin-Specific Stats (Real DB counts)
        model.addAttribute("totalPendientes",  acuerdoService.contarPorEstado(EstadoAcuerdo.PENDIENTE, filtro));
        model.addAttribute("totalDenunciadas", ofertaService.contarPorEstado(EstadoOferta.DENUNCIADA));
        model.addAttribute("totalCompletadas", acuerdoService.contarPorEstado(EstadoAcuerdo.COMPLETADO, filtro));
        model.addAttribute("toneladasCO2", acuerdoService.calcularCO2Ahorrado());
        model.addAttribute("isDashboard", true);
        model.addAttribute("esVistaAdmin", true);
        model.addAttribute("emailSoporte", "soporte@ecomostoles.es");
    }

    // ── GET /admin  →  redirects to /admin/panel ───────────────────────────
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/panel";
    }

    // ── GET /admin/panel ───────────────────────────────────────────────────────
    @GetMapping("/panel")
    public String panel(Model model, Principal principal, @RequestParam(required = false) String filtro) {
        addCommonAttributes(model, principal, filtro);
        model.addAttribute("activePanel", true);
        model.addAttribute("filtroActual", filtro);
        
        // Dynamic dates and labels for the view
        model.addAttribute("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("labelPendientes", "Pendientes");
        model.addAttribute("labelCompletadas", "Completadas");

        return "admin_panel";
    }

    // ── GET /admin/usuarios ────────────────────────────────────────────────────
    @GetMapping("/usuarios")
    public String usuarios(Model model, Principal principal, 
                           @RequestParam(required = false) String search,
                           @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeUsuarios", true);
        
        Page<Empresa> paginaEmpresas;
        if (search != null && !search.isEmpty()) {
            paginaEmpresas = empresaService.filtrarEmpresasPaginado(search, pageable);
            model.addAttribute("searchQuery", search);
            model.addAttribute("isSearch", true);
        } else {
            paginaEmpresas = empresaService.obtenerEmpresasPaginadas(pageable.getPageNumber(), pageable.getPageSize());
            model.addAttribute("isSearch", false);
        }
        
        model.addAttribute("empresas", paginaEmpresas.getContent());
        model.addAttribute("currentPage", paginaEmpresas.getNumber() + 1);
        model.addAttribute("totalPages", paginaEmpresas.getTotalPages());
        model.addAttribute("hasPrevious", paginaEmpresas.hasPrevious());
        model.addAttribute("hasNext", paginaEmpresas.hasNext());
        model.addAttribute("prevPage", paginaEmpresas.getNumber() - 1);
        model.addAttribute("nextPage", paginaEmpresas.getNumber() + 1);
        
        // Fix: Persist search parameters in pagination
        model.addAttribute("pagBaseUrl", "/admin/usuarios");
        String qs = (search != null && !search.isEmpty()) ? "&search=" + search : "";
        model.addAttribute("pagQueryString", qs);
        
        return "admin_usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        // CascadeType.ALL in Empresa.java will delete the user and all their dependencies.
        empresaService.eliminar(id);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable Long id) {
        // We reuse the existing profile view with ID path, which we recently refactored to support admin inspections/edits
        return "redirect:/perfil/" + id;
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal, 
                          @RequestParam(required = false) String estado,
                          @PageableDefault(size = 10, sort = "fechaPublicacion", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeOfertas", true);
        
        Page<OfertaResumen> paginaOfertas;
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoOferta enumEstado = EstadoOferta.valueOf(estado.toUpperCase());
                paginaOfertas = ofertaService.obtenerPorEstadoPaginado(enumEstado, pageable);
                model.addAttribute("filtroSeleccionado", estado);
            } catch (IllegalArgumentException e) {
                paginaOfertas = ofertaService.obtenerTodasPaginadas(pageable);
            }
        } else {
            paginaOfertas = ofertaService.obtenerTodasPaginadas(pageable);
        }
        
        model.addAttribute("todasOfertas", paginaOfertas.getContent());
        model.addAttribute("currentPage", paginaOfertas.getNumber() + 1);
        model.addAttribute("totalPages", paginaOfertas.getTotalPages());
        model.addAttribute("hasPrevious", paginaOfertas.hasPrevious());
        model.addAttribute("hasNext", paginaOfertas.hasNext());
        model.addAttribute("prevPage", paginaOfertas.getNumber() - 1);
        model.addAttribute("nextPage", paginaOfertas.getNumber() + 1);
        
        // Fix: Persist state filter in pagination
        model.addAttribute("pagBaseUrl", "/admin/ofertas");
        String qs = (estado != null && !estado.isEmpty()) ? "&estado=" + estado : "";
        model.addAttribute("pagQueryString", qs);
        
        return "admin_ofertas";
    }

    // ── GET /admin/reportes ────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeReportes", true);

        // Enterprise Plus: Batch-fetch all CO2 stats to avoid N+1 query pattern
        Map<Long, Double> co2Map = acuerdoService.obtenerRankingCO2();

        // Fetch companies and enrich with pre-calculated CO2 stats
        List<EmpresaDTO> topCompanies = empresaService.obtenerTodas().stream().map(e -> {
            EmpresaDTO dto = new EmpresaDTO(e);
            dto.setCo2Ahorrado(co2Map.getOrDefault(e.getId(), 0.0));
            // Ensure sector is meaningful
            dto.setSector(e.getSectorIndustrial() != null ? e.getSectorIndustrial() : "Industria");
            return dto;
        }).sorted((a, b) -> b.getCo2Ahorrado().compareTo(a.getCo2Ahorrado())) // Order by CO2 descending
          .collect(Collectors.toList());

        // Pre-calculating indices for Mustache rendering
        for (int i = 0; i < topCompanies.size(); i++) {
            topCompanies.get(i).setRanking(i + 1);
        }

        model.addAttribute("topCompanies", topCompanies);

        // EXTRA: Data for the Admin Chart (Top 5 companies by impact)
        List<String> labels = topCompanies.stream().limit(5).map(es.urjc.ecomostoles.backend.dto.EmpresaDTO::getNombreComercial).collect(java.util.stream.Collectors.toList());
        List<Double> data = topCompanies.stream().limit(5).map(es.urjc.ecomostoles.backend.dto.EmpresaDTO::getCo2Ahorrado).collect(java.util.stream.Collectors.toList());
        
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
        reportMetrics.put("co2Saved", model.getAttribute("toneladasCO2"));
        reportMetrics.put("transactions", model.getAttribute("totalAcuerdos"));
        reportMetrics.put("activeCompanies", model.getAttribute("totalUsuarios"));
        
        model.addAttribute("reportMetrics", reportMetrics);

        return "admin_reportes";
    }

    @GetMapping("/demandas")
    public String adminDemandas(Model model, Principal principal,
                                @PageableDefault(size = 10, sort = "fechaPublicacion", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeDemandas", true);
        
        Page<Demanda> paginaDemandas = demandaService.obtenerTodasPaginadas(pageable);
        
        model.addAttribute("demandas", paginaDemandas.getContent());
        model.addAttribute("currentPage", paginaDemandas.getNumber() + 1);
        model.addAttribute("totalPages", paginaDemandas.getTotalPages());
        model.addAttribute("hasPrevious", paginaDemandas.hasPrevious());
        model.addAttribute("hasNext", paginaDemandas.hasNext());
        model.addAttribute("prevPage", paginaDemandas.getNumber() - 1);
        model.addAttribute("nextPage", paginaDemandas.getNumber() + 1);
        
        // Dynamic stats for admin_demandas cards
        model.addAttribute("totalDemandasActivas", demandaService.contarTodas());
        model.addAttribute("totalInteresados", mensajeService.contarTodos());
        
        // Fix: Add base pagination meta for demands
        model.addAttribute("pagBaseUrl", "/admin/demandas");
        model.addAttribute("pagQueryString", "");
        
        return "admin_demandas";
    }

    @GetMapping("/acuerdos")
    public String adminAcuerdos(Model model, Principal principal,
                                @PageableDefault(size = 10, sort = "fechaRegistro", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeAcuerdos", true);
        
        Page<Acuerdo> paginaAcuerdos = acuerdoService.obtenerTodosPaginados(pageable);
        
        model.addAttribute("acuerdos", paginaAcuerdos.getContent());
        model.addAttribute("currentPage", paginaAcuerdos.getNumber() + 1);
        model.addAttribute("totalPages", paginaAcuerdos.getTotalPages());
        model.addAttribute("hasPrevious", paginaAcuerdos.hasPrevious());
        model.addAttribute("hasNext", paginaAcuerdos.hasNext());
        model.addAttribute("prevPage", paginaAcuerdos.getNumber() - 1);
        model.addAttribute("nextPage", paginaAcuerdos.getNumber() + 1);
        
        // Fix: Add base pagination meta for agreements
        model.addAttribute("pagBaseUrl", "/admin/acuerdos");
        model.addAttribute("pagQueryString", "");
        
        return "admin_acuerdos";
    }

    @GetMapping("/plataforma")
    public String adminPlataforma() {
        return "redirect:/admin/configuracion";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeConfig", true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("emailContacto", configuracionService.obtenerValorAuto("emailContacto"));
        configMap.put("comisionPlataforma", configuracionService.obtenerValorAuto("comisionPlataforma"));
        configMap.put("listaCategorias", configuracionService.obtenerValorAuto("listaCategorias"));
        configMap.put("listaUnidades", configuracionService.obtenerValorAuto("listaUnidades"));
        configMap.put("listaDisponibilidades", configuracionService.obtenerValorAuto("listaDisponibilidades"));
        configMap.put("listaSectores", configuracionService.obtenerValorAuto("listaSectores"));

        model.addAttribute("config", configMap);
        return "admin_configuracion";
    }
 
    /**
     * Deletes an offer from the administrative panel.
     */
    @PostMapping("/ofertas/{id}/eliminar")
    public String eliminarOferta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ofertaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "La oferta ha sido eliminada correctamente por el administrador.");
        return "redirect:/admin/ofertas";
    }

    @GetMapping("/ofertas/editar/{id}")
    public String editarOfertaAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Oferta oferta = ofertaService.buscarPorId(id).orElse(null);
        if (oferta == null) return "redirect:/admin/ofertas";
        
        // Inject select options for the form using helper
        inyectarOpcionesFormulario(model, oferta.getUnidad(), oferta.getDisponibilidad(), oferta.getTipoResiduo(), oferta.getEstado());

        return "editar_activo";
    }

    @PostMapping("/demandas/eliminar/{id}")
    public String eliminarDemandaAdmin(@PathVariable Long id) {
        demandaService.eliminar(id);
        return "redirect:/admin/demandas";
    }

    @GetMapping("/demandas/editar/{id}")
    public String editarDemandaAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Demanda demanda = demandaService.buscarPorId(id).orElse(null);
        if (demanda == null) return "redirect:/admin/demandas";

        model.addAttribute("demanda", demanda);
        
        // Inject select options for the form using helper
        inyectarOpcionesFormulario(model, demanda.getUnidad(), demanda.getUrgencia(), demanda.getCategoriaMaterial(), demanda.getEstado());

        return "editar_solicitud";
    }

    @PostMapping("/acuerdos/eliminar/{id}")
    public String eliminarAcuerdoAdmin(@PathVariable Long id) {
        acuerdoService.eliminar(id);
        return "redirect:/admin/acuerdos";
    }

    @GetMapping("/acuerdos/editar/{id}")
    public String editarAcuerdoAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Acuerdo acuerdo = acuerdoService.buscarPorId(id).orElse(null);
        if (acuerdo == null) return "redirect:/admin/acuerdos";

        model.addAttribute("acuerdo", acuerdo);
        
        // Centralized utility for form options (DRY)
        model.addAttribute("opcionesUnidad", FormOptionsHelper.getOpcionesUnidad(configuracionService, acuerdo.getUnidad()));
        model.addAttribute("opcionesEstado", FormOptionsHelper.getOpcionesEstadoAcuerdo(acuerdo.getEstado()));

        return "editar_acuerdo";
    }

    @GetMapping("/usuarios/ver/{id}")
    public String verUsuarioAdmin(@PathVariable Long id) {
        return "redirect:/perfil/" + id;
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioAdmin(@PathVariable Long id, Model model, Principal principal) {
        addCommonAttributes(model, principal);
        Optional<Empresa> empresaOpt = empresaService.buscarPorId(id);
        
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
        }

        // Variable required by profil_empresa.html to avoid context error
        model.addAttribute("emailSoporte", "soporte@ecomostoles.es");
        
        // Prepare sectors list with 'selected' status for UI
        String currentSector = empresaOpt.isPresent() ? empresaOpt.get().getSectorIndustrial() : "";
        List<String> sectores = configuracionService.obtenerListaSanitizada("listaSectores");

        List<Map<String, Object>> listaSectores = sectores.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", s);
            map.put("display", s);
            map.put("selected", s.equals(currentSector));
            return map;
        }).collect(Collectors.toList());
        
        model.addAttribute("listaSectores", listaSectores);

        return "perfil_empresa";
    }

    @GetMapping("/ofertas/ver/{id}")
    public String verOfertaAdmin(@PathVariable Long id) {
        return "redirect:/oferta/" + id;
    }

    @GetMapping("/demandas/ver/{id}")
    public String verDemandaAdmin(@PathVariable Long id) {
        return "redirect:/demanda/" + id;
    }

    @GetMapping("/acuerdos/ver/{id}")
    public String verAcuerdoAdmin(@PathVariable Long id) {
        return "redirect:/acuerdo/" + id;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    /**
     * Saves global platform configuration.
     */
    @PostMapping("/configuracion")
    public String guardarConfiguracion(@RequestParam(required = false) String emailContacto,
                                       @RequestParam(required = false) Double comisionPlataforma,
                                       @RequestParam(required = false) String listaCategorias,
                                       @RequestParam(required = false) String listaUnidades,
                                       @RequestParam(required = false) String listaDisponibilidades,
                                       @RequestParam(required = false) String listaSectores,
                                       RedirectAttributes redirectAttributes) {
        
        log.info("Intento de guardado de configuración -> Email: {}, Comisión: {}%", emailContacto, comisionPlataforma);
        
        // Validation: range 0-100% for business commissions (UX/Integrity)
        if (comisionPlataforma != null && (comisionPlataforma < 0 || comisionPlataforma > 100)) {
            redirectAttributes.addFlashAttribute("error", "La comisión debe estar entre 0 y 100%.");
            return "redirect:/admin/configuracion";
        }
        
        configuracionService.guardarOActualizarConfiguracion("emailContacto", emailContacto);
        configuracionService.guardarOActualizarConfiguracion("comisionPlataforma", String.valueOf(comisionPlataforma));
        configuracionService.guardarOActualizarConfiguracion("listaCategorias", listaCategorias);
        configuracionService.guardarOActualizarConfiguracion("listaUnidades", listaUnidades);
        configuracionService.guardarOActualizarConfiguracion("listaDisponibilidades", listaDisponibilidades);
        configuracionService.guardarOActualizarConfiguracion("listaSectores", listaSectores);

        redirectAttributes.addFlashAttribute("mensaje", "La configuración de la plataforma se ha actualizado correctamente en la base de datos.");
        return "redirect:/admin/configuracion";
    }

    /**
     * Exports a PDF report of registered companies.
     */
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf() {
        byte[] pdf = reportService.generarPdfUsuarios(empresaService.obtenerTodas());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Exports all offers as a CSV file.
     */
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv() {
        byte[] csv = reportService.generarCsvOfertas(ofertaService.obtenerTodas());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_ofertas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * Helper to inject common select options into the model for administrative forms.
     */
    private void inyectarOpcionesFormulario(Model model, String unit, String disp, String cat, Enum<?> state) {
        model.addAttribute("opcionesUnidad", buildOptions("listaUnidades", unit));
        model.addAttribute("opcionesDisponibilidad", buildOptions("listaDisponibilidades", disp));
        model.addAttribute("opcionesUrgencia", buildOptions("listaDisponibilidades", disp));
        model.addAttribute("opcionesTipo", buildOptions("listaCategorias", cat));
        model.addAttribute("opcionesCategoria", buildOptions("listaCategorias", cat));
        
        if (state != null) {
            List<SelectOption> options = new ArrayList<>();
            for (Object obj : state.getClass().getEnumConstants()) {
                Enum<?> constant = (Enum<?>) obj;
                options.add(new SelectOption(constant.name(), constant.name(), constant.equals(state)));
            }
            model.addAttribute("opcionesEstado", options);
        }
    }

    /**
     * Helper to build a list of SelectOption objects from a configuration key.
     */
    private List<SelectOption> buildOptions(String configKey, String selected) {
        List<String> items = configuracionService.obtenerListaSanitizada(configKey);
        List<SelectOption> options = new ArrayList<>();
        for (String item : items) {
            options.add(new SelectOption(item, item, item.equals(selected)));
        }
        return options;
    }
}
