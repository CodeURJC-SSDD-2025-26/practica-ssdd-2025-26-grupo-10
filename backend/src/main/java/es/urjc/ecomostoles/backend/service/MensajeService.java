package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Mensaje;
import es.urjc.ecomostoles.backend.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Service layer for Mensaje business logic.
 * Intermediary between controllers and the MensajeRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    /** Returns all messages in the system. */
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerTodos() {
        return mensajeRepository.findTop100ByOrderByFechaEnvioDesc();
    }

    /** Returns all messages received by a specific company. */
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerPorDestinatario(Empresa destinatario) {
        return mensajeRepository.findByDestinatario(destinatario);
    }

    /** Returns the total count of messages. */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return mensajeRepository.count();
    }

    /** Returns a message by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Mensaje> buscarPorId(Long id) {
        return mensajeRepository.findById(id);
    }

    /** Persists a new or updated message. */
    public Mensaje guardar(Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }

    /**
     * Logic for sending a new message, including automatic timestamping.
     */
    public void enviarMensaje(String asunto, String contenido, Empresa remitente, Empresa destinatario) {
        Mensaje nuevo = new Mensaje();
        nuevo.setAsunto(asunto);
        nuevo.setContenido(contenido);
        nuevo.setRemitente(remitente);
        nuevo.setDestinatario(destinatario);
        nuevo.setFechaEnvio(java.time.LocalDateTime.now());
        nuevo.setLeido(false);
        mensajeRepository.save(nuevo);
    }
}
