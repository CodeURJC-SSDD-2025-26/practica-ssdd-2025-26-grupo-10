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
 * Controller to handle demand-related web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandService.
 */
@Controller
public class DemandController {

    private final DemandService demandService;
    private final es.urjc.ecomostoles.backend.service.CompanyService companyService;

    public DemandController(DemandService demandService, es.urjc.ecomostoles.backend.service.CompanyService companyService) {
        this.demandService = demandService;
        this.companyService = companyService;
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
     * Shows the detail view for a specific demand.
     * Returns 404 redirect if the demand does not exist.
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
     * Saves a demand to the company's favorite list.
     */
    @PostMapping("/demandas/{id}/favorito")
    public String toggleFavorite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Simple logic for the demo: provide visual feedback
        redirectAttributes.addFlashAttribute("favoriteMessage",
                "La demanda #" + id + " ha sido guardada en tus favoritos correctamente.");
        return "redirect:/demanda/" + id;
    }
}
