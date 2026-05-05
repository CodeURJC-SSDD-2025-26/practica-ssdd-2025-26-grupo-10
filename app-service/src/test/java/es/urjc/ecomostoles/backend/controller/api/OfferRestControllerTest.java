package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.OfferDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfferRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test 1: Read Secure - Paginated offers")
    @WithMockUser(username = "contacto@metalesdelsur.es", roles = {"COMPANY"})
    void shouldReturnPaginatedOffers() throws Exception {
        mockMvc.perform(get("/api/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("Test 2: Write Secure - Create offer returns 201 and Location")
    @WithMockUser(username = "contacto@metalesdelsur.es", roles = {"COMPANY"})
    void shouldCreateOfferAndReturn201WithLocation() throws Exception {
        OfferDTO newOffer = new OfferDTO(
                null,
                "Palets de Madera 1",
                "Palets en buen estado",
                "WOOD",
                50.0,
                "unidades",
                150.0,
                "Inmediata",
                null, // status is set by server
                null, // publicationDate is set by server
                0,
                null  // company
        );

        mockMvc.perform(post("/api/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOffer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Palets de Madera 1"));
    }

    @Test
    @DisplayName("Test 3: Security Door - Access denied when not authenticated")
    void shouldFailWhenNotAuthenticated() throws Exception {
        OfferDTO newOffer = new OfferDTO(
                null,
                "Intento de hackeo",
                "Sin token JWT",
                "PLASTIC",
                100.0,
                "kg",
                0.0,
                "Inmediata",
                null,
                null,
                0,
                null
        );

        // Sin @WithMockUser, el SecurityFilterChain intercepta la petición.
        // Dado que hemos desactivado formLogin y configurado STATELESS, 
        // una petición sin credenciales a una ruta protegida devuelve HTTP 403 Forbidden.
        mockMvc.perform(post("/api/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOffer)))
                .andExpect(status().isForbidden());
    }
}
