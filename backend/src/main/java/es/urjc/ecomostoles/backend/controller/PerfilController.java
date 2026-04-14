package es.urjc.ecomostoles.backend.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling company profile view.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to EmpresaService.
 */
@Controller
public class PerfilController {

    private static final Logger log = LoggerFactory.getLogger(PerfilController.class);

    private final EmpresaService empresaService;

    public PerfilController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, @RequestParam(required = false) boolean exito, Principal principal) {
        Empresa empresa = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        model.addAttribute("empresa", empresa);
        if (exito) {
            model.addAttribute("exito", true);
        }
        return "perfil_empresa";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@RequestParam String nombreComercial,
                                @RequestParam String telefono,
                                @RequestParam String direccion,
                                @RequestParam String sector,
                                @RequestParam String descripcion,
                                @RequestParam(required = false) MultipartFile logoFile,
                                Principal principal) {

        Empresa empresa = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        empresa.setNombreComercial(nombreComercial);
        empresa.setTelefono(telefono);
        empresa.setDireccion(direccion);
        empresa.setSectorIndustrial(sector);
        empresa.setDescripcion(descripcion);

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                empresa.setLogo(logoFile.getBytes());
            } catch (Exception e) {
                log.error("⚠️ Error al leer el logo", e);
            }
        }

        empresaService.guardar(empresa);
        return "redirect:/perfil?exito=true";
    }
}
