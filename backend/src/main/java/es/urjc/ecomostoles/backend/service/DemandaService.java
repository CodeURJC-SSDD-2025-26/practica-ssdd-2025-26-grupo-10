package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final es.urjc.ecomostoles.backend.repository.AcuerdoRepository acuerdoRepository;

    public DemandaService(DemandaRepository demandaRepository,
                          es.urjc.ecomostoles.backend.repository.AcuerdoRepository acuerdoRepository) {
        this.demandaRepository = demandaRepository;
        this.acuerdoRepository = acuerdoRepository;
    }

    /** Returns all demands in the system. */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerTodas() {
        return demandaRepository.findTop50ByOrderByFechaPublicacionDesc();
    }

    /** Returns all demands in the system with pagination. */
    @Transactional(readOnly = true)
    public Page<Demanda> obtenerTodasPaginadas(Pageable pageable) {
        return demandaRepository.findAllPaginated(pageable);
    }

    /** Returns all demands filtered by state */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerPorEstado(es.urjc.ecomostoles.backend.model.EstadoDemanda estado) {
        return demandaRepository.findByEstadoJoinEmpresa(estado);
    }

    /** Returns all demands filtered by state with pagination. */
    @Transactional(readOnly = true)
    public Page<Demanda> obtenerPorEstadoPaginada(es.urjc.ecomostoles.backend.model.EstadoDemanda estado, Pageable pageable) {
        return demandaRepository.findByEstado(estado, pageable);
    }

    /** Returns top 3 smart offer recommendations for this company based on their demands */
    @Transactional(readOnly = true)
    public List<es.urjc.ecomostoles.backend.model.Oferta> obtenerSmartRecommendations(Empresa empresa) {
        return demandaRepository.findOfertasMatchingDemanda(
            empresa.getId(),
            org.springframework.data.domain.PageRequest.of(0, 3)
        );
    }

    /** Returns all demands belonging to a specific company. */
    @Transactional(readOnly = true)
    public List<Demanda> obtenerPorEmpresa(Empresa empresa) {
        return demandaRepository.findByEmpresa(empresa);
    }

    /** Returns all demands belonging to a specific company with pagination. */
    @Transactional(readOnly = true)
    public Page<Demanda> obtenerPorEmpresaPaginada(Empresa empresa, Pageable pageable) {
        return demandaRepository.findByEmpresa(empresa, pageable);
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
        if (acuerdoRepository.countByDemanda_Id(id) > 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "No se puede eliminar porque tiene acuerdos asociados");
        }
        demandaRepository.deleteById(id);
    }

    /** Returns the total count of demands. */
    @Transactional(readOnly = true)
    public long contarTodas() {
        return demandaRepository.count();
    }
}
