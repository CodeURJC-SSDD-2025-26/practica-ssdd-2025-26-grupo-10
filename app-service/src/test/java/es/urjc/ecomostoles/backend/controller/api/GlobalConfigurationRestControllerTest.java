package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.service.ConfigurationService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalConfigurationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationService configurationService;

    @Test
    @WithMockUser(username = "test@company.com", roles = {"COMPANY"})
    @DisplayName("Phase 4: Global Config - Get system configuration (200 OK)")
    void shouldGetGlobalConfiguration() throws Exception {
        // Wildcard stub as fallback: prevents NPE in Map.of() if any key returns null
        when(configurationService.getAutoValue(anyString())).thenReturn("default-value");

        // Specific stubs — override the wildcard for the keys we care about
        when(configurationService.getAutoValue("platformName")).thenReturn("EcoMóstoles Test");
        when(configurationService.getAutoValue("contactEmail")).thenReturn("test@ecomostoles.com");
        when(configurationService.getAutoValue("platformCity")).thenReturn("Móstoles");

        mockMvc.perform(get("/api/v1/configurations"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformName").value("EcoMóstoles Test"));
    }
}
