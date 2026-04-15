package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
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
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.service.ReportService;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public AdminController(EmpresaService empresaService,
                           OfertaService ofertaService,
                           DemandaService demandaService,
                           AcuerdoService acuerdoService,
                           ConfiguracionService configuracionService,
                           ReportService reportService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
        this.configuracionService = configuracionService;
        this.reportService = reportService;
    }

    // ... (keep private methods)

    // ── Helper: puts company + global KPIs in the model ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        addCommonAttributes(model, principal, null);
    }

    private void addCommonAttributes(Model model, Principal principal, String filtro) {
        if (principal != null) {
            Optional<Empresa> opt = empresaService.buscarPorEmail(principal.getName());
            opt.ifPresent(e -> model.addAttribute("empresa", e));
        }
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
                           @RequestParam(defaultValue = "0") int page) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeUsuarios", true);
        
        if (search != null && !search.isEmpty()) {
            model.addAttribute("empresas", empresaService.filtrarEmpresas(search));
            model.addAttribute("isSearch", true);
        } else {
            Page<Empresa> paginaEmpresas = empresaService.obtenerEmpresasPaginadas(page, 50);
            model.addAttribute("empresas", paginaEmpresas.getContent());
            model.addAttribute("hasPrevious", paginaEmpresas.hasPrevious());
            model.addAttribute("hasNext", paginaEmpresas.hasNext());
            model.addAttribute("currentPage", paginaEmpresas.getNumber());
            model.addAttribute("currentPagePlusOne", paginaEmpresas.getNumber() + 1);
            model.addAttribute("nextPage", paginaEmpresas.getNumber() + 1);
            model.addAttribute("previousPage", paginaEmpresas.getNumber() - 1);
            model.addAttribute("totalPages", paginaEmpresas.getTotalPages());
        }
        
        return "admin_usuarios";
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        empresaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "La empresa y todos sus datos han sido eliminados permanentemente por el administrador.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable Long id) {
        // We reuse the existing profile view with ID path, which we recently refactored to support admin inspections/edits
        return "redirect:/perfil/" + id;
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal, @RequestParam(required = false) String estado) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeOfertas", true);
        
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoOferta enumEstado = EstadoOferta.valueOf(estado.toUpperCase());
                model.addAttribute("todasOfertas", ofertaService.obtenerPorEstado(enumEstado));
                model.addAttribute("filtroSeleccionado", estado);
            } catch (IllegalArgumentException e) {
                model.addAttribute("todasOfertas", ofertaService.obtenerTodas());
            }
        } else {
            model.addAttribute("todasOfertas", ofertaService.obtenerTodas());
        }
        
        return "admin_ofertas";
    }

    // ── GET /admin/reportes ────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeReportes", true);

        // Fetch companies and enrich with CO2 stats
        List<EmpresaDTO> empresas = empresaService.obtenerTodas().stream().map(e -> {
            EmpresaDTO dto = new EmpresaDTO(e);
            dto.setCo2Ahorrado(acuerdoService.calcularCO2AhorradoPorEmpresa(e.getId()));
            return dto;
        }).sorted((a, b) -> b.getCo2Ahorrado().compareTo(a.getCo2Ahorrado())) // Order by CO2 descending
          .collect(Collectors.toList());

        // Pre-calculating indices for Mustache rendering (Strongly Typed index)
        for (int i = 0; i < empresas.size(); i++) {
            empresas.get(i).setRanking(i + 1);
        }

        model.addAttribute("empresas", empresas);
        return "admin_reportes";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("activeConfig", true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("modoMantenimiento", "true".equals(configuracionService.obtenerValorAuto("modoMantenimiento")));
        configMap.put("emailContacto", configuracionService.obtenerValorAuto("emailContacto"));
        configMap.put("comisionPlataforma", configuracionService.obtenerValorAuto("comisionPlataforma"));
        configMap.put("listaCategorias", configuracionService.obtenerValorAuto("listaCategorias"));

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
 
    /**
     * Saves global platform configuration.
     */
    @PostMapping("/configuracion")
    public String guardarConfiguracion(@RequestParam(required = false) String modoMantenimiento,
                                       @RequestParam(required = false) String emailContacto,
                                       @RequestParam(required = false) Double comisionPlataforma,
                                       @RequestParam(required = false) String listaCategorias,
                                       RedirectAttributes redirectAttributes) {
        
        configuracionService.guardarOActualizarConfiguracion("modoMantenimiento", modoMantenimiento != null ? "true" : "false");
        configuracionService.guardarOActualizarConfiguracion("emailContacto", emailContacto);
        configuracionService.guardarOActualizarConfiguracion("comisionPlataforma", String.valueOf(comisionPlataforma));
        configuracionService.guardarOActualizarConfiguracion("listaCategorias", listaCategorias);

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

    @GetMapping("/ajustes")
    public String ajustes(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navAjustes", true);
        model.addAttribute("emailActual", principal.getName());
        return "admin_ajustes";
    }

    @PostMapping("/ajustes")
    public String guardarAjustes(@RequestParam String emailAdmin, 
                                 Principal principal, 
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        Optional<Empresa> adminOpt = empresaService.buscarPorEmail(principal.getName());
        if (adminOpt.isPresent()) {
            Empresa admin = adminOpt.get();
            admin.setEmailContacto(emailAdmin);
            empresaService.guardar(admin);
            
            // SECURITY PROTOCOL: Invalidate current session after identity change
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            
            return "redirect:/login?emailActualizado=true";
        }
        return "redirect:/admin/panel";
    }
}
