package es.urjc.ecomostoles.backend.controller;


import es.urjc.ecomostoles.backend.model.Acuerdo;
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

/**
 * Controller responsible for displaying and registering new commercial agreements.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to AcuerdoService, EmpresaService and OfertaService.
 */
@Controller
public class AcuerdoController {

    private final AcuerdoService acuerdoService;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public AcuerdoController(AcuerdoService acuerdoService,
                             EmpresaService empresaService,
                             OfertaService ofertaService) {
        this.acuerdoService = acuerdoService;
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
    }

    /** Shows the form to register a new agreement. */
    @GetMapping("/acuerdo/nuevo")
    public String mostrarFormularioAcuerdo(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            model.addAttribute("activeNuevoAcuerdo", true);
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
            model.addAttribute("empresa", empresa);
            model.addAttribute("activeAcuerdos", true);
            List<Acuerdo> misAcuerdos = acuerdoService.obtenerPorEmpresa(empresa);
            model.addAttribute("acuerdos", misAcuerdos);

            // Dynamic KPI counts for status section
            model.addAttribute("acuerdosCompletados", acuerdoService.contarPorEmpresaYEstado(empresa, "COMPLETADO"));
            model.addAttribute("acuerdosPendientes",  acuerdoService.contarPorEmpresaYEstado(empresa, "PENDIENTE"));
            
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
            model.addAttribute("empresa", empresaLogueada);
            model.addAttribute("ofertas", ofertaService.obtenerPorEmpresa(empresaLogueada));

            List<Empresa> todasEmpresas = empresaService.obtenerTodas();
            todasEmpresas.removeIf(e -> e.getId().equals(empresaLogueada.getId()));
            model.addAttribute("todasEmpresas", todasEmpresas);

            model.addAttribute("acuerdo", acuerdo);
            model.addAttribute("errores", result.getAllErrors());
            return "crear_acuerdo";
        }

        // ── Lógica de Negocio delegada al Service ────────────────────────────────────
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
    @GetMapping("/acuerdos/{id}")
    public String mostrarDetalleAcuerdo(@PathVariable Long id, Model model, Principal principal) {
        Acuerdo acuerdo = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        String userEmail = principal.getName();
        Empresa logueada = empresaService.buscarPorEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean esAdmin = logueada.getRoles() != null && logueada.getRoles().contains("ADMIN");
        boolean esOrigen = acuerdo.getEmpresaOrigen() != null && acuerdo.getEmpresaOrigen().getEmailContacto().equals(userEmail);
        boolean esDestino = acuerdo.getEmpresaDestino() != null && acuerdo.getEmpresaDestino().getEmailContacto().equals(userEmail);

        if (!esAdmin && !esOrigen && !esDestino) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este acuerdo");
        }

        model.addAttribute("acuerdo", acuerdo);
        model.addAttribute("empresa", logueada);
        return "detalle_acuerdo";
    }
 
    /**
     * Shows the form to edit an existing agreement.
     */
    @GetMapping("/acuerdos/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Principal principal) {
        Acuerdo acuerdo = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));
 
        String userEmail = principal.getName();
        boolean esOrigen = acuerdo.getEmpresaOrigen() != null && acuerdo.getEmpresaOrigen().getEmailContacto().equals(userEmail);
        boolean esDestino = acuerdo.getEmpresaDestino() != null && acuerdo.getEmpresaDestino().getEmailContacto().equals(userEmail);
 
        if (!esOrigen && !esDestino) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este acuerdo");
        }
 
        model.addAttribute("acuerdo", acuerdo);

        // Dynamic Select Options for unit
        List<SelectOption> opcionesUnidad = new ArrayList<>();
        opcionesUnidad.add(new SelectOption("kg", "kg", "kg".equals(acuerdo.getUnidad())));
        opcionesUnidad.add(new SelectOption("ton", "toneladas", "ton".equals(acuerdo.getUnidad())));
        opcionesUnidad.add(new SelectOption("uds", "unidades", "uds".equals(acuerdo.getUnidad())));
        opcionesUnidad.add(new SelectOption("m2", "m²", "m2".equals(acuerdo.getUnidad())));
        opcionesUnidad.add(new SelectOption("L", "litros", "L".equals(acuerdo.getUnidad())));
        model.addAttribute("opcionesUnidad", opcionesUnidad);

        // Dynamic Select Options for status
        List<SelectOption> opcionesEstado = new ArrayList<>();
        String estadoActual = (acuerdo.getEstado() != null) ? acuerdo.getEstado().name() : "";
        
        opcionesEstado.add(new SelectOption("PENDIENTE", "Pendiente de firma", "PENDIENTE".equals(estadoActual)));
        opcionesEstado.add(new SelectOption("EN_CURSO", "En curso / Procesando", "EN_CURSO".equals(estadoActual)));
        opcionesEstado.add(new SelectOption("COMPLETADO", "Completado / Finalizado", "COMPLETADO".equals(estadoActual)));
        opcionesEstado.add(new SelectOption("ACEPTADO", "Aceptado", "ACEPTADO".equals(estadoActual)));
        opcionesEstado.add(new SelectOption("RECHAZADO", "Rechazado", "RECHAZADO".equals(estadoActual)));
        model.addAttribute("opcionesEstado", opcionesEstado);

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
 
        String userEmail = principal.getName();
        boolean esOrigen = acuerdoExistente.getEmpresaOrigen() != null && acuerdoExistente.getEmpresaOrigen().getEmailContacto().equals(userEmail);
        boolean esDestino = acuerdoExistente.getEmpresaDestino() != null && acuerdoExistente.getEmpresaDestino().getEmailContacto().equals(userEmail);
 
        if (!esOrigen && !esDestino) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este acuerdo");
        }
 
        // Update allowed fields
        acuerdoExistente.setMaterialIntercambiado(acuerdoActualizado.getMaterialIntercambiado());
        acuerdoExistente.setCantidad(acuerdoActualizado.getCantidad());
        acuerdoExistente.setUnidad(acuerdoActualizado.getUnidad());
        acuerdoExistente.setPrecioAcordado(acuerdoActualizado.getPrecioAcordado());
        acuerdoExistente.setFechaRecogida(acuerdoActualizado.getFechaRecogida());
        acuerdoExistente.setEstado(acuerdoActualizado.getEstado());
 
        acuerdoService.guardar(acuerdoExistente);
 
        return "redirect:/acuerdos/" + id;
    }
}
