package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.service.DemandaService;
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
 * Controller to handle demand-related (Demanda/Solicitudes) web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandaService.
 */
@Controller
public class DemandaController {

    private final DemandaService demandaService;

    public DemandaController(DemandaService demandaService) {
        this.demandaService  = demandaService;
    }

    /**
     * Retrieves active demands and displays the solicitudes marketplace page with pagination.
     */
    @GetMapping("/solicitudes")
    public String mostrarTablonDemandas(Model model, Principal principal,
            @PageableDefault(size = 9) Pageable pageable) {
        
        Page<Demanda> paginaDemandas = demandaService.obtenerPorEstadoPaginada(es.urjc.ecomostoles.backend.model.EstadoDemanda.ACTIVA, pageable);
        
        model.addAttribute("demandas", paginaDemandas.getContent());
        model.addAttribute("hasDemandas", !paginaDemandas.isEmpty());

        // Pagination metadata
        model.addAttribute("currentPage", paginaDemandas.getNumber() + 1);
        model.addAttribute("totalPages",  paginaDemandas.getTotalPages());
        model.addAttribute("hasNext",     paginaDemandas.hasNext());
        model.addAttribute("hasPrev",     paginaDemandas.hasPrevious());
        model.addAttribute("prevPage",    paginaDemandas.getNumber() - 1);
        model.addAttribute("nextPage",    paginaDemandas.getNumber() + 1);
        model.addAttribute("totalItems",  paginaDemandas.getTotalElements());

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
    public String mostrarDetalleDemanda(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<Demanda> demandaOpt = demandaService.buscarPorId(id);

        if (demandaOpt.isPresent()) {
            model.addAttribute("demanda", demandaOpt.get());

            return "detalle_solicitud";
        }

        return "redirect:/solicitudes";
    }

    /**
     * Saves a demand to the company's favorite list.
     */
    @PostMapping("/demandas/{id}/favorito")
    public String toggleFavorito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Simple logic for the demo: provide visual feedback
        redirectAttributes.addFlashAttribute("mensajeFavorito", "La demanda #" + id + " ha sido guardada en tus favoritos correctamente.");
        return "redirect:/demanda/" + id;
    }
}
