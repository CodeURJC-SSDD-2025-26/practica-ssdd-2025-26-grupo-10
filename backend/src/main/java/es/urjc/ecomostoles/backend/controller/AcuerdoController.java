package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.EstadoOferta;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
            List<Oferta> misOfertas = ofertaService.obtenerPorEmpresa(empresa);
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
            List<Acuerdo> misAcuerdos = acuerdoService.obtenerPorEmpresa(empresa);
            model.addAttribute("acuerdos", misAcuerdos);
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

        if (result.hasErrors()) {
            Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
            if (empresaOpt.isPresent()) {
                Empresa empresa = empresaOpt.get();
                model.addAttribute("empresa", empresa);
                model.addAttribute("ofertas", ofertaService.obtenerPorEmpresa(empresa));

                List<Empresa> todasEmpresas = empresaService.obtenerTodas();
                todasEmpresas.removeIf(e -> e.getId().equals(empresa.getId()));
                model.addAttribute("todasEmpresas", todasEmpresas);
            }
            model.addAttribute("errores", result.getAllErrors());
            return "crear_acuerdo";
        }

        // ── Lógica de Negocio delegada al Service ────────────────────────────────────
        if (ofertaId != null && acuerdo != null) {
            acuerdoService.registrarNuevoAcuerdo(acuerdo, principal.getName(), ofertaId, empresaDestinoId);
        }

        return "redirect:/acuerdos";
    }
}
