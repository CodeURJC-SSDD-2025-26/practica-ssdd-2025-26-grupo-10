package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle demand-related (Demanda/Solicitudes) web requests.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandaService and EmpresaService.
 */
@Controller
public class DemandaController {

    private final DemandaService demandaService;
    private final EmpresaService empresaService;

    public DemandaController(DemandaService demandaService, EmpresaService empresaService) {
        this.demandaService  = demandaService;
        this.empresaService  = empresaService;
    }

    /**
     * Retrieves all demands and displays the solicitudes marketplace page.
     */
    @GetMapping("/solicitudes")
    public String mostrarTablonDemandas(Model model, Principal principal) {
        if (principal != null) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(empresa -> model.addAttribute("empresa", empresa));
        }

        List<Demanda> todasLasDemandas = demandaService.obtenerTodas();
        model.addAttribute("demandas", todasLasDemandas);
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

            if (principal != null) {
                empresaService.buscarPorEmail(principal.getName())
                              .ifPresent(empresa -> model.addAttribute("empresa", empresa));
            }

            return "detalle_solicitud";
        }

        return "redirect:/solicitudes";
    }
}
