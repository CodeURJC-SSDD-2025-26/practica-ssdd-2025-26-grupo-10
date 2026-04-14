package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Demanda (Demand/Request) business logic.
 * Intermediary between controllers and the DemandaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
@Transactional
public class DemandaService {

    private final DemandaRepository demandaRepository;

    public DemandaService(DemandaRepository demandaRepository) {
        this.demandaRepository = demandaRepository;
    }

    /** Returns all demands in the system. */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerTodas() {
        return demandaRepository.findTop50ByOrderByFechaPublicacionDesc();
    }

    /** Returns all demands filtered by state */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerPorEstado(es.urjc.ecomostoles.backend.model.EstadoDemanda estado) {
        return demandaRepository.findByEstadoJoinEmpresa(estado);
    }

    /** Returns top 3 smart recommendations matching company sector directly from DB */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerSmartRecommendations(Empresa empresa) {
        if (empresa.getSectorIndustrial() == null) {
            return java.util.Collections.emptyList();
        }
        return demandaRepository.findSmartRecommendations(
            es.urjc.ecomostoles.backend.model.EstadoDemanda.ACTIVA,
            empresa.getId(),
            empresa.getSectorIndustrial(),
            org.springframework.data.domain.PageRequest.of(0, 3)
        );
    }

    /** Returns all demands belonging to a specific company. */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerPorEmpresa(Empresa empresa) {
        return demandaRepository.findByEmpresa(empresa);
    }

    /** Optimized count (does not fetch entire list) */
    @Transactional(readOnly = true)
    public long contarPorEmpresa(Empresa empresa) {
        return demandaRepository.countByEmpresa(empresa);
    }
    
    @Transactional(readOnly = true)
    public long contarActivasPorEmpresa(Empresa empresa) {
        return demandaRepository.countByEmpresaAndEstado(empresa, es.urjc.ecomostoles.backend.model.EstadoDemanda.ACTIVA);
    }

    /** Returns a demand by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Demanda> buscarPorId(Long id) {
        return demandaRepository.findById(id);
    }

    /** Persists a new or updated demand. */
    public Demanda guardar(Demanda demanda) {
        return demandaRepository.save(demanda);
    }

    /** Deletes a demand by its ID. */
    public void eliminar(Long id) {
        demandaRepository.deleteById(id);
    }

    /** Returns the total count of demands. */
    @Transactional(readOnly = true)
    public long contarTodas() {
        return demandaRepository.count();
    }
}
