package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle demand-related (Demanda/Solicitudes) web requests.
 * Connects the Solicitudes view with demand data from the database.
 */
@Controller
public class DemandaController {

    private final DemandaRepository demandaRepository;
    private final EmpresaRepository empresaRepository;

    /**
     * Constructor-based injection of repositories.
     * @param demandaRepository repository for demand data
     * @param empresaRepository repository for company data
     */
    public DemandaController(DemandaRepository demandaRepository, EmpresaRepository empresaRepository) {
        this.demandaRepository = demandaRepository;
        this.empresaRepository = empresaRepository;
    }

    /**
     * Retrieves all demands from the database and displays the solicitudes page.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the name of the template ("solicitudes")
     */
    @GetMapping("/solicitudes")
    public String mostrarTablonDemandas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
        }

        List<Demanda> todasLasDemandas = demandaRepository.findAll();
        model.addAttribute("demandas", todasLasDemandas);
        
        // Nav highlight: mark Demandas tab as active
        model.addAttribute("navDemandas", true);

        return "solicitudes";
    }

    /**
     * Shows the details of a specific demand.
     *
     * @param id    the ID of the demand to display
     * @param model the Spring UI model
     * @return the template name "detalle_solicitud" if found, else redirects to solicitudes
     */
    @GetMapping("/demanda/{id}")
    public String mostrarDetalleDemanda(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Demanda> demanda = demandaRepository.findById(id);
        if (demanda.isPresent()) {
            model.addAttribute("demanda", demanda.get());
            
            // Inject active company context for the navbar
            Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
            empresaOpt.ifPresent(empresa -> model.addAttribute("empresa", empresa));
            
            return "detalle_solicitud";
        } else {
            return "redirect:/solicitudes";
        }
    }

}
