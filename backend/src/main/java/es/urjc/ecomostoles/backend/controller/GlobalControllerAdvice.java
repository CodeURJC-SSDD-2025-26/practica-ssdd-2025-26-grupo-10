package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.MensajeService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final EmpresaService empresaService;
    private final MensajeService mensajeService;

    public GlobalControllerAdvice(EmpresaService empresaService, MensajeService mensajeService) {
        this.empresaService = empresaService;
        this.mensajeService = mensajeService;
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
}
