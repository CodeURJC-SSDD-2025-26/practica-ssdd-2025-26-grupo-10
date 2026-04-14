package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;

/**
 * Controller for handling company profile view.
 */
@Controller
public class PerfilController {

    private final EmpresaRepository empresaRepository;

    /**
     * Injecting EmpresaRepository via constructor for improved testability.
     */
    public PerfilController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    /**
     * Shows the company profile.
     * 
     * @param model result model to add company data
     * @return the profile view name (perfil_empresa.html) or a redirect to home if not found
     */
    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, @RequestParam(required = false) boolean exito, Principal principal) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            // Adding company object to the model for Mustache rendering
            model.addAttribute("empresa", empresaOpt.get());
            
            // Adding success message flag if present in URL
            if (exito) {
                model.addAttribute("exito", true);
            }
            
            // Return the corresponding view name
            return "perfil_empresa";
        }

        // Return a redirect to home if the company doesn't exist
        return "redirect:/";
    }

    /**
     * Saves the company profile data.
     * 
     * @param nombreComercial the commercial name of the company
     * @param telefono the phone number
     * @param direccion the address
     * @param sector the industrial sector (mapped to sectorIndustrial)
     * @param descripcion the description
     * @return a redirect to the profile page with a success parameter
     */
    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@RequestParam String nombreComercial,
                                @RequestParam String telefono,
                                @RequestParam String direccion,
                                @RequestParam String sector,
                                @RequestParam String descripcion,
                                @RequestParam(required = false) MultipartFile logoFile,
                                Principal principal) {

        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            // Updating fields from request parameters
            empresa.setNombreComercial(nombreComercial);
            empresa.setTelefono(telefono);
            empresa.setDireccion(direccion);
            empresa.setSectorIndustrial(sector);
            empresa.setDescripcion(descripcion);

            // Actualizar logo solo si se ha enviado un archivo nuevo
            if (logoFile != null && !logoFile.isEmpty()) {
                try {
                    empresa.setLogo(logoFile.getBytes());
                } catch (Exception e) {
                    System.err.println("⚠️ Error al leer el logo: " + e.getMessage());
                }
            }
            // Si logoFile está vacío, se conserva el logo existente en BBDD

            // Persisting changes to the database
            empresaRepository.save(empresa);

            // Redirect indicating success
            return "redirect:/perfil?exito=true";
        }

        // Redirect back if something goes wrong
        return "redirect:/perfil?error=true";
    }
}
