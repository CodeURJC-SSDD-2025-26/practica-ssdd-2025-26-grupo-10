package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle the retrieval and display of demands specific to the logged-in company.
 */
@Controller
public class MisDemandasController {

    private final EmpresaRepository empresaRepository;
    private final DemandaRepository demandaRepository;

    public MisDemandasController(EmpresaRepository empresaRepository, DemandaRepository demandaRepository) {
        this.empresaRepository = empresaRepository;
        this.demandaRepository = demandaRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: devuelve la demanda si el usuario es el autor o ADMIN.
    // Lanza 403 en caso contrario.
    // ─────────────────────────────────────────────────────────────────────────
    private Demanda verificarPropietarioDemanda(Long demandaId, Principal principal) {
        Demanda demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demanda no encontrada: " + demandaId));

        Empresa empresaLogueada = empresaRepository.findByEmailContacto(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean esAdmin = empresaLogueada.getRoles() != null
                && empresaLogueada.getRoles().contains("ADMIN");
        boolean esPropietario = demanda.getEmpresa() != null
                && demanda.getEmpresa().getId().equals(empresaLogueada.getId());

        if (!esAdmin && !esPropietario) {
            System.err.println("🚫 Acceso denegado: " + principal.getName()
                    + " intentó modificar demanda #" + demandaId
                    + " que pertenece a: " + demanda.getEmpresa().getEmailContacto());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta demanda.");
        }
        return demanda;
    }

    /**
     * Displays the "Mis Demandas" page for the active company.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the template name "mis_demandas" or a redirect to home if no company is found
     */
    @GetMapping("/dashboard/mis-demandas")
    public String mostrarMisDemandas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Add the company to the model for the navbar/sidebar
            model.addAttribute("empresa", empresa);

            // Fetch only the demands belonging to this company
            List<Demanda> misDemandas = demandaRepository.findByEmpresa(empresa);
            // Add the list of demands to the model
            model.addAttribute("demandas", misDemandas);

            // Return the view name
            return "mis_demandas";
        }

        // Redirect to home if the company is not found
        return "redirect:/";
    }

    /**
     * Shows the form to create a new demand.
     *
     * @param model the Spring UI model to pass data to the template
     * @return the template name "crear_solicitud" or redirect if company not found
     */
    @GetMapping("/demanda/nueva")
    public String mostrarFormularioNuevaDemanda(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            // Add an empty demand for the form
            model.addAttribute("demanda", new Demanda());
            return "crear_solicitud";
        }
        return "redirect:/";
    }

    /**
     * Processes the creation of a new demand.
     *
     * @param titulo            title of the demand
     * @param descripcion       long description
     * @param categoria         category of material (mapped to categoriaMaterial)
     * @param cantidad          quantity requested
     * @param precioMaximo      max budget for the demand (mapped to presupuestoMaximo)
     * @return redirect to the "Mis Demandas" dashboard
     */
    @PostMapping("/demanda/nueva")
    public String guardarNuevaDemanda(@RequestParam String titulo, 
                                      @RequestParam String descripcion, 
                                      @RequestParam String categoria,
                                      @RequestParam Double cantidad, 
                                      @RequestParam Double precioMaximo,
                                      Principal principal) {

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        
        if (empresaOpt.isPresent()) {
            Demanda nuevaDemanda = new Demanda();
            nuevaDemanda.setTitulo(titulo);
            nuevaDemanda.setDescripcion(descripcion);
            nuevaDemanda.setCategoriaMaterial(categoria);
            nuevaDemanda.setCantidad(cantidad);
            nuevaDemanda.setPresupuestoMaximo(precioMaximo);
            
            // Set author and publication date
            nuevaDemanda.setEmpresa(empresaOpt.get());
            nuevaDemanda.setFechaPublicacion(LocalDateTime.now());
            
            // Set default status
            nuevaDemanda.setEstado("Activa");

            // Save to database
            demandaRepository.save(nuevaDemanda);
            
            // Redirect to the dashboard list
            return "redirect:/dashboard/mis-demandas";
        }

        return "redirect:/";
    }

    /**
     * Deletes a demand.
     *
     * @param id the ID of the demand to delete
     * @return redirect to the "Mis Demandas" dashboard
     */
    @PostMapping("/dashboard/mis-demandas/eliminar/{id}")
    public String eliminarDemanda(@PathVariable Long id, Principal principal) {
        // Verifica propiedad: solo el autor o ADMIN puede borrar
        verificarPropietarioDemanda(id, principal);
        demandaRepository.deleteById(id);
        System.out.println("🗑️  Demanda #" + id + " eliminada por: " + principal.getName());
        return "redirect:/dashboard/mis-demandas";
    }

    /**
     * Shows the form to edit an existing demand.
     *
     * @param id    the ID of the demand to edit
     * @param model the Spring UI model
     * @return the template name "editar_solicitud" or redirect
     */
    @GetMapping("/demanda/editar/{id}")
    public String mostrarFormularioEditarDemanda(@PathVariable Long id, Model model, Principal principal) {
        // Verifica propiedad: solo el autor o ADMIN puede editar
        Demanda demanda = verificarPropietarioDemanda(id, principal);

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            model.addAttribute("demanda", demanda);
            return "editar_solicitud";
        }
        return "redirect:/dashboard/mis-demandas";
    }

    /**
     * Processes the update of an existing demand.
     *
     * @param id                the ID of the demand
     * @param titulo            the new title
     * @param descripcion       the new description
     * @param categoria         the new category
     * @param cantidad          the new quantity
     * @param precioMaximo      the new max budget
     * @return redirect to the "Mis Demandas" dashboard
     */
    @PostMapping("/demanda/editar/{id}")
    public String guardarEdicionDemanda(@PathVariable Long id,
                                        @RequestParam String titulo,
                                        @RequestParam String descripcion,
                                        @RequestParam String categoria,
                                        @RequestParam Double cantidad,
                                        @RequestParam Double precioMaximo) {

        Optional<Demanda> demandaOpt = demandaRepository.findById(id);
        
        if (demandaOpt.isPresent()) {
            Demanda demandaExistente = demandaOpt.get();
            demandaExistente.setTitulo(titulo);
            demandaExistente.setDescripcion(descripcion);
            demandaExistente.setCategoriaMaterial(categoria);
            demandaExistente.setCantidad(cantidad);
            demandaExistente.setPresupuestoMaximo(precioMaximo);
            
            demandaRepository.save(demandaExistente);
        }

        return "redirect:/dashboard/mis-demandas";
    }
}
