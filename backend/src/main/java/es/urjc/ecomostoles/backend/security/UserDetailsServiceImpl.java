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
 * UserDetailsService implementation that loads a user from the Company entity.
 *
 * Roles are read directly from Company.roles (@ElementCollection field).
 * If the list is empty or null, COMPANY is assigned by default.
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
        log.info("🔍 Attempting to log in user: {}", email);

        Company company = companyRepository.findByContactEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found in DB: {}", email);
                    return new UsernameNotFoundException("Empresa no encontrada: " + email);
                });

        // Read roles from the entity; if none, assign COMPANY by default
        List<String> roles = company.getRoles();
        String[] rolesArray = (roles != null && !roles.isEmpty())
                ? roles.toArray(new String[0])
                : new String[] { "COMPANY" };

        log.info("✅ Session started: {} | Roles: {}", company.getCommercialName(), List.of(rolesArray));

        return User.builder()
                .username(company.getContactEmail())
                .password(company.getPassword())
                .roles(rolesArray) // Spring automatically adds the ROLE_ prefix
                .build();
    }
}
