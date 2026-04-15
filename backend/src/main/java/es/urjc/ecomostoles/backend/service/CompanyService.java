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
 * Service layer for Company business logic.
 * Intermediary between controllers and the CompanyRepository, following
 * the Controller > Service > Repository architecture pattern.
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

    /** Filters companies by name, email, or CIF (Return top 50 as fallback). */
    @Transactional(readOnly = true)
    public List<Company> filterCompanies(String search) {
        return companyRepository
                .findByCommercialNameContainingIgnoreCaseOrContactEmailContainingIgnoreCaseOrTaxIdContainingIgnoreCase(
                        search, search, search, PageRequest.of(0, 50))
                .getContent();
    }

    /**
     * Handles the professional registration of a new company, including
     * password encryption and data persistence.
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
