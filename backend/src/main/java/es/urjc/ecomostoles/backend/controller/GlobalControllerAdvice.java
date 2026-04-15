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
        String units = configuracionService.obtenerValorAuto("listaUnidades");
        if (units == null || units.isEmpty()) return java.util.Collections.emptyList();
        return java.util.Arrays.asList(units.split("\\r?\\n"));
    }
    
    @ModelAttribute("listaDisponibilidades")
    public java.util.List<String> listaDisponibilidades() {
        String disp = configuracionService.obtenerValorAuto("listaDisponibilidades");
        if (disp == null || disp.isEmpty()) return java.util.Collections.emptyList();
        return java.util.Arrays.asList(disp.split("\\r?\\n"));
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
