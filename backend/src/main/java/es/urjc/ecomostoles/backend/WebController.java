package es.urjc.ecomostoles.backend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/") // Cuando alguien entre a "localhost:8080/", se ejecuta este método
    public String index(Model model) {
        
        // Devuelve el nombre de la plantilla sin la extensión. 
        // Spring irá a buscar "index.html" a la carpeta templates.
        return "index"; 
    }
}
