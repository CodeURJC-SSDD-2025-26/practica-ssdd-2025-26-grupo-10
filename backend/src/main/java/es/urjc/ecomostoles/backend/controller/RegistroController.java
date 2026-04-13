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

@Controller
public class RegistroController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        empresaRepository.save(empresa);

        return "redirect:/login?registrado";
    }
}
