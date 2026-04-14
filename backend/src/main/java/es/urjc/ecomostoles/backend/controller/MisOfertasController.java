package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.EstadoOferta;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages offers for the authenticated user (CRUD + ownership logic).
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to OfertaService and EmpresaService.
 *
 * Ownership rule:
 *   Before editing or deleting an offer, we verify that the logged-in user
 *   is the author company OR has ADMIN role. Returns 403 Forbidden otherwise.
 */
@Controller
public class MisOfertasController {

    private static final Logger log = LoggerFactory.getLogger(MisOfertasController.class);

    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public MisOfertasController(EmpresaService empresaService, OfertaService ofertaService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
    }

    // -------------------------------------------------------------------------
    // Ownership helper: returns the offer if the user is the author or ADMIN.
    // -------------------------------------------------------------------------
    private Oferta verificarPropietario(Long ofertaId, Principal principal) {
        Oferta oferta = ofertaService.buscarPorId(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Oferta no encontrada: " + ofertaId));

        Empresa empresaLogueada = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean esAdmin = empresaLogueada.getRoles() != null
                && empresaLogueada.getRoles().contains("ADMIN");
        boolean esPropietario = oferta.getEmpresa() != null
                && oferta.getEmpresa().getId().equals(empresaLogueada.getId());

        if (!esAdmin && !esPropietario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta oferta.");
        }

        return oferta;
    }

    // -------------------------------------------------------------------------
    // GET /dashboard/mis-ofertas
    // -------------------------------------------------------------------------
    @GetMapping("/dashboard/mis-ofertas")
    public String mostrarMisOfertas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            List<OfertaResumen> misOfertas = ofertaService.obtenerPorEmpresa(empresa);
            model.addAttribute("ofertas", misOfertas);
            return "mis_activos";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // GET /oferta/nueva — Show new offer form
    // -------------------------------------------------------------------------
    @GetMapping("/oferta/nueva")
    public String mostrarFormularioNuevaOferta(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            model.addAttribute("oferta", new Oferta());
            return "crear_activo";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /oferta/nueva — Create new offer with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/oferta/nueva")
    public String guardarNuevaOferta(@Valid @ModelAttribute("oferta") Oferta oferta,
                                     BindingResult result,
                                     @RequestParam(required = false) MultipartFile imagenFile,
                                     Model model,
                                     Principal principal) {

        if (result.hasErrors()) {
            Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
            empresaOpt.ifPresent(e -> model.addAttribute("empresa", e));
            model.addAttribute("errores", result.getAllErrors());
            return "crear_activo";
        }

        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
        if (empresaOpt.isPresent()) {
            oferta.setEmpresa(empresaOpt.get());
            oferta.setFechaPublicacion(LocalDateTime.now());
            oferta.setEstado(EstadoOferta.ACTIVA);

            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    oferta.setImagen(imagenFile.getBytes());
                } catch (Exception e) {
                    log.error("Error reading image", e);
                }
            }

            ofertaService.guardar(oferta);
            return "redirect:/dashboard/mis-ofertas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /ofertas/{id}/eliminar — Delete offer (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/ofertas/{id}/eliminar")
    public String eliminarOferta(@PathVariable Long id, Principal principal) {
        verificarPropietario(id, principal);
        ofertaService.eliminar(id);
        return "redirect:/dashboard/mis-ofertas";
    }

    // -------------------------------------------------------------------------
    // GET /oferta/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/ofertas/{id}/editar")
    public String mostrarFormularioEditarOferta(@PathVariable Long id,
                                                Model model,
                                                Principal principal) {
        Oferta oferta = verificarPropietario(id, principal);
        empresaService.buscarPorEmail(principal.getName())
                      .ifPresent(e -> model.addAttribute("empresa", e));
        model.addAttribute("oferta", oferta);
        return "editar_activo";
    }

    // -------------------------------------------------------------------------
    // POST /oferta/editar/{id} — Save changes with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/ofertas/{id}/editar")
    public String guardarCambiosOferta(@PathVariable Long id,
                                       @Valid @ModelAttribute("oferta") Oferta ofertaForm,
                                       BindingResult result,
                                       @RequestParam(required = false) MultipartFile imagenFile,
                                       Model model,
                                       Principal principal) {

        if (result.hasErrors()) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(e -> model.addAttribute("empresa", e));
            model.addAttribute("errores", result.getAllErrors());
            ofertaForm.setId(id);
            return "editar_activo";
        }

        Oferta oferta = verificarPropietario(id, principal);

        oferta.setTitulo(ofertaForm.getTitulo());
        oferta.setDescripcion(ofertaForm.getDescripcion());
        oferta.setCantidad(ofertaForm.getCantidad());
        oferta.setPrecio(ofertaForm.getPrecio());
        oferta.setTipoResiduo(ofertaForm.getTipoResiduo());

        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                oferta.setImagen(imagenFile.getBytes());
            } catch (Exception e) {
                log.error("Error reading updated image", e);
            }
        }

        ofertaService.guardar(oferta);
        return "redirect:/dashboard/mis-ofertas";
    }
}
