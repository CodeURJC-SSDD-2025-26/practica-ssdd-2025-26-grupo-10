package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller to handle market-related (Mercado) web requests.
 * Connects the Mercado view with offer data from the database.
 */
@Controller
public class MercadoController {

    private final OfertaRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;

    /**
     * Constructor-based injection of repositories.
     * 
     * @param ofertaRepository  repository for offer data
     * @param empresaRepository repository for company data
     */
    public MercadoController(OfertaRepository ofertaRepository, EmpresaRepository empresaRepository) {
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
    }

    /**
     * Retrieves active offers from the database and displays the marketplace page.
     * Supports filtering by keyword (title/description), tipoResiduo, and poligono.
     *
     * @param model       the Spring UI model to pass data to the template
     * @param principal   the currently authenticated user
     * @param keyword     optional free-text search over title and description
     * @param tipoResiduo optional filter by waste type
     * @param poligono    optional filter by the company's industrial estate
     * @return the name of the template ("mercado")
     */
    @GetMapping("/mercado")
    public String mostrarMercado(Model model, Principal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tipoResiduo,
            @RequestParam(required = false) String poligono) {

        // Only show active offers in the marketplace ("Activa" is the exact value
        // stored in DB)
        List<Oferta> ofertasFiltradas = ofertaRepository.findAll().stream()
                .filter(o -> "Activa".equalsIgnoreCase(o.getEstado()))
                .collect(Collectors.toList());

        // Filter by keyword — matches title OR description (case-insensitive)
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            ofertasFiltradas = ofertasFiltradas.stream()
                    .filter(o -> (o.getTitulo() != null && o.getTitulo().toLowerCase().contains(kw))
                            || (o.getDescripcion() != null && o.getDescripcion().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        // Filter by tipoResiduo (exact match, case-insensitive)
        if (tipoResiduo != null && !tipoResiduo.isBlank()) {
            ofertasFiltradas = ofertasFiltradas.stream()
                    .filter(o -> tipoResiduo.equalsIgnoreCase(o.getTipoResiduo()))
                    .collect(Collectors.toList());
        }

        // Filter by poligono — searches inside empresa.getDireccion() as free text
        if (poligono != null && !poligono.isBlank()) {
            ofertasFiltradas = ofertasFiltradas.stream()
                    .filter(o -> o.getEmpresa() != null
                            && o.getEmpresa().getDireccion() != null
                            && o.getEmpresa().getDireccion().toLowerCase().contains(poligono.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Populate the model with the filtered list
        model.addAttribute("ofertas", ofertasFiltradas);

        // Re-populate filter fields so the form remembers the user's selection
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("tipoResiduo", tipoResiduo != null ? tipoResiduo : "");
        model.addAttribute("poligono", poligono != null ? poligono : "");

        // Inject company context for the navbar
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        empresaOpt.ifPresent(empresa -> model.addAttribute("empresa", empresa));

        // Nav highlight: mark Mercado tab as active
        model.addAttribute("navMercado", true);

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
    public String mostrarDetalleOferta(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Oferta> oferta = ofertaRepository.findById(id);
        if (oferta.isPresent()) {
            model.addAttribute("oferta", oferta.get());

            // Inject active company context for the navbar
            Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
            empresaOpt.ifPresent(empresa -> model.addAttribute("empresa", empresa));

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
    public ResponseEntity<byte[]> descargarImagenOferta(@PathVariable("id") Long id) {
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
