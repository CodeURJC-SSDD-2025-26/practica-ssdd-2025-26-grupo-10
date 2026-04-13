package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle the retrieval and display of offers specific to the logged-in company.
 */
@Controller
public class MisOfertasController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;

    /**
     * Constructor-based dependency injection.
     * @param empresaRepository repository for company data
     * @param ofertaRepository repository for offer data
     */
    public MisOfertasController(EmpresaRepository empresaRepository, OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
    }

    /**
     * Displays the "My Offers" page for the active company.
     * 
     * @param model the Spring UI model to pass data to the template
     * @return the template name "mis_activos" or a redirect to home if no company is found
     */
    @GetMapping("/dashboard/mis-ofertas")
    public String mostrarMisOfertas(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Add the company to the model for the navbar/sidebar
            model.addAttribute("empresa", empresa);

            // Fetch only the offers belonging to this company
            List<Oferta> misOfertas = ofertaRepository.findByEmpresa(empresa);
            // Add the list of offers to the model
            model.addAttribute("ofertas", misOfertas);

            // Return the view name
            return "mis_activos";
        }

        // Redirect to home if the company is not found
        return "redirect:/";
    }

    /**
     * Shows the form to create a new offer.
     *
     * @param model the Spring UI model to pass data to the template
     * @return the template name "crear_activo" or redirect if company not found
     */
    @GetMapping("/oferta/nueva")
    public String mostrarFormularioNuevaOferta(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        if (empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            // Add an empty offer for the form
            model.addAttribute("oferta", new Oferta());
            return "crear_activo";
        }
        return "redirect:/";
    }

    /**
     * Processes the creation of a new offer.
     *
     * @param titulo      title of the offer
     * @param descripcion long description
     * @param cantidad    quantity offered
     * @param precio      price of the offer
     * @param categoria   type of waste (mapped to tipoResiduo)
     * @return redirect to the "My Offers" dashboard
     */
    @PostMapping("/oferta/nueva")
    public String guardarNuevaOferta(@RequestParam String titulo, 
                                     @RequestParam String descripcion, 
                                     @RequestParam Double cantidad, 
                                     @RequestParam Double precio, 
                                     @RequestParam String categoria,
                                     Principal principal) {

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
        
        if (empresaOpt.isPresent()) {
            Oferta nuevaOferta = new Oferta();
            nuevaOferta.setTitulo(titulo);
            nuevaOferta.setDescripcion(descripcion);
            nuevaOferta.setCantidad(cantidad);
            nuevaOferta.setPrecio(precio);
            nuevaOferta.setTipoResiduo(categoria); // category from form maps to tipoResiduo
            
            // Set author and publication date
            nuevaOferta.setEmpresa(empresaOpt.get());
            nuevaOferta.setFechaPublicacion(LocalDateTime.now());
            
            // Set default status
            nuevaOferta.setEstado("Activo");

            // Save to database
            ofertaRepository.save(nuevaOferta);
            
            // Redirect to the dashboard list
            return "redirect:/dashboard/mis-ofertas";
        }

        return "redirect:/";
    }

    /**
     * Deletes an offer by its unique identifier.
     *
     * @param id the ID of the offer to delete
     * @return redirect to the "My Offers" dashboard
     */
    @PostMapping("/dashboard/mis-ofertas/eliminar/{id}")
    public String eliminarOferta(@PathVariable Long id) {
        // Delete the offer using the repository
        ofertaRepository.deleteById(id);
        
        // Redirect back to the offers list
        return "redirect:/dashboard/mis-ofertas";
    }

    /**
     * Shows the form to edit an existing offer.
     *
     * @param id    the ID of the offer to edit
     * @param model the Spring UI model to pass data to the template
     * @return the template name "editar_activo" if found, else redirects to dashboard
     */
    @GetMapping("/oferta/editar/{id}")
    public String mostrarFormularioEditarOferta(@PathVariable Long id, Model model, Principal principal) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(id);
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (ofertaOpt.isPresent() && empresaOpt.isPresent()) {
            model.addAttribute("empresa", empresaOpt.get());
            model.addAttribute("oferta", ofertaOpt.get());
            return "editar_activo"; // Returns the edit form view
        }

        // Redirect to the dashboard if the offer or company context is missing
        return "redirect:/dashboard/mis-ofertas";
    }

    /**
     * Processes the submission of the updated offer form.
     *
     * @param id          the ID of the offer to update
     * @param titulo      updated title
     * @param descripcion updated description
     * @param cantidad    updated quantity
     * @param precio      updated price
     * @param categoria   updated type of waste (mapped to tipoResiduo)
     * @return redirect to the "My Offers" dashboard
     */
    @PostMapping("/oferta/editar/{id}")
    public String guardarCambiosOferta(@PathVariable Long id,
                                       @RequestParam String titulo,
                                       @RequestParam String descripcion,
                                       @RequestParam Double cantidad,
                                       @RequestParam Double precio,
                                       @RequestParam String categoria) {

        // Retrieve the existing offer from the database
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(id);
        
        if (ofertaOpt.isPresent()) {
            Oferta ofertaExistente = ofertaOpt.get();
            // Update fields with new data from the form
            ofertaExistente.setTitulo(titulo);
            ofertaExistente.setDescripcion(descripcion);
            ofertaExistente.setCantidad(cantidad);
            ofertaExistente.setPrecio(precio);
            ofertaExistente.setTipoResiduo(categoria);

            // Save the updated offer back to the database
            ofertaRepository.save(ofertaExistente);
            
            // Redirect back to the offers list
            return "redirect:/dashboard/mis-ofertas";
        }

        // Redirect to the dashboard if the offer is not found
        return "redirect:/dashboard/mis-ofertas";
    }
}
