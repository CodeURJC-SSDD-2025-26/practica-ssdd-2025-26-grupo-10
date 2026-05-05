package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.MessageDTO;
import es.urjc.ecomostoles.backend.mapper.MessageMapper;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST API controller for the Message resource.
 *
 * <p>Base path: {@code /api/v1/messages}</p>
 */
@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Messages", description = "Asynchronous messaging between tenants")
public class MessageRestController {

    private static final Logger log = LoggerFactory.getLogger(MessageRestController.class);

    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final CompanyService companyService;

    public MessageRestController(MessageService messageService, MessageMapper messageMapper, CompanyService companyService) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
        this.companyService = companyService;
    }

    @Operation(summary = "List all messages", description = "Returns a list of all messages in the system (ordered by send date desc).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of messages returned successfully",
                    content = @Content(schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAllMessages() {
        log.debug("[API] GET /api/v1/messages");
        List<MessageDTO> messages = messageService.getAll().stream()
                .map(messageMapper::toDto)
                .toList();
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Get message by ID", description = "Retrieves the detail of a single message.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message found",
                    content = @Content(schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Message not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable Long id) {
        log.debug("[API] GET /api/v1/messages/{}", id);
        Message message = messageService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Message not found with id: " + id));
        return ResponseEntity.ok(messageMapper.toDto(message));
    }

    @Operation(summary = "Send a new message", description = "Delivers a new message from one tenant to another.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message sent successfully",
                    content = @Content(schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sender or Recipient not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageDTO messageDTO) {
        log.info("[API] POST /api/v1/messages -- sending message: '{}'", messageDTO.subject());

        // Get Sender and Recipient companies
        Long senderId = (messageDTO.sender() != null) ? messageDTO.sender().getId() : null;
        Long recipientId = (messageDTO.recipient() != null) ? messageDTO.recipient().getId() : null;

        Company sender = null;
        if (senderId != null) {
            sender = companyService.findById(senderId)
                    .orElseThrow(() -> new NoSuchElementException("Sender company not found with id: " + senderId));
        }

        Company recipient = null;
        if (recipientId != null) {
            recipient = companyService.findById(recipientId)
                    .orElseThrow(() -> new NoSuchElementException("Recipient company not found with id: " + recipientId));
        }

        Message newMessage = messageMapper.toEntity(messageDTO);
        newMessage.setSender(sender);
        newMessage.setRecipient(recipient);
        newMessage.setSendDate(LocalDateTime.now());
        newMessage.setRead(false);

        Message saved = messageService.save(newMessage);
        log.info("[API] POST /api/v1/messages -- saved with ID: {}", saved.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(messageMapper.toDto(saved));
    }

    @Operation(summary = "Delete a message", description = "Permanently removes a message.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        log.info("[API] DELETE /api/v1/messages/{}", id);

        messageService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Message not found with id: " + id));

        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
