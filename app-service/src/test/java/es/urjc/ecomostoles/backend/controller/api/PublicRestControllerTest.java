package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.service.DemandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService offerService;

    @MockBean
    private DemandService demandService;

    @Test
    @DisplayName("Fase 4: Public Access - Listar ofertas de forma anónima (200 OK)")
    void shouldListPublicOffersAnonymously() throws Exception {
        when(offerService.getAllPaginated(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/public/offers"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Fase 4: Public Access - Listar demandas de forma anónima (200 OK)")
    void shouldListPublicDemandsAnonymously() throws Exception {
        when(demandService.getAllPaginated(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/public/demands"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }
}
