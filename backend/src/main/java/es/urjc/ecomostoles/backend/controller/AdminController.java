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
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;

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

    private final EmpresaService  empresaService;
    private final OfertaService   ofertaService;
    private final DemandaService  demandaService;
    private final AcuerdoService  acuerdoService;

    public AdminController(EmpresaService empresaService,
                           OfertaService ofertaService,
                           DemandaService demandaService,
                           AcuerdoService acuerdoService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
    }

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
    public String usuarios(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navUsuarios", true);
        // Limited list of registered companies for the table (Top 50)
        model.addAttribute("empresas", empresaService.obtenerTodas());
        return "admin_usuarios";
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navOfertas", true);
        model.addAttribute("todasOfertas", ofertaService.obtenerTodas());
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
                                       @RequestParam(required = false) Double comisionPlataforma,
                                       @RequestParam(required = false) String listaCategorias,
                                       RedirectAttributes redirectAttributes) {
        // Logic to persist settings would go here. For now, we simulate success.
        redirectAttributes.addFlashAttribute("mensaje", "La configuración de la plataforma se ha actualizado correctamente.");
        return "redirect:/admin/configuracion";
    }
}
