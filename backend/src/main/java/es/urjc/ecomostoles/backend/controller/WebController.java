package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import es.urjc.ecomostoles.backend.service.OfertaService;
import java.util.List;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;

@Controller
public class WebController {

    private final OfertaService ofertaService;

    public WebController(OfertaService ofertaService) {
        this.ofertaService  = ofertaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<OfertaResumen> recientes = ofertaService.obtenerRecientesActivas();
        model.addAttribute("ofertasRecientes", recientes);
        return "index";
    }


    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "privacidad";
    }

    @GetMapping("/recuperar-password")
    public String mostrarRecuperarPassword() {
        return "recuperar_password";
    }

    @PostMapping("/recuperar-password")
    public String procesarRecuperarPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        // Lógica de negocio simulada para recuperación de contraseña
        redirectAttributes.addFlashAttribute("mensajeRecuperacion", 
            "Si el correo existe en nuestro sistema, hemos enviado las instrucciones de recuperación.");
        return "redirect:/login";
    }
}
