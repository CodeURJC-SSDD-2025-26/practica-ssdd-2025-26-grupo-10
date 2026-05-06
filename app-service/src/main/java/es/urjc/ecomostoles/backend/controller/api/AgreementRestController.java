package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import es.urjc.ecomostoles.backend.mapper.AgreementMapper;
import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.service.AgreementService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API controller for the Agreement resource.
 *
 * <p>Base path: {@code /api/v1/agreements}</p>
 */
@RestController
@RequestMapping("/api/v1/agreements")
@Tag(name = "Agreements", description = "Management of B2B commercial agreements")
public class AgreementRestController {

    private static final Logger log = LoggerFactory.getLogger(AgreementRestController.class);

    private final AgreementService agreementService;
    private final AgreementMapper agreementMapper;

    public AgreementRestController(AgreementService agreementService, AgreementMapper agreementMapper) {
        this.agreementService = agreementService;
        this.agreementMapper = agreementMapper;
    }

    /**
     * Returns a list of agreements with dynamic response format.
     *
     * <p>If 'page' and 'size' are omitted, it returns a direct JSON array (List).
     * If they are provided, it returns a paginated JSON object (Page).</p>
     *
     * @param page Optional page index.
     * @param size Optional page size.
     * @return a {@link java.util.List} or {@link org.springframework.data.domain.Page} of {@link AgreementDTO}.
     */
    @Operation(
            summary     = "List agreements (Dynamic format)",
            description = "Returns all agreements as a direct array if no params provided, or a paginated object if page/size are set."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreements returned successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAllAgreements(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        log.debug("[API] GET /api/v1/agreements — Requested Page: {}, Size: {}", page, size);

        // Case 1: No pagination -> Return flat JSON Array
        if (page == null || size == null) {
            java.util.List<AgreementDTO> list = agreementService.findAllList()
                    .stream()
                    .map(agreementMapper::toDto)
                    .toList();
            return ResponseEntity.ok(list);
        }

        // Case 2: Pagination requested -> Return Spring Page Object
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, Sort.by("registrationDate").descending());

        Page<AgreementDTO> resultPage = agreementService
                .getAllPaginated(pageable)
                .map(agreementMapper::toDto);

        return ResponseEntity.ok(resultPage);
    }

    @Operation(summary = "Get agreement by ID", description = "Retrieves the detail of a single agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreement found",
                    content = @Content(schema = @Schema(implementation = AgreementDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agreement not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgreementDTO> getAgreementById(@PathVariable Long id) {
        log.debug("[API] GET /api/v1/agreements/{}", id);
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agreement not found with id: " + id));
        return ResponseEntity.ok(agreementMapper.toDto(agreement));
    }

    @Operation(summary = "Create an agreement", description = "Proposes a new commercial agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agreement created successfully",
                    content = @Content(schema = @Schema(implementation = AgreementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AgreementDTO> createAgreement(@Valid @RequestBody AgreementDTO agreementDTO) {
        log.info("[API] POST /api/v1/agreements");

        Agreement agreement = agreementMapper.toEntity(agreementDTO);
        
        // Extract user email from JWT security context
        String userEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long destinationCompanyId = (agreementDTO.destinationCompany() != null) ? agreementDTO.destinationCompany().getId() : null;

        agreementService.registerNewAgreement(agreement, userEmail, agreementDTO.offerId(), destinationCompanyId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(agreement.getId())
                .toUri();

        return ResponseEntity.created(location).body(agreementMapper.toDto(agreement));
    }

    @Operation(summary = "Update an agreement", description = "Updates fields or changes the state of an agreement (e.g., to COMPLETED).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreement updated successfully",
                    content = @Content(schema = @Schema(implementation = AgreementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Agreement not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<AgreementDTO> updateAgreement(
            @PathVariable Long id,
            @Valid @RequestBody AgreementDTO agreementDTO,
            Principal principal) {
        
        log.info("[API] PUT /api/v1/agreements/{}", id);
        
        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agreement not found with id: " + id));

        // IDOR Protection: Verify that the principal is part of the agreement
        boolean isOrigin = agreement.getOriginCompany() != null && agreement.getOriginCompany().getContactEmail().equals(principal.getName());
        boolean isDestination = agreement.getDestinationCompany() != null && agreement.getDestinationCompany().getContactEmail().equals(principal.getName());

        if (principal == null || (!isOrigin && !isDestination)) {
            log.warn("[SECURITY] IDOR attempt blocked: User {} tried to update agreement {}", 
                    principal != null ? principal.getName() : "anonymous", id);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to modify this agreement");
        }

        // Delegating entirely to the updateAgreement method in the service which handles side-effects
        Agreement updatedData = agreementMapper.toEntity(agreementDTO);
        Agreement updated = agreementService.updateAgreement(id, updatedData);
        
        return ResponseEntity.ok(agreementMapper.toDto(updated));
    }

    @Operation(summary = "Update agreement status", description = "Updates only the status of an existing agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Agreement not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AgreementDTO> updateAgreementStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> statusMap,
            Principal principal) {
        
        log.info("[API] PUT /api/v1/agreements/{}/status", id);

        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agreement not found with id: " + id));

        // IDOR Protection: Verify that the principal is part of the agreement
        boolean isOrigin = agreement.getOriginCompany() != null && agreement.getOriginCompany().getContactEmail().equals(principal.getName());
        boolean isDestination = agreement.getDestinationCompany() != null && agreement.getDestinationCompany().getContactEmail().equals(principal.getName());

        if (principal == null || (!isOrigin && !isDestination)) {
            log.warn("[SECURITY] IDOR attempt blocked: User {} tried to update status for agreement {}", 
                    principal != null ? principal.getName() : "anonymous", id);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to modify this agreement status");
        }

        String statusStr = statusMap.get("status");
        AgreementStatus newStatus = AgreementStatus.valueOf(statusStr);
        
        agreement.setStatus(newStatus);
        Agreement updated = agreementService.updateAgreement(id, agreement);
        
        return ResponseEntity.ok(agreementMapper.toDto(updated));
    }

    @Operation(summary = "Delete an agreement", description = "Cancels or deletes an agreement, if allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agreement deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete agreement in its current state"),
            @ApiResponse(responseCode = "404", description = "Agreement not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgreement(@PathVariable Long id, Principal principal) {
        log.info("[API] DELETE /api/v1/agreements/{}", id);

        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agreement not found with id: " + id));

        // IDOR Protection: Verify that the principal is part of the agreement (origin or destination)
        boolean isOrigin = agreement.getOriginCompany() != null && agreement.getOriginCompany().getContactEmail().equals(principal.getName());
        boolean isDestination = agreement.getDestinationCompany() != null && agreement.getDestinationCompany().getContactEmail().equals(principal.getName());

        if (principal == null || (!isOrigin && !isDestination)) {
            log.warn("[SECURITY] Unauthorized attempt to delete agreement ID: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this agreement");
        }

        // Refuse deletion if COMPLETED (business rule)
        if (AgreementStatus.COMPLETED.equals(agreement.getStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, 
                    "No se puede eliminar un acuerdo ya completado."
            );
        }

        agreementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
