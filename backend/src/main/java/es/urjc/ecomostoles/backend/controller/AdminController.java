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
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;

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

    public AdminController(EmpresaService empresaService,
                           OfertaService ofertaService,
                           DemandaService demandaService,
                           AcuerdoService acuerdoService,
                           ConfiguracionService configuracionService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
        this.configuracionService = configuracionService;
    }

    // ... (keep private methods)

    // ── Helper: puts company + global KPIs in the model ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            Optional<Empresa> opt = empresaService.buscarPorEmail(principal.getName());
            opt.ifPresent(e -> model.addAttribute("empresa", e));
        }
        // Global platform KPIs
        model.addAttribute("totalUsuarios",  empresaService.contarTodas());
        model.addAttribute("totalOfertas",   ofertaService.contarTodas());
        model.addAttribute("totalDemandas",  demandaService.contarTodas());
        model.addAttribute("totalAcuerdos",  acuerdoService.contarTodos());
 
        // Admin-Specific Stats (Real DB counts)
        model.addAttribute("totalPendientes",  acuerdoService.contarPorEstado(EstadoAcuerdo.PENDIENTE));
        model.addAttribute("totalDenunciadas", ofertaService.contarPorEstado(EstadoOferta.DENUNCIADA));
        model.addAttribute("totalCompletadas", acuerdoService.contarPorEstado(EstadoAcuerdo.COMPLETADO));
        model.addAttribute("toneladasCO2", acuerdoService.calcularCO2Ahorrado());
    }

    // ── GET /admin  →  redirects to /admin/panel ───────────────────────────
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/panel";
    }

    // ── GET /admin/panel ───────────────────────────────────────────────────────
    @GetMapping("/panel")
    public String panel(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navPanel", true);
        return "admin_panel";
    }

    // ── GET /admin/usuarios ────────────────────────────────────────────────────
    @GetMapping("/usuarios")
    public String usuarios(Model model, Principal principal, @RequestParam(required = false) String search) {
        addCommonAttributes(model, principal);
        model.addAttribute("navUsuarios", true);
        
        if (search != null && !search.isEmpty()) {
            model.addAttribute("empresas", empresaService.filtrarEmpresas(search));
        } else {
            // Limited list of registered companies for the table (Top 50)
            model.addAttribute("empresas", empresaService.obtenerTodas());
        }
        
        return "admin_usuarios";
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal, @RequestParam(required = false) String estado) {
        addCommonAttributes(model, principal);
        model.addAttribute("navOfertas", true);
        
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
        model.addAttribute("navReportes", true);

        // Fetch companies and enrich with CO2 stats
        List<EmpresaDTO> empresas = empresaService.obtenerTodas().stream().map(e -> {
            EmpresaDTO dto = new EmpresaDTO(e);
            dto.setCo2Ahorrado(acuerdoService.calcularCO2AhorradoPorEmpresa(e.getId()));
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("empresas", empresas);
        return "admin_reportes";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navConfiguracion", true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("modoMantenimiento", "true".equals(configuracionService.obtenerValorConfiguracion("modoMantenimiento", "false")));
        configMap.put("emailContacto", configuracionService.obtenerValorConfiguracion("emailContacto", "info@ecomostoles.com"));
        configMap.put("comisionPlataforma", configuracionService.obtenerValorConfiguracion("comisionPlataforma", "2.5"));
        configMap.put("listaCategorias", configuracionService.obtenerValorConfiguracion("listaCategorias", ""));

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
     * Exports all offers as a CSV file.
     */
    @GetMapping("/exportar/csv")
    public void exportarCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"ofertas_reporte.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID;Titulo;Empresa;Cantidad;Estado");

        ofertaService.obtenerTodas().forEach(o -> {
            writer.println(String.format("%d;%s;%s;%s;%s",
                    o.getId(),
                    o.getTitulo(),
                    o.getEmpresa() != null ? o.getEmpresa().getNombreComercial() : "N/A",
                    o.getCantidad() != null ? o.getCantidad().toString() : "0",
                    o.getEstado() != null ? o.getEstado().toString() : "N/A"
            ));
        });

        writer.flush();
        writer.close();
    }
}
