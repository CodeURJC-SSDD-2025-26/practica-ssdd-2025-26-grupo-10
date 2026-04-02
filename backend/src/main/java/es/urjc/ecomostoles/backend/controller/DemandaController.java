package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller to handle demand-related (Demanda/Solicitudes) web requests.
 * Connects the Solicitudes view with demand data from the database.
 */
@Controller
public class DemandaController {

    private final DemandaRepository demandaRepository;

    /**
     * Constructor-based injection of the Demanda repository.
     * @param demandaRepository repository for demand data
     */
    public DemandaController(DemandaRepository demandaRepository) {
        this.demandaRepository = demandaRepository;
    }

    /**
     * Retrieves all demands from the database and displays the solicitudes page.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the name of the template ("solicitudes")
     */
    @GetMapping("/solicitudes")
    public String mostrarSolicitudes(Model model) {
        // Fetch all demands from the database using JPA
        List<Demanda> demandas = demandaRepository.findAll();
        
        // Add the list of demands to the model to be rendered by Mustache/HTML
        model.addAttribute("demandas", demandas);
        
        return "solicitudes";
    }
}
