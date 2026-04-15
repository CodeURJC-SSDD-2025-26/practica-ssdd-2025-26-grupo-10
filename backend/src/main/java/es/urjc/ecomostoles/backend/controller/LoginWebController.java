package es.urjc.ecomostoles.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom authentication entrypoint dispatcher.
 * 
 * Maps the visual login form interface without interfering with the underlying Spring Security 
 * auth-provider mechanisms. Evaluates active-session scanning to gracefully repel redundant 
 * authentications, pushing hot sessions directly towards the bounded dashboard view.
 */
@Controller
public class LoginWebController {





    /**
     * Serves the unauthenticated access challenge view.
     * 
     * @param model MVC template injection container.
     * @param emailUpdated optional UI modifier indicating a recently persisted credential change.
     * @param error intercept flag pushed natively by Spring Security upon bad credentials.
     * @return logical view name mapping to the identity challenge page.
     */
    @GetMapping("/login")
    public String login(Model model,
            @RequestParam(required = false) String emailUpdated,
            @RequestParam(name = "error", required = false) String error) {
        
        // Intercept an existent session mapping to abort duplicate login executions (UX/IdP integrity protection)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        if (emailUpdated != null) {
            model.addAttribute("updatedEmail", true);
        }
        if (error != null) {
            model.addAttribute("error", true);
        }
        model.addAttribute("isLogin", true);
        return "login";
    }
}
