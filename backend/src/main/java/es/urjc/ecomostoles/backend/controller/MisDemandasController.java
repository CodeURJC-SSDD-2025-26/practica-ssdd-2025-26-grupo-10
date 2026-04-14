package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.EstadoDemanda;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle CRUD operations for demands specific to the logged-in company.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to DemandaService and EmpresaService.
 */
@Controller
public class MisDemandasController {

    private final EmpresaService empresaService;
    private final DemandaService demandaService;

    public MisDemandasController(EmpresaService empresaService, DemandaService demandaService) {
        this.empresaService = empresaService;
        this.demandaService = demandaService;
    }

    // -------------------------------------------------------------------------
    // Ownership helper: returns the demand if the user is the author or ADMIN.
    // -------------------------------------------------------------------------
    private Demanda verificarPropietarioDemanda(Long demandaId, Principal principal) {
        Demanda demanda = demandaService.buscarPorId(demandaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demanda no encontrada: " + demandaId));

        Empresa empresaLogueada = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean esAdmin = empresaLogueada.getRoles() != null
                && empresaLogueada.getRoles().contains("ADMIN");
        boolean esPropietario = demanda.getEmpresa() != null
                && demanda.getEmpresa().getId().equals(empresaLogueada.getId());

        if (!esAdmin && !esPropietario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta demanda.");
        }
        return demanda;
    }

    // -------------------------------------------------------------------------
    // GET /dashboard/mis-demandas
    // -------------------------------------------------------------------------
    @GetMapping("/dashboard/mis-demandas")
    public String mostrarMisDemandas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            List<Demanda> misDemandas = demandaService.obtenerPorEmpresa(empresa);
            model.addAttribute("demandas", misDemandas);
            model.addAttribute("totalDemandasActivas", demandaService.contarActivasPorEmpresa(empresa));
            return "mis_demandas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // GET /demanda/nueva — Show new demand form
    // -------------------------------------------------------------------------
    @GetMapping("/demanda/nueva")
    public String mostrarFormularioNuevaDemanda(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            model.addAttribute("demanda", new Demanda());
            return "crear_solicitud";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /demanda/nueva — Create new demand with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/demanda/nueva")
    public String guardarNuevaDemanda(@Valid @ModelAttribute("demanda") Demanda demanda,
                                      BindingResult result,
                                      Model model,
                                      Principal principal) {

        if (result.hasErrors()) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(e -> model.addAttribute("empresa", e));
            model.addAttribute("errores", result.getAllErrors());
            model.addAttribute("demanda", demanda);
            return "crear_solicitud";
        }

        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            demanda.setEmpresa(empresaOpt.get());
            demanda.setFechaPublicacion(LocalDateTime.now());
            demanda.setEstado(EstadoDemanda.ACTIVA);
            demandaService.guardar(demanda);
            return "redirect:/dashboard/mis-demandas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /demandas/{id}/eliminar — Delete demand (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/eliminar")
    public String eliminarDemanda(@PathVariable Long id, Principal principal) {
        verificarPropietarioDemanda(id, principal);
        demandaService.eliminar(id);
        return "redirect:/dashboard/mis-demandas";
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/demandas/{id}/editar")
    public String mostrarFormularioEditarDemanda(@PathVariable Long id, Model model, Principal principal) {
        Demanda demanda = verificarPropietarioDemanda(id, principal);
        empresaService.buscarPorEmail(principal.getName())
                      .ifPresent(e -> model.addAttribute("empresa", e));
        model.addAttribute("demanda", demanda);
        return "editar_solicitud";
    }

    // -------------------------------------------------------------------------
    // POST /demanda/editar/{id} — Save changes with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/demandas/{id}/editar")
    public String guardarEdicionDemanda(@PathVariable Long id,
                                        @Valid @ModelAttribute("demanda") Demanda demandaForm,
                                        BindingResult result,
                                        Model model,
                                        Principal principal) {

        if (result.hasErrors()) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(e -> model.addAttribute("empresa", e));
            model.addAttribute("errores", result.getAllErrors());
            demandaForm.setId(id);
            return "editar_solicitud";
        }

        Optional<Demanda> demandaOpt = demandaService.buscarPorId(id);
        if (demandaOpt.isPresent()) {
            Demanda demandaExistente = demandaOpt.get();
            demandaExistente.setTitulo(demandaForm.getTitulo());
            demandaExistente.setDescripcion(demandaForm.getDescripcion());
            demandaExistente.setCategoriaMaterial(demandaForm.getCategoriaMaterial());
            demandaExistente.setCantidad(demandaForm.getCantidad());
            demandaExistente.setPresupuestoMaximo(demandaForm.getPresupuestoMaximo());
            demandaService.guardar(demandaExistente);
        }

        return "redirect:/dashboard/mis-demandas";
    }
}
