package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import es.urjc.ecomostoles.backend.mapper.AgreementMapper;
import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.service.AgreementService;
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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgreementRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgreementService agreementService;

    @MockBean
    private AgreementMapper agreementMapper;

    private static final String TEST_EMAIL = "test@company.com";

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Fase 3: Agreement Lifecycle - Aceptar acuerdo (200 OK)")
    void shouldAcceptAgreement() throws Exception {
        Agreement agreement = new Agreement();
        agreement.setId(1L);
        agreement.setStatus(AgreementStatus.PENDING);

        when(agreementService.findById(anyLong())).thenReturn(Optional.of(agreement));
        when(agreementService.updateAgreement(anyLong(), any(Agreement.class))).thenReturn(agreement);
        when(agreementMapper.toDto(any(Agreement.class))).thenReturn(null); // Return value not strictly checked for 200

        mockMvc.perform(put("/api/v1/agreements/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Fase 3: Agreement Lifecycle - Error de negocio (404 Not Found)")
    void shouldReturn404WhenAgreementNotFound() throws Exception {
        when(agreementService.findById(anyLong())).thenThrow(new NoSuchElementException("Acuerdo no encontrado"));

        mockMvc.perform(put("/api/v1/agreements/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());
    }
}
