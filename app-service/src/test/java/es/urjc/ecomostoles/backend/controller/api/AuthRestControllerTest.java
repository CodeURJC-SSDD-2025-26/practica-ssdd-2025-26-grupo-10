package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.security.dto.AuthRequest;
import es.urjc.ecomostoles.backend.security.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures database is rolled back after each test
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test 1: Registration yields a valid JWT token")
    void shouldRegisterNewCompanyAndReturnToken() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "TechCorp SL",
                "W1234567A",
                "contacto@techcorp.es",
                "securePass123",
                "Calle Innovación 5, Madrid",
                "600123456",
                "Tecnología",
                "Empresa de reciclaje tecnológico"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Test 2: Login yields a valid JWT token for existing company")
    void shouldAuthenticateExistingCompanyAndReturnToken() throws Exception {
        // 1. Setup: Register a user first
        RegisterRequest registerReq = new RegisterRequest(
                "EcoMundo SL",
                "W2234567B",
                "auth_test@ecomundo.es",
                "password123",
                "Polígono Industrial, Madrid",
                "910000000",
                "Reciclaje",
                "Empresa verde"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        // 2. Execute: Try to login with the registered credentials
        AuthRequest loginReq = new AuthRequest(
                "auth_test@ecomundo.es",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Test 3: Login fails with incorrect password")
    void shouldFailLoginWithWrongPassword() throws Exception {
        // 1. Setup: Register a user
        RegisterRequest registerReq = new RegisterRequest(
                "MetalWorks SA",
                "W3334567C",
                "fail_test@metalworks.es",
                "correctPassword",
                "Calle Hierro 12",
                "600600600",
                "Metalurgia",
                "Procesamiento de metales"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        // 2. Execute: Try to login with incorrect credentials
        AuthRequest loginReq = new AuthRequest(
                "fail_test@metalworks.es",
                "wrongPassword"
        );

        // Spring Security throws BadCredentialsException which is translated to 401
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
