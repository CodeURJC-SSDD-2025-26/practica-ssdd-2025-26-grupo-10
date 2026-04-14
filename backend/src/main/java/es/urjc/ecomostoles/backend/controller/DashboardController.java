package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;

/**
 * Controller for handling the Dashboard view.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all domain data access to the four services.
 * Now uses MensajeService instead of direct repository access.
 *
 * - ADMIN: ve KPIs globales de toda la plataforma.
 * - EMPRESA: ve KPIs propios (sus ofertas, demandas, acuerdos, mensajes).
 */
@Controller
public class DashboardController {

    private final EmpresaService   empresaService;
    private final OfertaService    ofertaService;
    private final DemandaService   demandaService;
    private final AcuerdoService   acuerdoService;

    public DashboardController(EmpresaService empresaService,
                               OfertaService ofertaService,
                               DemandaService demandaService,
                               AcuerdoService acuerdoService) {
        this.empresaService    = empresaService;
        this.ofertaService     = ofertaService;
        this.demandaService    = demandaService;
        this.acuerdoService    = acuerdoService;
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get(); // Keep entity for logic
            EmpresaDTO empresaSegura = new EmpresaDTO(empresa);
            model.addAttribute("empresa", empresaSegura); // Add DTO to model
            model.addAttribute("navDashboard", true);

            boolean esAdmin = empresa.getRoles() != null
                    && empresa.getRoles().contains("ADMIN");

            if (esAdmin) {
                // ── Admin: KPIs globales de toda la plataforma ──────────────
                model.addAttribute("esAdmin", true);
                model.addAttribute("totalOfertas",  (int) ofertaService.contarTodas());
                model.addAttribute("totalDemandas", (int) demandaService.contarTodas());
                model.addAttribute("totalAcuerdos", (int) acuerdoService.contarTodos());

            } else {
                // ── Empresa normal: KPIs propios ────────────────────────────
                model.addAttribute("totalOfertas",  (int) ofertaService.contarPorEmpresa(empresa));
                model.addAttribute("totalDemandas", (int) demandaService.contarPorEmpresa(empresa));
                model.addAttribute("totalAcuerdos", (int) acuerdoService.contarPorEmpresa(empresa));
                
                // New dynamic KPI: Total re-introduced material
                model.addAttribute("materialReintroducido", acuerdoService.sumarMaterialReintroducido(empresa));


                // --- SMART MATCHING ALGORITHM ---
                List<Demanda> recommendedDemandas = demandaService.obtenerSmartRecommendations(empresa);

                model.addAttribute("smartRecommendations", recommendedDemandas);
                model.addAttribute("hasRecommendations", !recommendedDemandas.isEmpty());
            }

            // --- DYNAMIC CHART DATA GENERATION ---
            List<Integer> activityStats;
            if (esAdmin) {
                activityStats = List.of(
                    (int) ofertaService.contarTodas(),
                    (int) demandaService.contarTodas(),
                    (int) acuerdoService.contarTodos()
                );
            } else {
                activityStats = List.of(
                    (int) ofertaService.contarPorEmpresa(empresa),
                    (int) demandaService.contarPorEmpresa(empresa),
                    (int) acuerdoService.contarPorEmpresa(empresa)
                );
            }
            model.addAttribute("chartData", activityStats);

            return "dashboard";
        }

        return "redirect:/";
    }
}
