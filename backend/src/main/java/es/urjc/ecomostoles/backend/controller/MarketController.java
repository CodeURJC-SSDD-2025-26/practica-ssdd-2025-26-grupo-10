package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.OfferService;
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
public class MarketController {

    private final OfferService offerService;

    public MarketController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Retrieves active offers from the database and displays the marketplace page.
     * Supports filtering by keyword, wasteType, and industrialArea with pagination.
     */
    @GetMapping("/mercado")
    public String showMarket(Model model, Principal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String wasteType,
            @RequestParam(required = false) String industrialArea,
            @RequestParam(required = false) String error,
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable) {

        Page<OfferSummary> offerPage = offerService.searchFilteredOffers(keyword, wasteType, industrialArea,
                pageable);

        model.addAttribute("ofertas", offerPage.getContent());
        model.addAttribute("hasOfertas", !offerPage.isEmpty());

        // Pagination metadata
        model.addAttribute("currentPage", offerPage.getNumber() + 1);
        model.addAttribute("totalPages", offerPage.getTotalPages());
        model.addAttribute("hasNext", offerPage.hasNext());
        model.addAttribute("hasPrev", offerPage.hasPrevious());
        model.addAttribute("prevPage", offerPage.getNumber() - 1);
        model.addAttribute("nextPage", offerPage.getNumber() + 1);
        model.addAttribute("totalItems", offerPage.getTotalElements());

        // Dynamic base URL and query string for pagination partial
        model.addAttribute("pagBaseUrl", "/mercado");
        StringBuilder qs = new StringBuilder();
        if (keyword != null && !keyword.isEmpty())
            qs.append("&keyword=").append(keyword);
        if (wasteType != null && !wasteType.isEmpty())
            qs.append("&tipoResiduo=").append(wasteType);
        if (industrialArea != null && !industrialArea.isEmpty())
            qs.append("&poligono=").append(industrialArea);
        model.addAttribute("pagQueryString", qs.toString());

        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("tipoResiduo", wasteType != null ? wasteType : "");
        model.addAttribute("poligono", industrialArea != null ? industrialArea : "");

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
    public String showOfferDetail(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Offer> offerOpt = offerService.findById(id);
        if (offerOpt.isPresent()) {
            // Single clean call to the service to register an atomic visit
            offerService.registerVisit(id);

            Offer offer = offerOpt.get();
            model.addAttribute("oferta", offer);
            return "detalle_activo";
        }
        return "redirect:/mercado";
    }

    /**
     * Serves the image of an offer from the database BLOB.
     */
    @GetMapping("/oferta/{id}/imagen")
    public ResponseEntity<byte[]> downloadOfferImage(@PathVariable("id") Long id) {
        Optional<Offer> offer = offerService.findById(id);
        if (offer.isPresent() && offer.get().getImage() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(offer.get().getImage());
        }
        return ResponseEntity.notFound().build();
    }
}
