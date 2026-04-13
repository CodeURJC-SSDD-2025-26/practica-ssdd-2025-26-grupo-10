package es.urjc.ecomostoles.backend.controller;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;

/**
 * Controller for handling the Dashboard view.
 * It provides data such as company info and total number of offers.
 */
@Controller
public class DashboardController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;

    /**
     * Constructor-based injection of repositories.
     * 
     * @param empresaRepository repository for company data
     * @param ofertaRepository  repository for offer data
     */
    public DashboardController(EmpresaRepository empresaRepository, OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
    }

    /**
     * Shows the dashboard for the active company.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the name of the template ("dashboard") or a redirect to home if not found
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Simulating an active session by finding the core company by its contact email
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Injecting the company object for the dynamic navbar and profile display
            model.addAttribute("empresa", empresa);

            // Calculating the total number of offers published by this company
            List<Oferta> ofertas = ofertaRepository.findByEmpresa(empresa);
            int total = ofertas.size();
            // Adding the total offers count to the model
            model.addAttribute("totalOfertas", total);

            // Returning the view name without extension
            return "dashboard";
        }

        // If the company doesn't exist, redirect to the landing page
        return "redirect:/";
    }
}
