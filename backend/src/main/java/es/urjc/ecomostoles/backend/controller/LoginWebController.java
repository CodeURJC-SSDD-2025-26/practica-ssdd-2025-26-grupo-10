package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginWebController {

    private final CompanyService companyService;

    public LoginWebController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/login")
    public String login(Model model,
            @RequestParam(required = false) String emailUpdated,
            @RequestParam(name = "error", required = false) String error) {
        if (emailUpdated != null) {
            model.addAttribute("emailActualizado", true);
        }
        if (error != null) {
            model.addAttribute("error", true);
        }
        model.addAttribute("isLogin", true);
        return "login";
    }
}
