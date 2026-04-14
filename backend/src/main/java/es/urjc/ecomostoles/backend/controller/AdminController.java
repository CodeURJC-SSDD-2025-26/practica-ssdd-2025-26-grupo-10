package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

/**
 * Controlador del panel de administración.
 *
 * Todas las rutas están bajo /admin/** y protegidas por @PreAuthorize("hasRole('ADMIN')")
 * (refuerzo adicional sobre la regla de SecurityConfig).
 *
 * Vistas usadas (ya existentes):
 *   admin_panel, admin_usuarios, admin_ofertas, admin_reportes, admin_configuracion
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmpresaRepository  empresaRepository;
    private final OfertaRepository   ofertaRepository;
    private final DemandaRepository  demandaRepository;
    private final AcuerdoRepository  acuerdoRepository;

    public AdminController(EmpresaRepository empresaRepository,
                           OfertaRepository ofertaRepository,
                           DemandaRepository demandaRepository,
                           AcuerdoRepository acuerdoRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository  = ofertaRepository;
        this.demandaRepository = demandaRepository;
        this.acuerdoRepository = acuerdoRepository;
    }

    // ── Helper: pone empresa + KPIs globales en el modelo ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            Optional<Empresa> opt = empresaRepository.findByEmailContacto(principal.getName());
            opt.ifPresent(e -> model.addAttribute("empresa", e));
        }
        // KPIs globales de la plataforma
        model.addAttribute("totalUsuarios",  empresaRepository.count());
        model.addAttribute("totalOfertas",   ofertaRepository.count());
        model.addAttribute("totalDemandas",  demandaRepository.count());
        model.addAttribute("totalAcuerdos",  acuerdoRepository.count());
    }

    // ── GET /admin  →  redirige a /admin/panel ─────────────────────────────────
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
        // Lista completa de empresas registradas para la tabla
        model.addAttribute("empresas", empresaRepository.findAll());
        return "admin_usuarios";
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navOfertas", true);
        model.addAttribute("todasOfertas", ofertaRepository.findAll());
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
