package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.DemandDTO;
import es.urjc.ecomostoles.backend.mapper.DemandMapper;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.service.DemandService;
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
 * REST API controller for the Demand resource — full CRUD.
 *
 * <p>Base path: {@code /api/v1/demands}</p>
 */
@RestController
@RequestMapping("/api/v1/demands")
@Tag(name = "Demands", description = "Full CRUD for material exchange demands")
public class DemandRestController {

    private static final Logger log = LoggerFactory.getLogger(DemandRestController.class);

    private final DemandService demandService;
    private final DemandMapper demandMapper;

    public DemandRestController(DemandService demandService, DemandMapper demandMapper) {
        this.demandService = demandService;
        this.demandMapper = demandMapper;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/demands
    // -------------------------------------------------------------------------

    @Operation(
            summary = "List demands (paginated, optional keyword filter)",
            description = "Returns a paginated list of material exchange demands. " +
                    "Provide `keyword` to filter by title or description (ACTIVE demands only). " +
                    "Omit `keyword` to retrieve all demands across all statuses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of demands returned successfully",
                    content = @Content(schema = @Schema(implementation = DemandDTO.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<DemandDTO>> getAllDemands(
            @Parameter(
                    description = "Optional free-text keyword to search by title or description " +
                            "(returns only ACTIVE demands when provided)",
                    example = "madera"
            )
            @RequestParam(required = false) String keyword,

            @ParameterObject
            @PageableDefault(size = 12, sort = "publicationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Demand> page;

        if (keyword != null && !keyword.isBlank()) {
            log.debug("[API] GET /api/v1/demands -- keyword search: '{}', page: {}",
                    keyword, pageable.getPageNumber());
            page = demandService.searchFilteredDemands(keyword, es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE, pageable);
        } else {
            log.debug("[API] GET /api/v1/demands -- full listing, page: {}",
                    pageable.getPageNumber());
            page = demandService.getAllPaginated(pageable);
        }

        return ResponseEntity.ok(page.map(demandMapper::toDto));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/demands/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Get demand by ID",
            description = "Retrieves the complete detail of a single material exchange demand. " +
                    "Returns 404 if the demand does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demand found",
                    content = @Content(schema = @Schema(implementation = DemandDTO.class))),
            @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DemandDTO> getDemandById(
            @Parameter(description = "Database primary key of the demand", example = "1")
            @PathVariable Long id) {

        log.debug("[API] GET /api/v1/demands/{}", id);

        Demand demand = demandService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

        return ResponseEntity.ok(demandMapper.toDto(demand));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/demands
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Create a new demand",
            description = "Publishes a new material exchange demand. " +
                    "Returns 201 CREATED with a Location header pointing to the new resource. " +
                    "Fields 'id', 'publicationDate', 'expiryDate', 'createdAt' and 'visits' are set server-side."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Demand created successfully -- see Location header",
                    content = @Content(schema = @Schema(implementation = DemandDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DemandDTO> createDemand(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Demand data to publish (server-side fields are ignored)",
                    required = true)
            @Valid @RequestBody DemandDTO demandDTO) {

        log.info("[API] POST /api/v1/demands -- creating demand: '{}'", demandDTO.title());

        // Map inbound DTO to entity, then set server-controlled fields
        Demand newDemand = demandMapper.toEntity(demandDTO);
        newDemand.setVisits(0);
        newDemand.setStatus(
                demandDTO.status() != null
                        ? demandDTO.status()
                        : es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE);

        // createdAt, publicationDate, and expiryDate are automatically handled 
        // by the @PrePersist lifecycle hook in Demand entity.

        Demand saved = demandService.save(newDemand);
        log.info("[API] POST /api/v1/demands -- saved with ID: {}", saved.getId());

        // Build Location header: /api/v1/demands/{id}  -- CRITICAL for HTTP 201 compliance
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()           // base = current POST URL
                .path("/{id}")                  // append /{id} path segment
                .buildAndExpand(saved.getId())  // substitute {id} with the new PK
                .toUri();

        return ResponseEntity
                .created(location)              // HTTP 201 + Location header set
                .body(demandMapper.toDto(saved));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/demands/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update an existing demand",
            description = "Replaces the editable fields of a demand. " +
                    "Company ownership, visit count and publication dates are preserved. " +
                    "Returns 404 if the demand does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demand updated successfully",
                    content = @Content(schema = @Schema(implementation = DemandDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DemandDTO> updateDemand(
            @Parameter(description = "Database primary key of the demand to update", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New demand values (company, visits and publicationDate are ignored)",
                    required = true)
            @Valid @RequestBody DemandDTO demandDTO) {

        log.info("[API] PUT /api/v1/demands/{} -- updating fields", id);

        Demand existing = demandService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

        // Apply only editable fields; preserve audit/ownership data
        existing.setTitle(demandDTO.title());
        existing.setDescription(demandDTO.description());
        if (demandDTO.wasteCategory() != null) {
            existing.setWasteCategory(demandDTO.wasteCategory());
        }
        existing.setQuantity(demandDTO.quantity());
        existing.setUnit(demandDTO.unit());
        existing.setUrgency(demandDTO.urgency());
        existing.setMaxBudget(demandDTO.maxBudget());
        existing.setPickupZone(demandDTO.pickupZone());
        existing.setValidity(demandDTO.validity()); // Calling this will automatically trigger updateExpiryDate() internally
        
        if (demandDTO.status() != null) {
            existing.setStatus(demandDTO.status());
        }

        Demand updated = demandService.save(existing);
        log.info("[API] PUT /api/v1/demands/{} -- update committed", id);

        return ResponseEntity.ok(demandMapper.toDto(updated));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/demands/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Delete a demand",
            description = "Permanently removes a demand from the platform. " +
                    "Returns 204 NO CONTENT on success. " +
                    "Returns 400 if the demand cannot be deleted (e.g. has associated agreements). " +
                    "Returns 404 if the demand does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Demand deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot delete due to existing constraints", content = @Content),
            @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemand(
            @Parameter(description = "Database primary key of the demand to delete", example = "1")
            @PathVariable Long id) {

        log.info("[API] DELETE /api/v1/demands/{}", id);

        // Existence check -- prevents a misleading 204 on a non-existent resource
        demandService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

        // The demandService.delete(id) handles throwing a ResponseStatusException (400)
        // if there are existing agreements associated with this demand.
        demandService.delete(id);
        log.info("[API] DELETE /api/v1/demands/{} -- demand removed", id);

        return ResponseEntity.noContent().build();   // HTTP 204 -- no body
    }
}
