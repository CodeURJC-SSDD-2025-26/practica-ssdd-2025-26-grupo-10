package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginWebController {

    private final EmpresaService empresaService;

    public LoginWebController(EmpresaService empresaService) {
        this.empresaService = empresaService;
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
}
