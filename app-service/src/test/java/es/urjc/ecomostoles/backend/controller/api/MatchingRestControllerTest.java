package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.MatchResultDTO;
import es.urjc.ecomostoles.backend.service.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    private static final String TEST_EMAIL = "test@company.com";

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Fase 3: Smart Matching - Obtener sugerencias para una demanda (200 OK)")
    void shouldGetBestMatchesForDemand() throws Exception {
        // Mock a simple result list
        MatchResultDTO mockResult = new MatchResultDTO(null, 95.0, "Alta compatibilidad");
        when(matchingService.findBestMatchesForDemand(anyLong())).thenReturn(Collections.singletonList(mockResult));

        mockMvc.perform(get("/api/v1/matches/demands/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].matchScore").value(95.0));
    }
}
