package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import es.urjc.ecomostoles.backend.service.DashboardService;
import es.urjc.ecomostoles.backend.service.EmpresaService;

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
    private final DashboardService dashboardService;

    public DashboardController(EmpresaService empresaService, DashboardService dashboardService) {
        this.empresaService    = empresaService;
        this.dashboardService  = dashboardService;
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", new EmpresaDTO(empresa));
            model.addAttribute("navDashboard", true);

            // Delegating business logic to Service
            model.addAllAttributes(dashboardService.obtenerEstadisticas(empresa));

            return "dashboard";
        }

        return "redirect:/";
    }
}
