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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import es.urjc.ecomostoles.backend.dto.SelectOption;

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
    private final es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService;

    public MisDemandasController(EmpresaService empresaService, DemandaService demandaService, es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService) {
        this.empresaService = empresaService;
        this.demandaService = demandaService;
        this.configuracionService = configuracionService;
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
    public String mostrarMisDemandas(Model model, Principal principal,
            @PageableDefault(size = 5) Pageable pageable) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("activeDemandas", true);
            model.addAttribute("isDashboard", true);
            
            Page<Demanda> paginaDemandas = demandaService.obtenerPorEmpresaPaginada(empresa, pageable);
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
            model.addAttribute("pagBaseUrl", "/dashboard/mis-demandas");
            model.addAttribute("pagQueryString", "");

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
            model.addAttribute("activeNuevaDemanda", true);
            model.addAttribute("isDashboard", true);
            model.addAttribute("demanda", new Demanda());
            injectDynamicOptions(model);
            return "crear_solicitud";
        }
        return "redirect:/";
    }

    private void injectDynamicOptions(Model model) {
        model.addAttribute("listaCategorias", configuracionService.obtenerListaSanitizada("listaCategorias"));
        model.addAttribute("listaUnidades", configuracionService.obtenerListaSanitizada("listaUnidades"));
        model.addAttribute("listaDisponibilidades", configuracionService.obtenerListaSanitizada("listaDisponibilidades"));
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
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("errores", result.getAllErrors());
            model.addAttribute("demanda", demanda);
            injectDynamicOptions(model);
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
    private void cargarOpcionesSelect(Model model, Demanda demanda) {
        // Dynamic Categories
        List<String> categorias = configuracionService.obtenerListaSanitizada("listaCategorias");
        List<SelectOption> opcionesCategoria = new ArrayList<>();
        for(String cat : categorias) {
            opcionesCategoria.add(new SelectOption(cat, cat, cat.equals(demanda.getCategoriaMaterial())));
        }
        model.addAttribute("opcionesCategoria", opcionesCategoria);

        // Dynamic Units
        List<String> unidadesList = configuracionService.obtenerListaSanitizada("listaUnidades");
        List<SelectOption> opcionesUnidad = new ArrayList<>();
        for(String u : unidadesList) {
            opcionesUnidad.add(new SelectOption(u, u, u.equals(demanda.getUnidad())));
        }
        model.addAttribute("opcionesUnidad", opcionesUnidad);

        // Dynamic Urgency (reusing listaDisponibilidades)
        List<String> disponibilidadesList = configuracionService.obtenerListaSanitizada("listaDisponibilidades");
        List<SelectOption> opcionesUrgencia = new ArrayList<>();
        for(String d : disponibilidadesList) {
            opcionesUrgencia.add(new SelectOption(d, d, d.equals(demanda.getUrgencia())));
        }
        model.addAttribute("opcionesUrgencia", opcionesUrgencia);

        // Dynamic Select Options for status
        List<SelectOption> opcionesEstado = new ArrayList<>();
        opcionesEstado.add(new SelectOption("ACTIVA", "ACTIVA", EstadoDemanda.ACTIVA.equals(demanda.getEstado())));
        opcionesEstado.add(new SelectOption("CERRADA", "CERRADA", EstadoDemanda.CERRADA.equals(demanda.getEstado())));
        model.addAttribute("opcionesEstado", opcionesEstado);
    }

    // -------------------------------------------------------------------------
    // GET /demanda/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/demandas/{id}/editar")
    public String mostrarFormularioEditarDemanda(@PathVariable Long id, Model model, Principal principal) {
        Demanda demanda = verificarPropietarioDemanda(id, principal);
        model.addAttribute("demanda", demanda);
        model.addAttribute("isDashboard", true);

        cargarOpcionesSelect(model, demanda);

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

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Demanda demandaExistente = verificarPropietarioDemanda(id, principal);

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            cargarOpcionesSelect(model, demandaForm);
            
            model.addAttribute("errores", result.getAllErrors());
            demandaForm.setId(id);
            return "editar_solicitud";
        }

        demandaExistente.setTitulo(demandaForm.getTitulo());
        demandaExistente.setDescripcion(demandaForm.getDescripcion());
        demandaExistente.setCategoriaMaterial(demandaForm.getCategoriaMaterial());
        demandaExistente.setCantidad(demandaForm.getCantidad());
        demandaExistente.setUnidad(demandaForm.getUnidad());
        demandaExistente.setUrgencia(demandaForm.getUrgencia());
        demandaExistente.setPresupuestoMaximo(demandaForm.getPresupuestoMaximo());
        demandaExistente.setVigencia(demandaForm.getVigencia());
        demandaExistente.setZonaRecogida(demandaForm.getZonaRecogida());
        demandaExistente.setEstado(demandaForm.getEstado());
        
        demandaService.guardar(demandaExistente);

        return "redirect:/dashboard/mis-demandas";
    }
}
