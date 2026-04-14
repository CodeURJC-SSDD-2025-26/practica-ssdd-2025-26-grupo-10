package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.security.Principal;
import es.urjc.ecomostoles.backend.service.EmpresaService;

@Controller
public class WebController {

    private final EmpresaService empresaService;

    public WebController(EmpresaService empresaService) {
        this.empresaService = empresaService;
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
}
