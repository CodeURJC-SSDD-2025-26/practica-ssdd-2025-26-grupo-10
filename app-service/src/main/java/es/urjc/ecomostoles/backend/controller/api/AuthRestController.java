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

/**
 * Public REST API controller for Identity Management.
 *
 * <p>Base path: {@code /api/v1/auth}</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT token issuance")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new company", description = "Creates a new company record and returns a valid JWT token for immediate access.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Authenticate and get token", description = "Validates credentials against the database and returns a JWT token for accessing secured API endpoints.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
