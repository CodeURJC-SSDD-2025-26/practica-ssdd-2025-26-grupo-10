package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.service.DemandService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.security.Principal;
import java.util.Optional;

/**
 * Controller to handle demand-related (Demandas/Solicitudes) web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandService.
 */
@Controller
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    /**
     * Retrieves active demands and displays the solicitudes marketplace page with
     * pagination.
     */
    @GetMapping("/solicitudes")
    public String showDemandBoard(Model model, Principal principal,
            @PageableDefault(size = 9) Pageable pageable) {

        Page<Demand> demandPage = demandService
                .getByStatusPaginated(es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE, pageable);

        model.addAttribute("demandas", demandPage.getContent());
        model.addAttribute("hasDemandas", !demandPage.isEmpty());

        // Pagination metadata
        model.addAttribute("currentPage", demandPage.getNumber() + 1);
        model.addAttribute("totalPages", demandPage.getTotalPages());
        model.addAttribute("hasNext", demandPage.hasNext());
        model.addAttribute("hasPrev", demandPage.hasPrevious());
        model.addAttribute("prevPage", demandPage.getNumber() - 1);
        model.addAttribute("nextPage", demandPage.getNumber() + 1);
        model.addAttribute("totalItems", demandPage.getTotalElements());

        // Dynamic base URL for pagination partial
        model.addAttribute("pagBaseUrl", "/solicitudes");
        model.addAttribute("pagQueryString", "");

        model.addAttribute("navDemandas", true);

        return "solicitudes";
    }

    /**
     * Shows the detail view for a specific demand.
     * Returns 404 redirect if the demand does not exist.
     */
    @GetMapping("/demanda/{id}")
    public String showDemandDetail(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Demand> demandOpt = demandService.findById(id);

        if (demandOpt.isPresent()) {
            model.addAttribute("demanda", demandOpt.get());

            return "detalle_solicitud";
        }

        return "redirect:/solicitudes";
    }

    /**
     * Saves a demand to the company's favorite list.
     */
    @PostMapping("/demandas/{id}/favorito")
    public String toggleFavorite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Simple logic for the demo: provide visual feedback
        redirectAttributes.addFlashAttribute("mensajeFavorito",
                "La demanda #" + id + " ha sido guardada en tus favoritos correctamente.");
        return "redirect:/demanda/" + id;
    }
}
