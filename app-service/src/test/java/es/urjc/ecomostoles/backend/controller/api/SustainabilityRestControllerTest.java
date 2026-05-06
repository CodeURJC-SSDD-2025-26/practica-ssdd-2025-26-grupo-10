package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;
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

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SustainabilityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SustainabilityEngine sustainabilityEngine;

    @Test
    @WithMockUser(username = "test@company.com", roles = {"COMPANY"})
    @DisplayName("Phase 4: Sustainability Engine - Calculate CO2 impact (200 OK)")
    void shouldCalculateCo2Impact() throws Exception {
        when(sustainabilityEngine.calculateCo2Impact(anyDouble(), anyString())).thenReturn(150.5);

        mockMvc.perform(get("/api/v1/sustainability/impacts")
                        .param("quantity", "500")
                        .param("category", "METAL_WASTE"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.co2SavedKg").value(150.5));
    }
}
