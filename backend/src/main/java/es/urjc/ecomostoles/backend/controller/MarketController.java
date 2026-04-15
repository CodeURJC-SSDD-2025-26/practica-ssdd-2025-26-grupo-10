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
            @RequestParam(required = false) es.urjc.ecomostoles.backend.model.WasteCategory wasteCategory,
            @RequestParam(required = false) String industrialPark,
            @RequestParam(required = false) String error,
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable) {

        Page<OfferSummary> offerPage = offerService.searchFilteredOffers(keyword, wasteCategory, industrialPark,
                pageable);

        model.addAttribute("offers", offerPage.getContent());
        model.addAttribute("hasOffers", !offerPage.isEmpty());

        // Pagination metadata
        model.addAttribute("currentPage", offerPage.getNumber() + 1);
        model.addAttribute("totalPages", offerPage.getTotalPages() == 0 ? 1 : offerPage.getTotalPages());
        model.addAttribute("hasNext", offerPage.hasNext());
        model.addAttribute("hasPrevious", offerPage.hasPrevious());
        model.addAttribute("prevPage", offerPage.getNumber() - 1);
        model.addAttribute("nextPage", offerPage.getNumber() + 1);
        model.addAttribute("totalItems", offerPage.getTotalElements());

        // Dynamic base URL and query string for pagination partial
        model.addAttribute("paginationBaseUrl", "/mercado");
        StringBuilder qs = new StringBuilder();
        if (keyword != null && !keyword.isEmpty())
            qs.append("&keyword=").append(keyword);
        if (wasteCategory != null)
            qs.append("&wasteCategory=").append(wasteCategory.name());
        if (industrialPark != null && !industrialPark.isEmpty())
            qs.append("&industrialPark=").append(industrialPark);
        model.addAttribute("paginationQueryString", qs.toString());

        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("wasteCategory", wasteCategory != null ? wasteCategory.name() : "");
        model.addAttribute("industrialPark", industrialPark != null ? industrialPark : "");

        if ("AutoAcuerdo".equals(error)) {
            model.addAttribute("autoAgreementError", "No puedes realizar un acuerdo comercial con tu propia empresa.");
        }

        model.addAttribute("navMarket", true);
        model.addAttribute("isMercado", true);

        return "mercado";
    }

    /**
     * Shows the details of a specific offer.
     */
    @GetMapping("/oferta/{id}")
    public String showOfferDetail(@PathVariable("id") Long id, Model model, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Optional<Offer> offerOpt = offerService.findById(id);
        if (offerOpt.isPresent()) {
            Offer offer = offerOpt.get();
            model.addAttribute("offer", offer);

            // Check if user is the owner to hide contact form and skip visit count
            boolean isOwner = false;
            if (principal != null) {
                isOwner = offer.getCompany().getContactEmail().equals(principal.getName());
            }
            model.addAttribute("isOwner", isOwner);

            // Only register visit if not viewed by owner
            if (!isOwner) {
                offerService.registerVisit(id);
            }

            return "detalle_activo";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "La oferta que buscas no existe o ha sido eliminada.");
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
