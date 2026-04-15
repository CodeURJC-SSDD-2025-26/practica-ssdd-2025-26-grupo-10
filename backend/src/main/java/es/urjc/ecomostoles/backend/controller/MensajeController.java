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
 
        model.addAttribute("activeMensajes", true);
        model.addAttribute("isDashboard", true);
 
        // Retrieves messages where the current company is the recipient or the sender
        List<Mensaje> recibidos = mensajeService.obtenerPorDestinatario(empresa);
        List<Mensaje> enviados  = mensajeService.obtenerPorRemitente(empresa);

        model.addAttribute("mensajesRecibidos", recibidos);
        model.addAttribute("mensajesEnviados",  enviados);
        model.addAttribute("mensajes",          recibidos); // for backward compatibility in templates if needed
 
        return "mensajes";
    }

    /**
     * Shows the detail of a specific message.
     */
    @GetMapping("/mensajes/{id}")
    public String mostrarDetalleMensaje(@PathVariable Long id, Model model, Principal principal) {
        Mensaje mensaje = mensajeService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
 
        Empresa empresa = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
 
        // Security: Only the sender or the recipient can view the message
        boolean esDestinatario = mensaje.getDestinatario().getId().equals(empresa.getId());
        boolean esRemitente = mensaje.getRemitente().getId().equals(empresa.getId());
 
        if (!esDestinatario && !esRemitente) {
             return "redirect:/mensajes?error=forbidden";
        }
 
        // Mark as read if the recipient is the logged-in user
        if (esDestinatario && !mensaje.isLeido()) {
            mensaje.setLeido(true);
            mensajeService.guardar(mensaje);
        }
 
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("esRecibido", esDestinatario);
        model.addAttribute("isDashboard", true);
 
        return "detalle_mensaje";
    }
 
    /**
     * Sends a message to the owner of an offer.
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
 
    /**
     * Shows the form to compose a new message.
     */
    @GetMapping("/mensajes/nuevo")
    public String nuevoMensaje(@RequestParam Long receptorId, 
                               @RequestParam(required = false) String asunto, 
                               Model model, Principal principal) {
        Empresa receptor = empresaService.buscarPorId(receptorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa receptora no encontrada"));
        
        model.addAttribute("receptor", receptor);
        model.addAttribute("asunto", asunto != null ? asunto : "");
        model.addAttribute("isDashboard", true);
        return "redactar_mensaje";
    }

    /**
     * Processes the submission of a new message.
     */
    @PostMapping("/mensajes/enviar")
    public String enviarMensaje(@RequestParam Long receptorId, 
                                @RequestParam String asunto, 
                                @RequestParam String contenido, 
                                Principal principal) {
        Empresa remitente = empresaService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Remitente no encontrado"));
        Empresa destinatario = empresaService.buscarPorId(receptorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatario no encontrado"));

        mensajeService.enviarMensaje(asunto, contenido, remitente, destinatario);
        return "redirect:/mensajes?exito=true";
    }

    /**
     * Deletes a message (Security: ownership check).
     */
    @PostMapping("/mensajes/{id}/eliminar")
    public String eliminarMensaje(@PathVariable Long id, Principal principal) {
        Mensaje mensaje = mensajeService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));

        // IDOR protection: only sender or recipient can delete
        // principal.getName() is the email (standard in this project)
        boolean esDestinatario = mensaje.getDestinatario().getEmailContacto().equals(principal.getName());
        boolean esRemitente = mensaje.getRemitente().getEmailContacto().equals(principal.getName());

        if (!esDestinatario && !esRemitente) {
             return "redirect:/mensajes?error=forbidden";
        }

        mensajeService.eliminar(id);
        return "redirect:/mensajes?exito=true";
    }
}
