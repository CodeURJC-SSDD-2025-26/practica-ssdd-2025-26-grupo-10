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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;

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
    public ResponseEntity<?> getAllMessages(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Principal principal) {
        log.debug("[API] GET /api/v1/messages");

        // Plain list logic (no parameters)
        if (page == null || size == null) {
            List<Message> allMessages;
            if (principal == null) {
                allMessages = List.of();
            } else {
                Company userCompany = companyService.findByEmail(principal.getName())
                        .orElseThrow(() -> new NoSuchElementException("Authenticated user company not found"));

                // Only return messages where the user is sender OR recipient
                allMessages = messageService.getAll().stream()
                        .filter(m -> (m.getSender() != null && m.getSender().getId().equals(userCompany.getId())) ||
                                     (m.getRecipient() != null && m.getRecipient().getId().equals(userCompany.getId())))
                        .toList();
            }

            List<MessageDTO> messages = allMessages.stream()
                    .map(messageMapper::toDto)
                    .toList();
            return ResponseEntity.ok(messages);
        }

        // Pagination logic
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size);

        Page<MessageDTO> resultPage;
        if (principal == null) {
            resultPage = messageService.getAllPaginated(pageable).map(messageMapper::toDto);
        } else {
            Company userCompany = companyService.findByEmail(principal.getName())
                    .orElseThrow(() -> new NoSuchElementException("Authenticated user company not found"));
            resultPage = messageService.getByCompanyPaginated(userCompany, pageable).map(messageMapper::toDto);
        }

        return ResponseEntity.ok(resultPage);
    }

    @Operation(summary = "Get message by ID", description = "Retrieves the detail of a single message.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message found",
                    content = @Content(schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Message not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable Long id, Principal principal) {
        log.debug("[API] GET /api/v1/messages/{}", id);
        Message message = messageService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Message not found with id: " + id));

        // IDOR Protection: Verify that the principal is part of the message
        boolean isSender = message.getSender() != null && message.getSender().getContactEmail().equals(principal.getName());
        boolean isRecipient = message.getRecipient() != null && message.getRecipient().getContactEmail().equals(principal.getName());

        if (principal == null || (!isSender && !isRecipient)) {
            log.warn("[SECURITY] Unauthorized attempt to read message ID: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this message");
        }

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
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageDTO messageDTO, Principal principal) {
        log.info("[API] POST /api/v1/messages -- sending message: '{}'", messageDTO.subject());

        // Enforce sender identity from Principal (Impersonation protection)
        Company sender = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("Authenticated sender company not found: " + principal.getName()));

        // Get Recipient company from DTO
        Long recipientId = (messageDTO.recipient() != null) ? messageDTO.recipient().getId() : null;
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
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, Principal principal) {
        log.info("[API] DELETE /api/v1/messages/{}", id);

        Message message = messageService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Message not found with id: " + id));

        // IDOR Protection: Verify that the principal is the sender of the message
        if (principal == null || !message.getSender().getContactEmail().equals(principal.getName())) {
            log.warn("[SECURITY] Unauthorized attempt to delete message ID: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this message");
        }

        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
