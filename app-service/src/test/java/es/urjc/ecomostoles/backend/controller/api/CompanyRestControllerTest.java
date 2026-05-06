package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.mapper.CompanyMapper;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CompanyRestController.
 *
 * Uses @MockBean for CompanyService to avoid DataIntegrityViolationException
 * caused by the unique constraint on taxId when the controller calls
 * existing.setTaxId(...) during an update — which triggers a DB-level conflict
 * even when deleteAll() has been called within the same @Transactional test boundary.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    // CompanyMapper is real — it's a simple MapStruct bean with no side effects
    @Autowired
    private CompanyMapper companyMapper;

    private static final String TEST_EMAIL = "test@company.com";
    private static final Long   TEST_ID    = 1L;
    private static final String TEST_TAXID = "A12345678";

    /** Helper: builds a fully populated in-memory Company matching our test principal. */
    private Company buildTestCompany() {
        Company c = new Company();
        c.setId(TEST_ID);
        c.setCommercialName("Original Corp");
        c.setTaxId(TEST_TAXID);
        c.setContactEmail(TEST_EMAIL);
        c.setPassword("secure123");
        c.setAddress("Calle Falsa 123");
        c.setPhone("912345678");
        c.setIndustrialSector("Tech");
        c.setDescription("Descripcion inicial obligatoria");
        c.setRoles(List.of("COMPANY"));
        return c;
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Phase 2: Company - Get profile by ID (200)")
    void shouldGetCompanyById() throws Exception {
        Company mockCompany = buildTestCompany();
        when(companyService.findById(anyLong())).thenReturn(Optional.of(mockCompany));

        mockMvc.perform(get("/api/v1/companies/" + TEST_ID))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commercialName", is("Original Corp")));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Phase 2: Company - Update profile with complete DTO (200)")
    void shouldUpdateCompanyProfile() throws Exception {
        // Stub: find returns the existing entity, save returns the updated one
        Company existing = buildTestCompany();
        Company updated  = buildTestCompany();
        updated.setCommercialName("Updated Corp");
        updated.setAddress("Calle Nueva 456");

        when(companyService.findById(TEST_ID)).thenReturn(Optional.of(existing));
        when(companyService.save(any(Company.class))).thenReturn(updated);

        // DTO with all mandatory fields — mirrors exactly what the controller accepts
        CompanyDTO updateReq = new CompanyDTO();
        updateReq.setId(TEST_ID);
        updateReq.setCommercialName("Updated Corp");
        updateReq.setContactEmail(TEST_EMAIL);
        updateReq.setTaxId(TEST_TAXID);
        updateReq.setAddress("Calle Nueva 456");
        updateReq.setPhone("918887766");
        updateReq.setIndustrialSector("Renewables");
        updateReq.setDescription("Nueva descripcion para pasar la validacion del DTO");

        mockMvc.perform(put("/api/v1/companies/" + TEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commercialName", is("Updated Corp")));
    }
}
