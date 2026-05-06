package es.urjc.ecomostoles.backend.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.dto.MessageDTO;
import es.urjc.ecomostoles.backend.mapper.MessageMapper;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.MessageService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageMapper messageMapper;

    @MockBean
    private CompanyService companyService;

    private static final String TEST_EMAIL = "test@company.com";

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = { "COMPANY" })
    @DisplayName("Phase 3: Messaging - Send a message (201 Created)")
    void shouldSendMessage() throws Exception {
        // Mock companies
        Company sender = new Company();
        sender.setId(1L);
        Company recipient = new Company();
        recipient.setId(2L);

        when(companyService.findById(1L)).thenReturn(Optional.of(sender));
        when(companyService.findById(2L)).thenReturn(Optional.of(recipient));
        when(companyService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(sender)); // Added for IDOR/Impersonation
                                                                                      // check

        // Mock message mapping and saving
        Message mockMessage = new Message();
        mockMessage.setId(10L);

        when(messageMapper.toEntity(any(MessageDTO.class))).thenReturn(mockMessage);
        when(messageService.save(any(Message.class))).thenReturn(mockMessage);
        when(messageMapper.toDto(any(Message.class))).thenReturn(null);

        // Prepare DTO
        CompanyDTO senderDTO = new CompanyDTO();
        senderDTO.setId(1L);
        CompanyDTO recipientDTO = new CompanyDTO();
        recipientDTO.setId(2L);

        MessageDTO messageDTO = new MessageDTO(
                null,
                "Asunto de prueba",
                "Cuerpo del mensaje",
                null,
                false,
                senderDTO,
                recipientDTO);

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }
}
