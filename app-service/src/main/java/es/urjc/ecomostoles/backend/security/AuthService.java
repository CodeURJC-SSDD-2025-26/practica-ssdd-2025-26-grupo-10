package es.urjc.ecomostoles.backend.security;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.repository.CompanyRepository;
import es.urjc.ecomostoles.backend.security.dto.AuthRequest;
import es.urjc.ecomostoles.backend.security.dto.AuthResponse;
import es.urjc.ecomostoles.backend.security.dto.RegisterRequest;
import es.urjc.ecomostoles.backend.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrator for Identity Provisioning and Authentication.
 * 
 * Handles the registration of new tenants, encrypts their credentials securely using BCrypt, 
 * and authenticates existing tenants using the centralized AuthenticationManager before 
 * issuing JWT access tokens.
 */
@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(CompanyRepository companyRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Provisions a new Company tenant and generates an immediate access token.
     */
    public AuthResponse register(RegisterRequest request) {
        if (companyRepository.findByContactEmail(request.contactEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        Company company = new Company();
        company.setCommercialName(request.commercialName());
        company.setTaxId(request.taxId());
        company.setContactEmail(request.contactEmail());
        // Enforce cryptographic hashing before DB persistence
        company.setPassword(passwordEncoder.encode(request.password()));
        company.setAddress(request.address());
        company.setPhone(request.phone());
        company.setIndustrialSector(request.industrialSector());
        company.setDescription(request.description());
        // Default role for new business registrants
        company.setRoles(List.of("COMPANY"));

        companyRepository.save(company);

        // Build UserDetails to generate the token
        UserDetails userDetails = User.builder()
                .username(company.getContactEmail())
                .password(company.getPassword())
                .roles("COMPANY")
                .build();

        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }

    /**
     * Authenticates a user against the DB and issues a JWT token if credentials are valid.
     */
    public AuthResponse authenticate(AuthRequest request) {
        // 1. Delegate validation to Spring's AuthenticationManager (checks BCrypt hash)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Fetch the authenticated entity
        Company company = companyRepository.findByContactEmail(request.email())
                .orElseThrow();

        // 3. Extract roles
        List<String> roles = company.getRoles();
        String[] rolesArray = (roles != null && !roles.isEmpty())
                ? roles.toArray(new String[0])
                : new String[] { "COMPANY" };

        // 4. Hydrate UserDetails representation
        UserDetails userDetails = User.builder()
                .username(company.getContactEmail())
                .password(company.getPassword())
                .roles(rolesArray)
                .build();

        // 5. Issue cryptographic token
        String jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }
}
