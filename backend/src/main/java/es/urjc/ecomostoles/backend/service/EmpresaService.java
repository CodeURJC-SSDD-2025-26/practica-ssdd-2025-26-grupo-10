package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Empresa (Company) business logic.
 * Intermediary between controllers and the EmpresaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    /** Returns all companies in the system. */
    public List<Empresa> obtenerTodas() {
        return empresaRepository.findAll();
    }

    /** Finds a company by its contact email (used as username). */
    public Optional<Empresa> buscarPorEmail(String email) {
        return empresaRepository.findByEmailContacto(email);
    }

    /** Finds a company by its commercial name. */
    public Optional<Empresa> buscarPorNombreComercial(String nombre) {
        return empresaRepository.findByNombreComercial(nombre);
    }

    /** Returns a company by its ID, or empty if not found. */
    public Optional<Empresa> buscarPorId(Long id) {
        return empresaRepository.findById(id);
    }

    /** Persists a new or updated company. */
    public Empresa guardar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    /** Deletes a company by its ID. */
    public void eliminar(Long id) {
        empresaRepository.deleteById(id);
    }

    /** Returns the total count of companies. */
    public long contarTodas() {
        return empresaRepository.count();
    }
}
