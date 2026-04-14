package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.model.Empresa;
import java.util.List;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import es.urjc.ecomostoles.backend.repository.MensajeRepository;

/**
 * Controller for handling the Dashboard view.
 *
 * - ADMIN: ve KPIs globales de toda la plataforma.
 * - EMPRESA: ve KPIs propios (sus ofertas, demandas, acuerdos, mensajes).
 */
@Controller
public class DashboardController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;
    private final DemandaRepository demandaRepository;
    private final AcuerdoRepository acuerdoRepository;
    private final MensajeRepository mensajeRepository;

    public DashboardController(EmpresaRepository empresaRepository,
                               OfertaRepository ofertaRepository,
                               DemandaRepository demandaRepository,
                               AcuerdoRepository acuerdoRepository,
                               MensajeRepository mensajeRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
        this.demandaRepository = demandaRepository;
        this.acuerdoRepository = acuerdoRepository;
        this.mensajeRepository = mensajeRepository;
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            model.addAttribute("navDashboard", true);

            boolean esAdmin = empresa.getRoles() != null
                    && empresa.getRoles().contains("ADMIN");

            if (esAdmin) {
                // ── Admin: KPIs globales de toda la plataforma ──────────────
                model.addAttribute("esAdmin", true);
                model.addAttribute("totalOfertas",  (int) ofertaRepository.count());
                model.addAttribute("totalDemandas", (int) demandaRepository.count());
                model.addAttribute("totalAcuerdos", (int) acuerdoRepository.count());
                model.addAttribute("totalMensajes",  (int) mensajeRepository.count());
            } else {
                // ── Empresa normal: KPIs propios ────────────────────────────
                model.addAttribute("totalOfertas",  ofertaRepository.findByEmpresa(empresa).size());
                model.addAttribute("totalDemandas", demandaRepository.findByEmpresa(empresa).size());
                model.addAttribute("totalAcuerdos", acuerdoRepository.findByEmpresa(empresa).size());
                model.addAttribute("totalMensajes",  mensajeRepository.findByDestinatario(empresa).size());

                // --- SMART MATCHING ALGORITHM ---
                // Encontrar demandas activas de OTRAS empresas del MISMO sector
                List<Demanda> recommendedDemandas = demandaRepository.findAll().stream()
                        .filter(d -> "Activa".equalsIgnoreCase(d.getEstado()))
                        .filter(d -> d.getEmpresa() != null && !d.getEmpresa().getId().equals(empresa.getId()))
                        .filter(d -> d.getEmpresa().getSectorIndustrial() != null && 
                                     d.getEmpresa().getSectorIndustrial().equalsIgnoreCase(empresa.getSectorIndustrial()))
                        .limit(3)
                        .toList();

                model.addAttribute("smartRecommendations", recommendedDemandas);
                model.addAttribute("hasRecommendations", !recommendedDemandas.isEmpty());
            }

            // --- DYNAMIC CHART DATA GENERATION ---
            // Simulación de agregación mensual 
            List<Integer> monthlyStats = List.of(400, 650, 520, 780, 610, 890, 750, 430, 920, 1050, 870, 1200);
            model.addAttribute("chartData", monthlyStats);

            return "dashboard";
        }

        return "redirect:/";
    }
}
