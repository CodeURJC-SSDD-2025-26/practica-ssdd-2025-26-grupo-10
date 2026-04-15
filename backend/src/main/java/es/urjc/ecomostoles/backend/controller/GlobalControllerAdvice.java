package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.MensajeService;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
            return mensajeService.obtenerPorDestinatario(userOpt.get()).size();
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

    @ModelAttribute("listaUnidades")
    public java.util.List<String> listaUnidades() {
        return java.util.List.of("kg", "uds", "toneladas", "m2", "litros");
    }

    @ModelAttribute("listaDisponibilidades")
    public java.util.List<String> listaDisponibilidades() {
        return java.util.List.of("Inmediata", "En 1 semana", "Consultar");
    }
}
