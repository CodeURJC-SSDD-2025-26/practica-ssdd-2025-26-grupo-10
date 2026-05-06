package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.DemandDTO;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.DemandStatus;
import es.urjc.ecomostoles.backend.repository.AgreementRepository;
import es.urjc.ecomostoles.backend.repository.CompanyRepository;
import es.urjc.ecomostoles.backend.repository.DemandRepository;
import es.urjc.ecomostoles.backend.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemandRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    private static final String TEST_EMAIL = "demand_owner@test.com";

    @BeforeEach
    void setUp() {
        agreementRepository.deleteAll();
        offerRepository.deleteAll();
        demandRepository.deleteAll();
        companyRepository.deleteAll();

        String validTaxId = "B" + String.format("%08d", new Random().nextInt(100000000));

        Company owner = new Company();
        owner.setCommercialName("Demand Owner Corp");
        owner.setTaxId(validTaxId);
        owner.setContactEmail(TEST_EMAIL);
        owner.setPassword("password");
        owner.setAddress("Calle Demand 1");
        owner.setPhone("123");
        owner.setIndustrialSector("Industry");
        owner.setDescription("Desc");
        owner.setRoles(List.of("COMPANY"));
        companyRepository.save(owner);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Fase 2: Demand CRUD - Crear demanda válida (201)")
    void shouldCreateDemandSuccessfully() throws Exception {
        DemandDTO request = new DemandDTO(
                null, "Busco Chatarra", "METAL_WASTE", "Chatarra",
                200.0, "kg", "Alta", 500.0, "Móstoles", "30",
                DemandStatus.ACTIVE, null, null, null, 0, null
        );

        mockMvc.perform(post("/api/v1/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("Fase 2: Demand CRUD - Listado paginado (200)")
    void shouldListDemandsPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/demands?page=0&size=5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Fase 2: Demand Validation - Presupuesto negativo (400)")
    void shouldReturn400WhenBudgetIsNegative() throws Exception {
        DemandDTO invalidRequest = new DemandDTO(
                null, "Error Budget", "PLASTIC_WASTE", "Error",
                1.0, "kg", "Baja", -10.0, "Madrid", "15",
                DemandStatus.ACTIVE, null, null, null, 0, null
        );

        mockMvc.perform(post("/api/v1/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
