package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

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
        return "admin_reportes";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navConfiguracion", true);
        return "admin_configuracion";
    }
}
