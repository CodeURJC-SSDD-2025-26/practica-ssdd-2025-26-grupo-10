package es.urjc.ecomostoles.backend;

import java.security.Principal;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    private final EmpresaRepository empresaRepository;

    public WebController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }
    @GetMapping("/")
    public String index(Model model, Principal principal) {
        // Solo inyectamos los datos de empresa si el usuario está logueado
        if (principal != null) {
            Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
            empresaOpt.ifPresent(empresa -> model.addAttribute("empresa", empresa));
        }
        return "index";
    }
}
