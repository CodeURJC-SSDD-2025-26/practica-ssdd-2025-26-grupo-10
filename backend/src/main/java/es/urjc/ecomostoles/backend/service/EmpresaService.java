package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Empresa (Company) business logic.
 * Intermediary between controllers and the EmpresaRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder   passwordEncoder;

    public EmpresaService(EmpresaRepository empresaRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns all companies in the system. */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerTodas() {
        return empresaRepository.findTop50ByOrderByIdDesc();
    }

    /** Finds a company by its contact email (used as username). */
    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorEmail(String email) {
        return empresaRepository.findByEmailContacto(email);
    }

    /** Finds a company by its commercial name. */
    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorNombreComercial(String nombre) {
        return empresaRepository.findByNombreComercial(nombre);
    }

    /** Returns a company by its ID, or empty if not found. */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public long contarTodas() {
        return empresaRepository.count();
    }

    /** Filters companies by name, email, or CIF. */
    @Transactional(readOnly = true)
    public List<Empresa> filtrarEmpresas(String search) {
        return empresaRepository.findByNombreComercialContainingIgnoreCaseOrEmailContactoContainingIgnoreCaseOrCifContainingIgnoreCase(search, search, search);
    }

    /**
     * Handles the professional registration of a new company, including
     * password encryption and data persistence.
     */
    public Empresa registrarNuevaEmpresa(Empresa empresa, String rawPassword, byte[] logoBytes) {
        if (empresaRepository.findByEmailContacto(empresa.getEmailContacto()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        empresa.setPassword(passwordEncoder.encode(rawPassword));
        
        if (logoBytes != null && logoBytes.length > 0) {
            empresa.setLogo(logoBytes);
        }
        
        return empresaRepository.save(empresa);
    }
}
