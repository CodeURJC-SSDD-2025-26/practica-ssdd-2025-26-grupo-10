package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Mensaje;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.MensajeRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle reading and listing messages.
 */
@Controller
public class MensajeController {

    private final EmpresaRepository empresaRepository;
    private final MensajeRepository mensajeRepository;
    private final OfertaRepository ofertaRepository;

    public MensajeController(EmpresaRepository empresaRepository, MensajeRepository mensajeRepository, OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.mensajeRepository = mensajeRepository;
        this.ofertaRepository = ofertaRepository;
    }

    /**
     * Shows the inbox messages for the active company.
     *
     * @param model Spring UI model
     * @return view template "mensajes"
     */
    @GetMapping("/mensajes")
    public String mostrarMensajes(Model model) {
        // Obteniendo mock de empresa activa simulada
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);

            // Recupera lista de mensajes donde la empresa actual es la destinataria
            List<Mensaje> misMensajes = mensajeRepository.findByDestinatario(empresa);
            model.addAttribute("mensajes", misMensajes);

            return "mensajes";
        }

        return "redirect:/";
    }

    /**
     * Envia un mensaje al propietario de una oferta.
     */
    @PostMapping("/mensajes/enviar/{ofertaId}")
    public String enviarMensajeOferta(@PathVariable Long ofertaId, @RequestParam String contenido) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(ofertaId);
        Optional<Empresa> remitenteOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (ofertaOpt.isPresent() && remitenteOpt.isPresent()) {
            Oferta oferta = ofertaOpt.get();
            Empresa remitente = remitenteOpt.get();
            Empresa destinatario = oferta.getEmpresa();

            Mensaje nuevoMensaje = new Mensaje();
            nuevoMensaje.setAsunto("Re: " + oferta.getTitulo());
            nuevoMensaje.setContenido(contenido);
            nuevoMensaje.setRemitente(remitente);
            nuevoMensaje.setDestinatario(destinatario);
            nuevoMensaje.setFechaEnvio(LocalDateTime.now());
            nuevoMensaje.setLeido(false);

            mensajeRepository.save(nuevoMensaje);
        }

        return "redirect:/mensajes";
    }
}
