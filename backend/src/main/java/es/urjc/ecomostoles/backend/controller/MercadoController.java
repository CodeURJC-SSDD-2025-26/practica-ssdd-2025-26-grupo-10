package es.urjc.ecomostoles.backend.controller;


import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

/**
 * Controller to handle market-related (Mercado) web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates data access to OfertaService.
 */
@Controller
public class MercadoController {

    private final OfertaService ofertaService;

    public MercadoController(OfertaService ofertaService) {
        this.ofertaService  = ofertaService;
    }

    /**
     * Retrieves active offers from the database and displays the marketplace page.
     * Supports filtering by keyword, tipoResiduo, and poligono with pagination.
     */
    @GetMapping("/mercado")
    public String mostrarMercado(Model model, Principal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tipoResiduo,
            @RequestParam(required = false) String poligono,
            @RequestParam(required = false) String error,
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable) {

        Page<OfertaResumen> paginaOfertas = ofertaService.buscarOfertasFiltradas(keyword, tipoResiduo, poligono, pageable);

        model.addAttribute("ofertas",   paginaOfertas.getContent());
        model.addAttribute("hasOfertas", !paginaOfertas.isEmpty());
        
        // Pagination metadata
        model.addAttribute("currentPage", paginaOfertas.getNumber() + 1);
        model.addAttribute("totalPages",  paginaOfertas.getTotalPages());
        model.addAttribute("hasNext",     paginaOfertas.hasNext());
        model.addAttribute("hasPrev",     paginaOfertas.hasPrevious());
        model.addAttribute("prevPage",    paginaOfertas.getNumber() - 1);
        model.addAttribute("nextPage",    paginaOfertas.getNumber() + 1);
        model.addAttribute("totalItems",  paginaOfertas.getTotalElements());

        // Dynamic base URL and query string for pagination partial
        model.addAttribute("pagBaseUrl", "/mercado");
        StringBuilder qs = new StringBuilder();
        if (keyword != null && !keyword.isEmpty()) qs.append("&keyword=").append(keyword);
        if (tipoResiduo != null && !tipoResiduo.isEmpty()) qs.append("&tipoResiduo=").append(tipoResiduo);
        if (poligono != null && !poligono.isEmpty()) qs.append("&poligono=").append(poligono);
        model.addAttribute("pagQueryString", qs.toString());

        model.addAttribute("keyword",    keyword    != null ? keyword    : "");
        model.addAttribute("tipoResiduo", tipoResiduo != null ? tipoResiduo : "");
        model.addAttribute("poligono",   poligono   != null ? poligono   : "");

        if ("AutoAcuerdo".equals(error)) {
            model.addAttribute("errorAutoAcuerdo", "No puedes realizar un acuerdo comercial con tu propia empresa.");
        }
        
        model.addAttribute("navMercado", true);
        model.addAttribute("isMercado", true);

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
