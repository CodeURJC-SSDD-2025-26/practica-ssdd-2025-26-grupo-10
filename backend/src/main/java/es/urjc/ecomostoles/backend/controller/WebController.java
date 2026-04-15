package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import es.urjc.ecomostoles.backend.service.EmailService;
import java.util.List;

@Controller
public class WebController {

    private final OfertaService  ofertaService;
    private final EmpresaService empresaService;
    private final AcuerdoService acuerdoService;
    private final EmailService   emailService;

    public WebController(OfertaService ofertaService, 
                         EmpresaService empresaService, 
                         AcuerdoService acuerdoService,
                         EmailService emailService) {
        this.ofertaService  = ofertaService;
        this.empresaService = empresaService;
        this.acuerdoService = acuerdoService;
        this.emailService   = emailService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<OfertaResumen> recientes = ofertaService.obtenerRecientesActivas();
        model.addAttribute("ofertasRecientes", recientes);
        
        // Injects real metrics from the database
        model.addAttribute("totalEmpresas", empresaService.contarTodas());
        model.addAttribute("totalOfertas",   ofertaService.contarTodas());
        model.addAttribute("totalCo2",       acuerdoService.calcularCO2Ahorrado());
        
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
        // Verifies if the email exists in the DB before "sending"
        if (empresaService.buscarPorEmail(email).isPresent()) {
            emailService.enviarEmailRecuperacion(email);
        }
        
        // Keep the generic message for security (prevent user enumeration)
        redirectAttributes.addFlashAttribute("mensajeRecuperacion", 
            "Si el correo existe en nuestro sistema, hemos enviado las instrucciones de recuperación.");
        return "redirect:/login";
    }
}
