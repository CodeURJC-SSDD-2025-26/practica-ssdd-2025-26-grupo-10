package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.repository.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;

/**
 * Identity Management Service governing the lifecycle of corporate Tenants.
 * 
 * Orchestrates secure registration workflows, role-based boundary filtering, and 
 * credential management. Implements the specialized logic for administrative 
 * identity reconciliation and public client discovery.
 */
@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanyService(CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns all companies in the system. */
    @Transactional(readOnly = true)
    public List<Company> getAll() {
        return companyRepository.findTop50ByOrderByIdDesc();
    }

    /** Returns a page of companies for administrative management. */
    @Transactional(readOnly = true)
    public Page<Company> getCompaniesPaginated(int page, int size) {
        return companyRepository
                .findAll(PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending()));
    }

    /** Returns a page of companies EXCLUDING administrators. */
    @Transactional(readOnly = true)
    public Page<Company> getClientsPaginated(org.springframework.data.domain.Pageable pageable) {
        return companyRepository.findAllClients(pageable);
    }

    /** Finds a company by its contact email (used as username). */
    @Transactional(readOnly = true)
    public Optional<Company> findByEmail(String email) {
        return companyRepository.findByContactEmail(email);
    }

    /** Finds a company by its commercial name. */
    @Transactional(readOnly = true)
    public Optional<Company> findByCommercialName(String name) {
        return companyRepository.findByCommercialName(name);
    }

    /** Returns a company by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    /** Persists a new or updated company. */
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    /** Deletes a company by its ID. */
    public void delete(Long id) {
        companyRepository.deleteById(id);
    }

    /** Returns the total count of companies. */
    @Transactional(readOnly = true)
    public long countAll() {
        return companyRepository.count();
    }

    /** Filters companies by name, email, or taxId with pagination. */
    @Transactional(readOnly = true)
    public Page<Company> filterCompaniesPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        return companyRepository
                .findByCommercialNameContainingIgnoreCaseOrContactEmailContainingIgnoreCaseOrTaxIdContainingIgnoreCase(
                        search, search, search, pageable);
    }

    /** Filters ONLY client companies with pagination. */
    @Transactional(readOnly = true)
    public Page<Company> searchClientsPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        return companyRepository.searchClients(search, pageable);
    }

    /** Filters companies by name, email, or CIF (Return top 50 as fallback). */
    @Transactional(readOnly = true)
    public List<Company> filterCompanies(String search) {
        return companyRepository
                .findByCommercialNameContainingIgnoreCaseOrContactEmailContainingIgnoreCaseOrTaxIdContainingIgnoreCase(
                        search, search, search, PageRequest.of(0, 50))
                .getContent();
    }

    /**
     * Executes the secure registration of a new corporate tenant.
     * 
     * Enforces unique identity constraints (Email) and applies unidirectional salt-based 
     * password hashing via the configured PasswordEncoder. Dynamically attaches 
     * binary logo assets to the persistent entity profile.
     * 
     * @param company Initial company template.
     * @param rawPassword Plaintext credential to be encrypted.
     * @param logoBytes Binary payload for the corporate logo.
     * @return The persisted Company entity with secured credentials.
     */
    public Company registerNewCompany(Company company, String rawPassword, byte[] logoBytes) {
        if (companyRepository.findByContactEmail(company.getContactEmail()).isPresent()) {
            throw new IllegalArgumentException("Este email ya está registrado");
        }

        company.setPassword(passwordEncoder.encode(rawPassword));

        if (logoBytes != null && logoBytes.length > 0) {
            company.setLogo(logoBytes);
        }

        return companyRepository.save(company);
    }
}
