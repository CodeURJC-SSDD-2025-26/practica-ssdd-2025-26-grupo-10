package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private OfferService offerService;

    private static final String TEST_EMAIL = "image_owner@test.com";

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Phase 2: Image - Upload company logo (Mocked Service 204)")
    void shouldUploadCompanyLogo() throws Exception {
        // Mock the company retrieval and save to avoid any DB/Disk 500 errors
        Company mockCompany = new Company();
        mockCompany.setId(1L);
        mockCompany.setContactEmail(TEST_EMAIL);

        when(companyService.findById(anyLong())).thenReturn(Optional.of(mockCompany));
        when(companyService.save(any(Company.class))).thenReturn(mockCompany);

        MockMultipartFile file = new MockMultipartFile(
                "imageFile", 
                "logo.png", 
                "image/png", 
                "mock-binary-data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/images/companies/1")
                        .file(file))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"COMPANY"})
    @DisplayName("Phase 2: Image - Download non-existent logo (404)")
    void shouldReturn404WhenLogoNotFound() throws Exception {
        Company mockCompany = new Company();
        mockCompany.setId(1L);
        mockCompany.setLogo(null);

        when(companyService.findById(anyLong())).thenReturn(Optional.of(mockCompany));

        mockMvc.perform(get("/api/v1/images/companies/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());
    }
}
