package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import java.util.List;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;

@Controller
public class WebController {

    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public WebController(EmpresaService empresaService, OfertaService ofertaService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        List<OfertaResumen> recientes = ofertaService.obtenerRecientesActivas();
        model.addAttribute("ofertasRecientes", recientes);
        return "index";
    }

    private void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            empresaService.buscarPorEmail(principal.getName())
                          .ifPresent(empresa -> model.addAttribute("empresa", empresa));
        }
    }

    @GetMapping("/terminos")
    public String terminos(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad(Model model, Principal principal) {
        addCommonAttributes(model, principal);
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
