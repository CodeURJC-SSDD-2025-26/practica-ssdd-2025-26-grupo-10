package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Oferta (Offer) business logic.
 * Intermediary between controllers and the OfertaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;

    public OfertaService(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    /** Returns all offers in the system. */
    public List<Oferta> obtenerTodas() {
        return ofertaRepository.findAll();
    }

    /** Returns all offers filtered by state */
    public List<Oferta> obtenerPorEstado(es.urjc.ecomostoles.backend.model.EstadoOferta estado) {
        return ofertaRepository.findByEstado(estado);
    }

    /** Returns top 3 recent active offers directly from DB */
    public List<Oferta> obtenerRecientesActivas() {
        return ofertaRepository.findTop3ByEstadoOrderByFechaPublicacionDesc(es.urjc.ecomostoles.backend.model.EstadoOferta.ACTIVA);
    }

    /** Returns all offers belonging to a specific company. */
    public List<Oferta> obtenerPorEmpresa(Empresa empresa) {
        return ofertaRepository.findByEmpresa(empresa);
    }

    /** Returns an offer by its ID, or empty if not found. */
    public Optional<Oferta> buscarPorId(Long id) {
        return ofertaRepository.findById(id);
    }

    /** Persists a new or updated offer. */
    public Oferta guardar(Oferta oferta) {
        return ofertaRepository.save(oferta);
    }

    /** Deletes an offer by its ID. */
    public void eliminar(Long id) {
        ofertaRepository.deleteById(id);
    }

    /** Returns the total count of offers. */
    public long contarTodas() {
        return ofertaRepository.count();
    }
}
