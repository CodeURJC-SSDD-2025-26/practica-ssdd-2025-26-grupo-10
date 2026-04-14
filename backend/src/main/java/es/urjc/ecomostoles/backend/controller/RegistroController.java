package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final EmpresaRepository empresaRepository;

    private final PasswordEncoder passwordEncoder;

    public RegistroController(EmpresaRepository empresaRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
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
            @RequestParam(required = false) MultipartFile logoFile,
            Model model) {

        // Comprobar si ya existe una empresa con ese email
        if (empresaRepository.findByEmailContacto(emailContacto).isPresent()) {
            model.addAttribute("error", true);
            return "registro";
        }

        // Crear nueva empresa
        Empresa empresa = new Empresa();
        empresa.setNombreComercial(nombreComercial);
        empresa.setCif(cif);
        empresa.setDireccion(direccion);
        empresa.setSectorIndustrial(sector);
        empresa.setEmailContacto(emailContacto);

        // ¡CRÍTICO! Encriptar la contraseña antes de guardar
        empresa.setPassword(passwordEncoder.encode(password));

        // Persistir logo como BLOB si el usuario adjuntó uno
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                empresa.setLogo(logoFile.getBytes());
            } catch (Exception e) {
                log.error("⚠️ Error al leer el logo", e);
            }
        }

        empresaRepository.save(empresa);

        return "redirect:/login?registrado";
    }
}
