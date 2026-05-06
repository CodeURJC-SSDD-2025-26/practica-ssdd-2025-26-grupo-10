package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.security.AuthService;
import es.urjc.ecomostoles.backend.security.dto.AuthRequest;
import es.urjc.ecomostoles.backend.security.dto.AuthResponse;
import es.urjc.ecomostoles.backend.security.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST API controller for Security and Identity resources.
 * 
 * <p>
 * Follows a verb-free architectural design by treating authentication
 * and registration as the creation of 'Token' and 'Registration' resources.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Security", description = "Endpoints for identity management and JWT issuance")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Create a new registration", description = "Registers a new company and returns a valid JWT token.")
    @PostMapping("/registrations")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/companies/{id}")
                .buildAndExpand(response.companyId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Create an access token", description = "Authenticates credentials and issues a new JWT token resource.")
    @PostMapping("/tokens")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(summary = "Delete an access token", description = "Invalidates the current session/token (stateless logout).")
    @org.springframework.web.bind.annotation.DeleteMapping("/tokens")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
