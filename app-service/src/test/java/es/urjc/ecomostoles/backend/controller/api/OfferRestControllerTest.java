package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.OfferDTO;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.OfferStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OfferRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private CompanyRepository companyRepository;

        @Autowired
        private OfferRepository offerRepository;

        @Autowired
        private DemandRepository demandRepository;

        @Autowired
        private AgreementRepository agreementRepository;

        private static final String TEST_EMAIL = "offer_owner@test.com";

        @BeforeEach
        void setUp() {
                agreementRepository.deleteAll();
                offerRepository.deleteAll();
                demandRepository.deleteAll();
                companyRepository.deleteAll();

                // Guaranteed unique and valid TaxID
                String validTaxId = "A" + String.format("%08d", new Random().nextInt(100000000));

                Company owner = new Company();
                owner.setCommercialName("Offer Owner Corp");
                owner.setTaxId(validTaxId);
                owner.setContactEmail(TEST_EMAIL);
                owner.setPassword("password");
                owner.setAddress("Calle Test 123");
                owner.setPhone("123");
                owner.setIndustrialSector("Industry");
                owner.setDescription("Desc");
                owner.setRoles(List.of("COMPANY"));
                companyRepository.save(owner);
        }

        @Test
        @WithMockUser(username = TEST_EMAIL, roles = { "COMPANY" })
        @DisplayName("Phase 2: Offer CRUD - Create valid offer (201)")
        void shouldCreateOfferSuccessfully() throws Exception {
                OfferDTO request = new OfferDTO(
                                null, "Lote de Palets", "Palets de madera en buen estado",
                                "WOOD_WASTE", 50.0, "uds", 100.0, "Inmediata",
                                OfferStatus.ACTIVE, null, 0, null, false);

                mockMvc.perform(post("/api/v1/offers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = TEST_EMAIL)
        @DisplayName("Phase 2: Offer CRUD - Paginated listing (200)")
        void shouldListOffersPaginated() throws Exception {
                mockMvc.perform(get("/api/v1/offers?page=0&size=5"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = TEST_EMAIL, roles = { "COMPANY" })
        @DisplayName("Phase 2: Offer Validation - Empty title (400)")
        void shouldReturn400WhenTitleIsEmpty() throws Exception {
                OfferDTO invalidRequest = new OfferDTO(
                                null, "", "Palets",
                                "WOOD_WASTE", 10.0, "kg", 5.0, "Hoy",
                                OfferStatus.ACTIVE, null, 0, null, false);

                mockMvc.perform(post("/api/v1/offers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = TEST_EMAIL, roles = { "COMPANY" })
        @DisplayName("Phase 2: Offer Validation - Negative price (400)")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
                OfferDTO invalidRequest = new OfferDTO(
                                null, "Oferta", "Desc",
                                "METAL_WASTE", 10.0, "kg", -1.0, "Mañana",
                                OfferStatus.ACTIVE, null, 0, null, false);

                mockMvc.perform(post("/api/v1/offers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }
}
