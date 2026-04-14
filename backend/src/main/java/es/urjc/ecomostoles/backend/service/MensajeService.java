package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Mensaje;
import es.urjc.ecomostoles.backend.repository.MensajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Mensaje business logic.
 * Intermediary between controllers and the MensajeRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    /** Returns all messages in the system. */
    public List<Mensaje> obtenerTodos() {
        return mensajeRepository.findAll();
    }

    /** Returns all messages received by a specific company. */
    public List<Mensaje> obtenerPorDestinatario(Empresa destinatario) {
        return mensajeRepository.findByDestinatario(destinatario);
    }

    /** Returns the total count of messages. */
    public long contarTodos() {
        return mensajeRepository.count();
    }

    /** Persists a new or updated message. */
    public Mensaje guardar(Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }
}
