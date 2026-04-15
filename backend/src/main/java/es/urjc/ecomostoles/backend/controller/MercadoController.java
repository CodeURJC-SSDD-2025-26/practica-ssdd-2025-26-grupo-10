package es.urjc.ecomostoles.backend.controller;


import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle market-related (Mercado) web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates data access to OfertaService and EmpresaService.
 */
@Controller
public class MercadoController {

    private final OfertaService ofertaService;
    private final EmpresaService empresaService;

    public MercadoController(OfertaService ofertaService, EmpresaService empresaService) {
        this.ofertaService  = ofertaService;
        this.empresaService = empresaService;
    }

    /**
     * Retrieves active offers from the database and displays the marketplace page.
     * Supports filtering by keyword, tipoResiduo, and poligono.
     */
    @GetMapping("/mercado")
    public String mostrarMercado(Model model, Principal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tipoResiduo,
            @RequestParam(required = false) String poligono,
            @RequestParam(required = false) String error) {

        List<OfertaResumen> ofertasFiltradas = ofertaService.buscarOfertasFiltradas(keyword, tipoResiduo, poligono);

        model.addAttribute("ofertas", ofertasFiltradas);
        model.addAttribute("keyword",    keyword    != null ? keyword    : "");
        model.addAttribute("tipoResiduo", tipoResiduo != null ? tipoResiduo : "");
        model.addAttribute("poligono",   poligono   != null ? poligono   : "");

        if ("AutoAcuerdo".equals(error)) {
            model.addAttribute("errorAutoAcuerdo", "No puedes realizar un acuerdo comercial con tu propia empresa.");
        }
        
        model.addAttribute("navMercado", true);

        return "mercado";
    }

    /**
     * Shows the details of a specific offer.
     */
    @GetMapping("/oferta/{id}")
    public String mostrarDetalleOferta(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Oferta> ofertaOpt = ofertaService.buscarPorId(id);
        if (ofertaOpt.isPresent()) {
            // Single clean call to the service to register an atomic visit
            ofertaService.registrarVisita(id);
            
            Oferta oferta = ofertaOpt.get();
            model.addAttribute("oferta", oferta);
            return "detalle_activo";
        }
        return "redirect:/mercado";
    }

    /**
     * Serves the image of an offer from the database BLOB.
     */
    @GetMapping("/oferta/{id}/imagen")
    public ResponseEntity<byte[]> descargarImagenOferta(@PathVariable("id") Long id) {
        Optional<Oferta> oferta = ofertaService.buscarPorId(id);
        if (oferta.isPresent() && oferta.get().getImagen() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(oferta.get().getImagen());
        }
        return ResponseEntity.notFound().build();
    }
}
