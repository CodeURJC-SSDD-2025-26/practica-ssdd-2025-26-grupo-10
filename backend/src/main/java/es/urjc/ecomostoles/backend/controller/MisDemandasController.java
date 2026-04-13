package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

/**
 * Controller to handle the retrieval and display of demands specific to the logged-in company.
 */
@Controller
public class MisDemandasController {

    private final EmpresaRepository empresaRepository;
    private final DemandaRepository demandaRepository;

    /**
     * Constructor-based dependency injection.
     * @param empresaRepository repository for company data
     * @param demandaRepository repository for demand data
     */
    public MisDemandasController(EmpresaRepository empresaRepository, DemandaRepository demandaRepository) {
        this.empresaRepository = empresaRepository;
        this.demandaRepository = demandaRepository;
    }

    /**
     * Displays the "Mis Demandas" page for the active company.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the template name "mis_demandas" or a redirect to home if no company is found
     */
    @GetMapping("/dashboard/mis-demandas")
    public String mostrarMisDemandas(Model model) {
        // Fetch the active company (simulated session)
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Add the company to the model for the navbar/sidebar
            model.addAttribute("empresa", empresa);

            // Fetch only the demands belonging to this company
            List<Demanda> misDemandas = demandaRepository.findByEmpresa(empresa);
            // Add the list of demands to the model
            model.addAttribute("demandas", misDemandas);

            // Return the view name
            return "mis_demandas";
        }

        // Redirect to home if the company is not found
        return "redirect:/";
    }
}
