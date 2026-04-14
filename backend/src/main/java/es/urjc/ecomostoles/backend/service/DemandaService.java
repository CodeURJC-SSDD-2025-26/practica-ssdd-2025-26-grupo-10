package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Demanda (Demand/Request) business logic.
 * Intermediary between controllers and the DemandaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
public class DemandaService {

    private final DemandaRepository demandaRepository;

    public DemandaService(DemandaRepository demandaRepository) {
        this.demandaRepository = demandaRepository;
    }

    /** Returns all demands in the system. */
    public List<Demanda> obtenerTodas() {
        return demandaRepository.findAll();
    }

    /** Returns all demands belonging to a specific company. */
    public List<Demanda> obtenerPorEmpresa(Empresa empresa) {
        return demandaRepository.findByEmpresa(empresa);
    }

    /** Returns a demand by its ID, or empty if not found. */
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
    public long contarTodas() {
        return demandaRepository.count();
    }
}
