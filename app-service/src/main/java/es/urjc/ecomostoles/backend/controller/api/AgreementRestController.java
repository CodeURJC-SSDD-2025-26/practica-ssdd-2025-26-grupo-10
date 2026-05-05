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
import java.util.NoSuchElementException;

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

    @Operation(summary = "List agreements", description = "Returns a paginated list of commercial agreements.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of agreements returned successfully",
                    content = @Content(schema = @Schema(implementation = AgreementDTO.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<AgreementDTO>> getAllAgreements(
            @ParameterObject
            @PageableDefault(size = 12, sort = "registrationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("[API] GET /api/v1/agreements -- page: {}", pageable.getPageNumber());
        Page<Agreement> page = agreementService.getAllPaginated(pageable);
        return ResponseEntity.ok(page.map(agreementMapper::toDto));
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
        
        // Use a harcoded user email for Phase 3 (will be extracted from JWT in Phase 4)
        String userEmail = "contacto@metalesdelsur.es";
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
            @Valid @RequestBody AgreementDTO agreementDTO) {
        
        log.info("[API] PUT /api/v1/agreements/{}", id);
        
        // Delegating entirely to the updateAgreement method in the service which handles side-effects
        Agreement updatedData = agreementMapper.toEntity(agreementDTO);
        Agreement updated = agreementService.updateAgreement(id, updatedData);
        
        return ResponseEntity.ok(agreementMapper.toDto(updated));
    }

    @Operation(summary = "Delete an agreement", description = "Cancels or deletes an agreement, if allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agreement deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete agreement in its current state"),
            @ApiResponse(responseCode = "404", description = "Agreement not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgreement(@PathVariable Long id) {
        log.info("[API] DELETE /api/v1/agreements/{}", id);

        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agreement not found with id: " + id));

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
