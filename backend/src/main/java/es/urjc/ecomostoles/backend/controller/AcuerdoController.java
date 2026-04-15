package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import es.urjc.ecomostoles.backend.dto.SelectOption;
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;

/**
 * Controller responsible for displaying and registering new commercial
 * agreements.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to AcuerdoService, EmpresaService and
 * OfertaService.
 */
@Controller
public class AcuerdoController {

    private final AcuerdoService acuerdoService;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;
    private final es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService;

    public AcuerdoController(AcuerdoService acuerdoService,
            EmpresaService empresaService,
            OfertaService ofertaService,
            es.urjc.ecomostoles.backend.service.ConfiguracionService configuracionService) {
        this.acuerdoService = acuerdoService;
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
        this.configuracionService = configuracionService;
    }

    /** Shows the form to register a new agreement. */
    @GetMapping("/acuerdo/nuevo")
    public String mostrarFormularioAcuerdo(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("activeNuevoAcuerdo", true);
            model.addAttribute("isDashboard", true);
            List<OfertaResumen> misOfertas = ofertaService.obtenerPorEmpresa(empresa);
            model.addAttribute("ofertas", misOfertas);

            List<Empresa> todasEmpresas = empresaService.obtenerTodas();
            todasEmpresas.removeIf(e -> e.getId().equals(empresa.getId()));
            model.addAttribute("todasEmpresas", todasEmpresas);

            return "crear_acuerdo";
        }

        return "redirect:/";
    }

    /** Shows the agreement history for the active company. */
    @GetMapping("/acuerdos")
    public String mostrarMisAcuerdos(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("activeAcuerdos", true);
            model.addAttribute("isDashboard", true);
            List<Acuerdo> misAcuerdos = acuerdoService.obtenerPorEmpresa(empresa);
            model.addAttribute("acuerdos", misAcuerdos);

            // Dynamic KPI counts for status section
            model.addAttribute("acuerdosCompletados", acuerdoService.contarPorEmpresaYEstado(empresa, EstadoAcuerdo.COMPLETADO));
            model.addAttribute("acuerdosPendientes", acuerdoService.contarPorEmpresaYEstado(empresa, EstadoAcuerdo.PENDIENTE));

            return "mis_acuerdos";
        }

        return "redirect:/";
    }

    /**
     * Processes the creation of a new agreement with Bean Validation.
     */
    @PostMapping("/acuerdo/nuevo")
    public String registrarAcuerdo(@Valid @ModelAttribute Acuerdo acuerdo,
            BindingResult result,
            @RequestParam(name = "ofertaId", required = false) Long ofertaId,
            @RequestParam(name = "empresaDestinoId", required = false) Long empresaDestinoId,
            Model model,
            Principal principal) {

        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        Empresa empresaLogueada = empresaOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (result.hasErrors()) {
            model.addAttribute("ofertas", ofertaService.obtenerPorEmpresa(empresaLogueada));

            List<Empresa> todasEmpresas = empresaService.obtenerTodas();
            todasEmpresas.removeIf(e -> e.getId().equals(empresaLogueada.getId()));
            model.addAttribute("todasEmpresas", todasEmpresas);

            model.addAttribute("acuerdo", acuerdo);
            model.addAttribute("errores", result.getAllErrors());
            return "crear_acuerdo";
        }

        // ── Business Logic delegated to the Service ────────────────────────────────────
        try {
            if (ofertaId != null && acuerdo != null) {
                acuerdoService.registrarNuevoAcuerdo(acuerdo, principal.getName(), ofertaId, empresaDestinoId);
            }
        } catch (SelfAgreementException e) {
            return "redirect:/mercado?error=AutoAcuerdo";
        }

        return "redirect:/acuerdos";
    }

    /**
     * Shows detail of a specific agreement (with IDOR protection).
     */
    @GetMapping("/acuerdo/{id}")
    public String mostrarDetalleAcuerdo(@PathVariable Long id, Model model, Principal principal) {
        Acuerdo acuerdo = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        String userEmail = principal.getName();
        Empresa logueada = empresaService.buscarPorEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean esAdmin = esAdmin(logueada);
        if (!tienePermisoSobreAcuerdo(acuerdo, logueada, esAdmin)) {
            return "redirect:/acuerdos?error=forbidden";
        }

        model.addAttribute("acuerdo", acuerdo);
        model.addAttribute("isDashboard", true);
        model.addAttribute("esVistaAdmin", esAdmin);
        model.addAttribute("emailSoporte", configuracionService.obtenerValorAuto("emailContacto"));

        return "detalle_acuerdo";
    }

    /**
     * Shows the form to edit an existing agreement.
     */
    @GetMapping("/acuerdos/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Principal principal) {
        Acuerdo acuerdo = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        Empresa logueada = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!tienePermisoSobreAcuerdo(acuerdo, logueada, esAdmin(logueada))) {
            return "redirect:/acuerdos?error=forbidden";
        }

        model.addAttribute("acuerdo", acuerdo);
        model.addAttribute("isDashboard", true);

        // Centralized utility for form options (DRY)
        model.addAttribute("opcionesUnidad", FormOptionsHelper.getOpcionesUnidad(configuracionService, acuerdo.getUnidad()));
        model.addAttribute("opcionesEstado", FormOptionsHelper.getOpcionesEstadoAcuerdo(acuerdo.getEstado()));

        return "editar_acuerdo";
    }

    /**
     * Processes the update of an existing agreement.
     */
    @PostMapping("/acuerdos/{id}/editar")
    public String actualizarAcuerdo(@PathVariable Long id, @Valid @ModelAttribute Acuerdo acuerdoActualizado,
            BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("errores", result.getAllErrors());
            return "editar_acuerdo";
        }

        Acuerdo acuerdoExistente = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        Empresa logueada = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!tienePermisoSobreAcuerdo(acuerdoExistente, logueada, esAdmin(logueada))) {
            return "redirect:/acuerdos?error=forbidden";
        }

        acuerdoService.actualizarAcuerdo(id, acuerdoActualizado);

        return "redirect:/acuerdos/" + id;
    }

    /**
     * Delete an agreement as a user (Cancellation).
     * Security: Verifies that the logged-in company is either the origin or destination.
     */
    @PostMapping("/mis_acuerdos/eliminar/{id}")
    public String eliminarAcuerdoComoUsuario(@PathVariable Long id, Principal principal) {
        Acuerdo acuerdo = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        Empresa logueada = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!tienePermisoSobreAcuerdo(acuerdo, logueada, esAdmin(logueada))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este acuerdo.");
        }

        if (EstadoAcuerdo.COMPLETADO.equals(acuerdo.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden eliminar acuerdos ya completados para preservar el historial de impacto.");
        }

        acuerdoService.eliminar(id);
        return "redirect:/acuerdos?exito=eliminado";
    }
    
    private boolean tienePermisoSobreAcuerdo(Acuerdo acuerdo, Empresa logueada, boolean esAdmin) {
        if (esAdmin) return true;
        String email = logueada.getEmailContacto();
        boolean esOrigen = acuerdo.getEmpresaOrigen() != null && acuerdo.getEmpresaOrigen().getEmailContacto().equals(email);
        boolean esDestino = acuerdo.getEmpresaDestino() != null && acuerdo.getEmpresaDestino().getEmailContacto().equals(email);
        return esOrigen || esDestino;
    }

    private boolean esAdmin(Empresa empresa) {
        return empresa.getRoles() != null && empresa.getRoles().contains("ADMIN");
    }
}
