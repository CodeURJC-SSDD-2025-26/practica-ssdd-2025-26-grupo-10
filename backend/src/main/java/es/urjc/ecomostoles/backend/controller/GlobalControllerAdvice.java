package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.MensajeService;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final EmpresaService empresaService;
    private final MensajeService mensajeService;
    private final ConfiguracionService configuracionService;

    public GlobalControllerAdvice(EmpresaService empresaService, MensajeService mensajeService, ConfiguracionService configuracionService) {
        this.empresaService = empresaService;
        this.mensajeService = mensajeService;
        this.configuracionService = configuracionService;
    }

    @ModelAttribute("empresa")
    public es.urjc.ecomostoles.backend.dto.EmpresaDTO user(Principal principal) {
        if (principal == null) {
            return null;
        }
        return empresaService.buscarPorEmail(principal.getName())
                             .map(es.urjc.ecomostoles.backend.dto.EmpresaDTO::new)
                             .orElse(null);
    }

    @ModelAttribute("totalMensajes")
    public int totalMensajes(Principal principal) {
        if (principal == null) {
            return 0;
        }
        Optional<Empresa> userOpt = empresaService.buscarPorEmail(principal.getName());
        if (userOpt.isPresent()) {
            // Optimized: Atomic count from DB instead of loading all messages to memory
            return (int) mensajeService.contarPorDestinatario(userOpt.get());
        }
        return 0;
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return java.time.LocalDate.now().getYear();
    }

    @ModelAttribute("emailSoporte")
    public String emailSoporte() {
        return configuracionService.obtenerValorConfiguracion("emailContacto", "soporte@ecomostoles.com");
    }

    @ModelAttribute("currentDate")
    public String currentDate() {
        return java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES"))
                .format(java.time.LocalDate.now());
    }

    @ModelAttribute("listaUnidades")
    public java.util.List<String> listaUnidades() {
        return configuracionService.obtenerListaSanitizada("listaUnidades");
    }
    
    @ModelAttribute("listaDisponibilidades")
    public java.util.List<String> listaDisponibilidades() {
        return configuracionService.obtenerListaSanitizada("listaDisponibilidades");
    }

    @ModelAttribute("listaCategorias")
    public java.util.List<String> listaCategorias() {
        return configuracionService.obtenerListaSanitizada("listaCategorias");
    }

    @ModelAttribute("listaSectores")
    public java.util.List<String> listaSectores() {
        return configuracionService.obtenerListaSanitizada("listaSectores");
    }

    @ModelAttribute("unidadPrincipal")
    public String unidadPrincipal() {
        java.util.List<String> unidades = configuracionService.obtenerListaSanitizada("listaUnidades");
        return unidades.isEmpty() ? "kg" : unidades.get(0);
    }

    @ModelAttribute("platformName")
    public String getPlatformName() {
        return "EcoMóstoles";
    }

    @ModelAttribute("platformCity")
    public String platformCity() {
        return configuracionService.obtenerValorAuto("platformCity");
    }

    @ModelAttribute("platformLocation")
    public String platformLocation() {
        return configuracionService.obtenerValorAuto("platformLocation");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }

    @ModelAttribute("socialLinkedin")
    public String socialLinkedin() {
        return configuracionService.obtenerValorAuto("social_linkedin");
    }

    @ModelAttribute("socialTwitter")
    public String socialTwitter() {
        return configuracionService.obtenerValorAuto("social_twitter");
    }

    @ModelAttribute("socialFacebook")
    public String socialFacebook() {
        return configuracionService.obtenerValorAuto("social_facebook");
    }

    @ModelAttribute("platformStatus")
    public String platformStatus() {
        return configuracionService.obtenerValorAuto("platformStatus");
    }

    @ModelAttribute("isEmpresa")
    public boolean isEmpresa(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("EMPRESA"));
    }

    @ModelAttribute("isAdminPanel")
    public boolean isAdminPanel(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/admin");
    }
}
