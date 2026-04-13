package es.urjc.ecomostoles.backend;

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
    @GetMapping("/") // Cuando alguien entre a "localhost:8080/", se ejecuta este método
    public String index(Model model) {
        // Inject active company context for the navbar
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");
        empresaOpt.ifPresent(empresa -> model.addAttribute("empresa", empresa));

        // Devuelve el nombre de la plantilla sin la extensión. 
        // Spring irá a buscar "index.html" a la carpeta templates.
        return "index"; 
    }
}
