package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.repository.CompanyRepository;
import es.urjc.ecomostoles.backend.security.dto.AuthRequest;
import es.urjc.ecomostoles.backend.security.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Ensures database is rolled back after each test to keep it clean
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Prepare a clean state and a base user for login tests
        companyRepository.deleteAll();
        
        Company testUser = new Company();
        testUser.setCommercialName("Test Company");
        testUser.setTaxId("A12345678");
        testUser.setContactEmail("login@test.com");
        testUser.setPassword(passwordEncoder.encode("secretPass"));
        testUser.setAddress("Calle Test 123");
        testUser.setPhone("912345678");
        testUser.setIndustrialSector("IT");
        testUser.setDescription("Empresa creada para pruebas de integración");
        testUser.setRoles(List.of("COMPANY"));
        testUser.setVerified(true);
        
        companyRepository.save(testUser);
    }

    // ── HAPPY PATH: REGISTRATION ──────────────────────────────────────────

    @Test
    @DisplayName("Phase 1: Happy Path - Register new company (201 Created)")
    void shouldRegisterNewCompanySuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "QA Test Corp",
                "B12345678", // Different CIF than setUp to avoid unique constraint
                "qa@testcorp.com",
                "password123",
                "Calle del QA 1",
                "600000000",
                "Software",
                "Empresa para pruebas de automatización"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Verify 201 Created
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ── HAPPY PATH: LOGIN ─────────────────────────────────────────────────

    @Test
    @DisplayName("Phase 1: Happy Path - Login with valid credentials (200 OK)")
    void shouldLoginSuccessfully() throws Exception {
        // Authenticate with the credentials set up in @BeforeEach
        AuthRequest loginReq = new AuthRequest("login@test.com", "secretPass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk()) // Verify 200 OK
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ── ERROR HANDLING: BAD CREDENTIALS ───────────────────────────────────

    @Test
    @DisplayName("Phase 1: Security - Login with incorrect password (401 Unauthorized)")
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        // Try to login with the correct email but WRONG password
        AuthRequest loginReq = new AuthRequest("login@test.com", "wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized()) // Verify 401 Unauthorized
                .andExpect(jsonPath("$.message", containsString("Credenciales incorrectas")))
                .andExpect(jsonPath("$.statusCode", is(401)));
    }

    // ── ADVICE VALIDATION: BAD REQUEST ───────────────────────────────────

    @Test
    @DisplayName("Phase 1: Advice - Register with invalid DTO (400 Bad Request)")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Invalid request: Empty name and invalid CIF format
        RegisterRequest invalidRequest = new RegisterRequest(
                "", // Blank name
                "invalid-cif", // Bad CIF
                "invalid-email-format", // Invalid email
                "123", // Too short password
                "", // Blank address
                null, null, null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // Verify 400 Bad Request
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
