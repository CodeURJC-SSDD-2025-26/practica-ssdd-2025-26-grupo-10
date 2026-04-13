package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import es.urjc.ecomostoles.backend.repository.MensajeRepository;

/**
 * Controller for handling the Dashboard view.
 * It provides data such as company info and total KPIs.
 */
@Controller
public class DashboardController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;
    private final DemandaRepository demandaRepository;
    private final AcuerdoRepository acuerdoRepository;
    private final MensajeRepository mensajeRepository;

    /**
     * Constructor-based injection of repositories.
     */
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

    /**
     * Shows the dashboard for the active company.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the name of the template ("dashboard") or a redirect to home if not found
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Injecting the company object for the dynamic navbar and profile display
            model.addAttribute("empresa", empresa);

            // Fetching sizes for the Dashboard KPIs
            int totalOfertas = ofertaRepository.findByEmpresa(empresa).size();
            int totalDemandas = demandaRepository.findByEmpresa(empresa).size();
            int totalAcuerdos = acuerdoRepository.findByEmpresa(empresa).size();
            int totalMensajes = mensajeRepository.findByDestinatario(empresa).size();

            // Adding the variables to the model
            model.addAttribute("totalOfertas", totalOfertas);
            model.addAttribute("totalDemandas", totalDemandas);
            model.addAttribute("totalAcuerdos", totalAcuerdos);
            model.addAttribute("totalMensajes", totalMensajes);

            // Returning the view name without extension
            return "dashboard";
        }

        // If the company doesn't exist, redirect to the landing page
        return "redirect:/";
    }
}
