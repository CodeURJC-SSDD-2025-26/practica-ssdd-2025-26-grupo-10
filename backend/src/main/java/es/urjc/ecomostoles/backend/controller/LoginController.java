package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String emailActualizado) {
        if (emailActualizado != null) {
            model.addAttribute("emailActualizado", true);
        }
        return "login";
    }
}
