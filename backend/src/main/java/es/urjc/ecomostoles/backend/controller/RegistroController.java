package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class RegistroController {

    private static final Logger log = LoggerFactory.getLogger(RegistroController.class);

    private final EmpresaService empresaService;

    public RegistroController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarEmpresa(
            @RequestParam String nombreComercial,
            @RequestParam String cif,
            @RequestParam String direccion,
            @RequestParam String sector,
            @RequestParam String emailContacto,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) MultipartFile logoFile,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden.");
            // Repoblar modelo para no perder datos
            model.addAttribute("nombreComercial", nombreComercial);
            model.addAttribute("cif", cif);
            model.addAttribute("direccion", direccion);
            model.addAttribute("sector", sector);
            model.addAttribute("emailContacto", emailContacto);
            return "registro";
        }

        try {
            // Mapping fields to new Empresa object
            Empresa nuevaEmpresa = new Empresa();
            nuevaEmpresa.setNombreComercial(nombreComercial);
            nuevaEmpresa.setCif(cif);
            nuevaEmpresa.setDireccion(direccion);
            nuevaEmpresa.setSectorIndustrial(sector);
            nuevaEmpresa.setEmailContacto(emailContacto);

            byte[] logoBytes = null;
            if (logoFile != null && !logoFile.isEmpty()) {
                logoBytes = logoFile.getBytes();
            }

            // Delegating logic to Service (Professional pattern)
            empresaService.registrarNuevaEmpresa(nuevaEmpresa, password, logoBytes);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            return "registro";
        } catch (Exception e) {
            log.error("⚠️ Error general en el registro de empresa", e);
            model.addAttribute("error", true);
            return "registro";
        }

        return "redirect:/login?registrado";
    }
}
