package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

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

    /**
     * Shows the details of a specific offer.
     *
     * @param id    the ID of the offer to display
     * @param model the Spring UI model
     * @return the template name "detalle_activo" if found, else redirects to market
     */
    @GetMapping("/oferta/{id}")
    public String mostrarDetalleOferta(@PathVariable Long id, Model model) {
        Optional<Oferta> oferta = ofertaRepository.findById(id);
        if (oferta.isPresent()) {
            model.addAttribute("oferta", oferta.get());
            return "detalle_activo";
        } else {
            return "redirect:/mercado";
        }
    }

    /**
     * Serves the image of an offer from the database BLOB.
     *
     * @param id the ID of the offer
     * @return a ResponseEntity containing the image bytes
     */
    @GetMapping("/oferta/{id}/imagen")
    public ResponseEntity<byte[]> descargarImagenOferta(@PathVariable Long id) {
        Optional<Oferta> oferta = ofertaRepository.findById(id);
        if (oferta.isPresent() && oferta.get().getImagen() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(oferta.get().getImagen());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
