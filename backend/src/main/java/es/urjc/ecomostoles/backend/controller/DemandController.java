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
 * Controller mapping HTTP requests corresponding to the Demand entity
 * lifecycle.
 * 
 * Manages the front-end rendering and interaction flows for the Demand
 * marketplace. Implements a
 * strict Controller -> Service architecture, enforcing boundaries by delegating
 * data retrieval,
 * pagination, and business rules entirely to the service layer.
 */
@Controller
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    /**
     * Renders the marketplace board displaying all active material demands.
     * 
     * @param model     the Spring MVC model holding data to render the view.
     * @param principal the authenticated user context.
     * @param pageable  pagination parameters (page, size, sort) resolved from the
     *                  URL footprint.
     * @return the resolved path to the mustache template serving the demands
     *         marketplace.
     */
    @GetMapping("/solicitudes")
    public String showDemandBoard(Model model, Principal principal,
            @PageableDefault(size = 9) Pageable pageable) {

        Page<Demand> demandPage = demandService
                .getByStatusPaginated(es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE, pageable);

        model.addAttribute("demands", demandPage.getContent());
        model.addAttribute("hasDemands", !demandPage.isEmpty());

        // Pagination metadata
        model.addAttribute("currentPage", demandPage.getNumber() + 1);
        model.addAttribute("totalPages", demandPage.getTotalPages() == 0 ? 1 : demandPage.getTotalPages());
        model.addAttribute("hasNext", demandPage.hasNext());
        model.addAttribute("hasPrevious", demandPage.hasPrevious());
        model.addAttribute("prevPage", demandPage.getNumber() - 1);
        model.addAttribute("nextPage", demandPage.getNumber() + 1);
        model.addAttribute("totalItems", demandPage.getTotalElements());

        // Dynamic base URL for pagination partial
        model.addAttribute("paginationBaseUrl", "/solicitudes");
        model.addAttribute("paginationQueryString", "");

        model.addAttribute("navDemands", true);

        return "solicitudes";
    }

    /**
     * Displays the detailed view for a single demand object.
     * 
     * Applies conditional logic to increment visit counters only if the accessor is
     * not
     * the entity owner, preventing skewed analytics.
     * 
     * @param id                 the unique identifier corresponding to the target
     *                           Demand entity.
     * @param model              the current MVC context model.
     * @param principal          the current authenticated user instance.
     * @param redirectAttributes flash context for propagating soft errors.
     * @return the detailed view template, or redirects backwards on entity
     *         resolution failure.
     */
    @GetMapping("/demanda/{id}")
    public String showDemandDetail(@PathVariable("id") Long id, Model model, Principal principal,
            RedirectAttributes redirectAttributes) {
        Optional<Demand> demandOpt = demandService.findById(id);

        if (demandOpt.isPresent()) {
            Demand demand = demandOpt.get();
            model.addAttribute("demand", demand);

            boolean isOwner = false;
            if (principal != null) {
                // Use email comparison for consistency since principal.getName() is email
                isOwner = demand.getCompany().getContactEmail().equals(principal.getName());
            }
            model.addAttribute("isOwner", isOwner);

            // Only register visit if not viewed by owner
            if (!isOwner) {
                demandService.registerVisit(id);
            }

            return "detalle_solicitud";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "La demanda solicitada no existe o ha sido eliminada.");
        return "redirect:/solicitudes";
    }

    /**
     * Handles POST requests for favoriting a specific demand.
     * 
     * @param id                 the identifier of the demand being marked as
     *                           preferred.
     * @param redirectAttributes attribute dispatcher to render UI feedback to the
     *                           client.
     * @return redirection directive returning the user back to the detail view.
     */
    @PostMapping("/demandas/{id}/favorito")
    public String toggleFavorite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Simple logic for the demo: provide visual feedback
        redirectAttributes.addFlashAttribute("favoriteMessage",
                "La demanda #" + id + " ha sido guardada en tus favoritos correctamente.");
        return "redirect:/demanda/" + id;
    }
}
