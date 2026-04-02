package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller to handle market-related (Mercado) web requests.
 * Connects the Mercado view with offer data from the database.
 */
@Controller
public class MercadoController {

    private final OfertaRepository ofertaRepository;

    /**
     * Constructor-based injection of the Oferta repository.
     * @param ofertaRepository repository for offer data
     */
    public MercadoController(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    /**
     * Retrieves all offers from the database and displays the marketplace page.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the name of the template ("mercado")
     */
    @GetMapping("/mercado")
    public String mostrarMercado(Model model) {
        // Fetch all offers from the database using JPA
        List<Oferta> listaDeOfertas = ofertaRepository.findAll();
        
        // Add the list of offers to the model to be rendered by Mustache/HTML
        model.addAttribute("ofertas", listaDeOfertas);
        
        return "mercado";
    }
}
