package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.service.EmailService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginWebController {

    private final EmpresaService empresaService;
    private final EmailService emailService;

    public LoginWebController(EmpresaService empresaService, EmailService emailService) {
        this.empresaService = empresaService;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String login(Model model, 
                        @RequestParam(required = false) String emailActualizado,
                        @RequestParam(name = "error", required = false) String error) {
        if (emailActualizado != null) {
            model.addAttribute("emailActualizado", true);
        }
        if (error != null) {
            model.addAttribute("error", true);
        }
        model.addAttribute("isLogin", true);
        return "login";
    }

    @GetMapping("/recuperar_password")
    public String recuperarPassword(Model model) {
        return "recuperar_password";
    }

    @PostMapping("/recuperar_password")
    public String procesarRecuperarPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        if (empresaService.buscarPorEmail(email).isPresent()) {
            emailService.enviarEmailRecuperacion(email);
        }
        redirectAttributes.addFlashAttribute("mensajeRecuperacion", 
            "Si el correo existe en nuestro sistema, hemos enviado las instrucciones de recuperación.");
        return "redirect:/login";
    }
}
