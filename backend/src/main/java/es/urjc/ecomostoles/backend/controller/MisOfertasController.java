package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona las ofertas del usuario autenticado (CRUD + lógica de propiedad).
 *
 * Lógica de propiedad (Ownership):
 *   Antes de editar o eliminar una oferta se verifica que el usuario logueado
 *   sea la empresa autora O tenga rol ADMIN. Si no, se devuelve 403 Forbidden.
 */
@Controller
public class MisOfertasController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository  ofertaRepository;

    public MisOfertasController(EmpresaRepository empresaRepository,
                                 OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository  = ofertaRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper de propiedad: devuelve la oferta si el usuario es el autor o ADMIN.
    // Lanza 403 en caso contrario.
    // ─────────────────────────────────────────────────────────────────────────
    private Oferta verificarPropietario(Long ofertaId, Principal principal) {
        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Oferta no encontrada: " + ofertaId));

        // Obtener la empresa del usuario logueado
        Empresa empresaLogueada = empresaRepository.findByEmailContacto(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean esAdmin = empresaLogueada.getRoles() != null
                && empresaLogueada.getRoles().contains("ADMIN");
        boolean esPropietario = oferta.getEmpresa() != null
                && oferta.getEmpresa().getId().equals(empresaLogueada.getId());

        if (!esAdmin && !esPropietario) {
            System.err.println("🚫 Acceso denegado: " + principal.getName()
                    + " intentó modificar oferta #" + ofertaId
                    + " que pertenece a: " + oferta.getEmpresa().getEmailContacto());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta oferta.");
        }

        return oferta;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /dashboard/mis-ofertas
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/dashboard/mis-ofertas")
    public String mostrarMisOfertas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            List<Oferta> misOfertas = ofertaRepository.findByEmpresa(empresa);
            model.addAttribute("ofertas", misOfertas);
            return "mis_activos";
        }
        return "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /oferta/nueva — Formulario nueva oferta
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/oferta/nueva")
    public String mostrarFormularioNuevaOferta(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            model.addAttribute("oferta", new Oferta());
            return "crear_activo";
        }
        return "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /oferta/nueva — Crear nueva oferta
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/oferta/nueva")
    public String guardarNuevaOferta(@RequestParam String titulo,
                                     @RequestParam String descripcion,
                                     @RequestParam Double cantidad,
                                     @RequestParam Double precio,
                                     @RequestParam String categoria,
                                     @RequestParam(required = false) MultipartFile imagenFile,
                                     Principal principal) {

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            Oferta nuevaOferta = new Oferta();
            nuevaOferta.setTitulo(titulo);
            nuevaOferta.setDescripcion(descripcion);
            nuevaOferta.setCantidad(cantidad);
            nuevaOferta.setPrecio(precio);
            nuevaOferta.setTipoResiduo(categoria);
            nuevaOferta.setEmpresa(empresaOpt.get());
            nuevaOferta.setFechaPublicacion(LocalDateTime.now());
            nuevaOferta.setEstado("Activa");

            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    nuevaOferta.setImagen(imagenFile.getBytes());
                } catch (Exception e) {
                    System.err.println("⚠️ Error al leer la imagen: " + e.getMessage());
                }
            }

            ofertaRepository.save(nuevaOferta);
            return "redirect:/dashboard/mis-ofertas";
        }
        return "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /dashboard/mis-ofertas/eliminar/{id}
    // ── Lógica de propiedad: solo el autor o un ADMIN pueden borrar ──────────
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/dashboard/mis-ofertas/eliminar/{id}")
    public String eliminarOferta(@PathVariable Long id, Principal principal) {
        // verificarPropietario lanza 403 si el usuario no tiene permiso
        verificarPropietario(id, principal);

        ofertaRepository.deleteById(id);
        System.out.println("🗑️  Oferta #" + id + " eliminada por: " + principal.getName());
        return "redirect:/dashboard/mis-ofertas";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /oferta/editar/{id} — Formulario edición
    // ── Lógica de propiedad: solo el autor o un ADMIN ────────────────────────
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/oferta/editar/{id}")
    public String mostrarFormularioEditarOferta(@PathVariable Long id,
                                                Model model,
                                                Principal principal) {
        // verificarPropietario lanza 403 si el usuario no tiene permiso
        Oferta oferta = verificarPropietario(id, principal);

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        empresaOpt.ifPresent(e -> model.addAttribute("empresa", e));
        model.addAttribute("oferta", oferta);
        return "editar_activo";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /oferta/editar/{id} — Guardar cambios
    // ── Lógica de propiedad: solo el autor o un ADMIN ────────────────────────
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/oferta/editar/{id}")
    public String guardarCambiosOferta(@PathVariable Long id,
                                       @RequestParam String titulo,
                                       @RequestParam String descripcion,
                                       @RequestParam Double cantidad,
                                       @RequestParam Double precio,
                                       @RequestParam String categoria,
                                       @RequestParam(required = false) MultipartFile imagenFile,
                                       Principal principal) {

        // verificarPropietario lanza 403 si el usuario no tiene permiso
        Oferta oferta = verificarPropietario(id, principal);

        oferta.setTitulo(titulo);
        oferta.setDescripcion(descripcion);
        oferta.setCantidad(cantidad);
        oferta.setPrecio(precio);
        oferta.setTipoResiduo(categoria);

        // Solo actualizar imagen si se envía una nueva; si no, se conserva la BLOB
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                oferta.setImagen(imagenFile.getBytes());
            } catch (Exception e) {
                System.err.println("⚠️ Error al leer imagen actualizada: " + e.getMessage());
            }
        }

        ofertaRepository.save(oferta);
        System.out.println("✏️  Oferta #" + id + " editada por: " + principal.getName());
        return "redirect:/dashboard/mis-ofertas";
    }
}
