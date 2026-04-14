package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Mensaje;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.MensajeService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller to handle reading and listing messages.
 * Uses Controller > Service > Repository architecture.
 */
@Controller
public class MensajeController {

    private final EmpresaService empresaService;
    private final MensajeService mensajeService;
    private final OfertaService  ofertaService;

    public MensajeController(EmpresaService empresaService,
                             MensajeService mensajeService,
                             OfertaService  ofertaService) {
        this.empresaService = empresaService;
        this.mensajeService = mensajeService;
        this.ofertaService  = ofertaService;
    }

    /**
     * Shows the inbox messages for the active company.
     *
     * @param model Spring UI model
     * @return view template "mensajes"
     */
    @GetMapping("/mensajes")
    public String mostrarMensajes(Model model, Principal principal) {
        Empresa empresa = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado"));
 
        model.addAttribute("empresa", empresa);
 
        // Recupera lista de mensajes donde la empresa actual es la destinataria
        List<Mensaje> misMensajes = mensajeService.obtenerPorDestinatario(empresa);
        model.addAttribute("mensajes", misMensajes);
 
        return "mensajes";
    }
 
    /**
     * Envia un mensaje al propietario de una oferta.
     */
    @PostMapping("/mensajes/enviar/{ofertaId}")
    public String enviarMensajeOferta(@PathVariable Long ofertaId, @RequestParam String contenido, Principal principal) {
        Oferta oferta = ofertaService.buscarPorId(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada"));
        Empresa remitente = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Remitente no encontrado"));
 
        Empresa destinatario = oferta.getEmpresa();
        String asunto = "Re: " + oferta.getTitulo();
 
        mensajeService.enviarMensaje(asunto, contenido, remitente, destinatario);
 
        return "redirect:/mensajes";
    }
}
