package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Oferta (Offer) business logic.
 * Intermediary between controllers and the OfertaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
@Transactional
public class OfertaService {

    private final OfertaRepository ofertaRepository;

    public OfertaService(OfertaRepository ofertaRepository) {
        this.ofertaRepository = ofertaRepository;
    }

    /** Returns all offers in the system as projections. */
    @Transactional(readOnly = true)
    public List<OfertaResumen> obtenerTodas() {
        return ofertaRepository.findTop50ByOrderByFechaPublicacionDesc(OfertaResumen.class);
    }

    /** Returns all offers filtered by state (Projected) */
    @Transactional(readOnly = true)
    public List<OfertaResumen> obtenerPorEstado(es.urjc.ecomostoles.backend.model.EstadoOferta estado) {
        return ofertaRepository.findByEstadoJoinEmpresa(estado, OfertaResumen.class);
    }

    /** Returns top 3 recent active offers directly from DB (Projected) */
    @Transactional(readOnly = true)
    public List<OfertaResumen> obtenerRecientesActivas() {
        return ofertaRepository.findTop3ByEstadoOrderByFechaPublicacionDesc(es.urjc.ecomostoles.backend.model.EstadoOferta.ACTIVA, OfertaResumen.class);
    }

    /** Returns all offers belonging to a specific company (Projected). */
    @Transactional(readOnly = true)
    public List<OfertaResumen> obtenerPorEmpresa(Empresa empresa) {
        return ofertaRepository.findByEmpresa(empresa, OfertaResumen.class);
    }

    /** Optimized count (does not fetch entire list) */
    @Transactional(readOnly = true)
    public long contarPorEmpresa(Empresa empresa) {
        return ofertaRepository.countByEmpresa(empresa);
    }

    /** Returns an offer by its ID, or empty if not found. */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public long contarTodas() {
        return ofertaRepository.count();
    }
}
