package es.urjc.ecomostoles.backend.security;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.repository.CompanyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom authentication provider adapter bridging the DB schema with Spring Security.
 * 
 * Implements the Principal retrieval logic by mapping Company identity records to 
 * UserDetails contracts. Dynamically evaluates role hierarchies to ensure the 
 * security context is populated with correct authority tokens during the login handshake.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final CompanyRepository companyRepository;

    public UserDetailsServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("[Security] Authentication attempt -> Principal: {}", email);

        Company company = companyRepository.findByContactEmail(email)
                .orElseThrow(() -> {
                    log.warn("[Security] Authentication failed -> Principal not found: {}", email);
                    return new UsernameNotFoundException("Company not found: " + email);
                });

        // Read roles from the entity; if none, assign COMPANY by default
        List<String> roles = company.getRoles();
        String[] rolesArray = (roles != null && !roles.isEmpty())
                ? roles.toArray(new String[0])
                : new String[] { "COMPANY" };

        log.info("[Security] Authentication success -> Principal: {} | Roles: {}", company.getContactEmail(), List.of(rolesArray));

        return User.builder()
                .username(company.getContactEmail())
                .password(company.getPassword())
                .roles(rolesArray) // Spring automatically adds the ROLE_ prefix
                .build();
    }
}
