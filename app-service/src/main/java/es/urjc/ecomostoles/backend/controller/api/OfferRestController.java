package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.OfferDTO;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.mapper.OfferMapper;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.OfferService;
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
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * REST API controller for the Offer resource — full CRUD.
 *
 * <h3>Pagination strategy</h3>
 * <ul>
 *   <li><b>List endpoint</b> (GET /) uses {@link OfferSummary} Spring Data projections,
 *       excluding BLOB image bytes at SQL level for high-throughput paginations.</li>
 *   <li><b>Detail endpoint</b> (GET /{id}) fetches the full {@link Offer} entity and maps
 *       it to {@link OfferDTO} via {@link OfferMapper}.</li>
 * </ul>
 *
 * <h3>Keyword filtering</h3>
 * The optional {@code keyword} parameter routes through
 * {@code OfferService#searchFilteredOffers} (DB-level LIKE, ACTIVE offers only).
 * Omitting it returns all paginated offers regardless of status.
 *
 * <p>Base path: {@code /api/v1/offers}</p>
 */
@RestController
@RequestMapping("/api/v1/offers")
@Tag(name = "Offers", description = "Full CRUD for material exchange offers")
public class OfferRestController {

    private static final Logger log = LoggerFactory.getLogger(OfferRestController.class);

    private final OfferService offerService;
    private final OfferMapper  offerMapper;

    public OfferRestController(OfferService offerService, OfferMapper offerMapper) {
        this.offerService = offerService;
        this.offerMapper  = offerMapper;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/offers
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "List offers (paginated, optional keyword filter)",
            description = "Returns a paginated list of material exchange offers. " +
                          "Provide `keyword` to filter by title or description (ACTIVE offers only). " +
                          "Omit `keyword` to retrieve all offers across all statuses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of offers returned successfully",
                    content = @Content(schema = @Schema(implementation = OfferSummary.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<OfferSummary>> getAllOffers(
            @Parameter(
                    description = "Optional free-text keyword to search by title or description " +
                                  "(returns only ACTIVE offers when provided)",
                    example     = "plastico"
            )
            @RequestParam(required = false) String keyword,

            @ParameterObject
            @PageableDefault(size = 12, sort = "publicationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<OfferSummary> page;

        if (keyword != null && !keyword.isBlank()) {
            log.debug("[API] GET /api/v1/offers -- keyword search: '{}', page: {}",
                    keyword, pageable.getPageNumber());
            page = offerService.searchFilteredOffers(keyword, null, null, pageable);
        } else {
            log.debug("[API] GET /api/v1/offers -- full listing, page: {}",
                    pageable.getPageNumber());
            page = offerService.getAllPaginated(pageable);
        }

        return ResponseEntity.ok(page);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/offers/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Get offer by ID",
            description = "Retrieves the complete detail of a single material exchange offer. " +
                          "Returns 404 if the offer does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer found",
                    content = @Content(schema = @Schema(implementation = OfferDTO.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(
            @Parameter(description = "Database primary key of the offer", example = "1")
            @PathVariable Long id) {

        log.debug("[API] GET /api/v1/offers/{}", id);

        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Offer not found with id: " + id));

        return ResponseEntity.ok(offerMapper.toDto(offer));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/offers
    // -------------------------------------------------------------------------

    /**
     * Creates a new material exchange offer.
     *
     * Returns 201 CREATED with:
     *  - Location header: /api/v1/offers/{newId}  built via ServletUriComponentsBuilder
     *  - Body: the persisted OfferDTO
     *
     * Server-side fields (id, publicationDate, visits) are always overwritten
     * and must not be relied upon in the request body.
     */
    @Operation(
            summary     = "Create a new offer",
            description = "Publishes a new material exchange offer. " +
                          "Returns 201 CREATED with a Location header pointing to the new resource. " +
                          "Fields 'id', 'publicationDate' and 'visits' are set server-side."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Offer created successfully -- see Location header",
                    content = @Content(schema = @Schema(implementation = OfferDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<OfferDTO> createOffer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Offer data to publish (id, publicationDate and visits are ignored)",
                    required    = true)
            @Valid @RequestBody OfferDTO offerDTO) {

        log.info("[API] POST /api/v1/offers -- creating offer: '{}'", offerDTO.title());

        // Map inbound DTO to entity, then set server-controlled fields
        Offer newOffer = offerMapper.toEntity(offerDTO);
        newOffer.setPublicationDate(LocalDateTime.now());
        newOffer.setVisits(0);
        newOffer.setStatus(
                offerDTO.status() != null
                        ? offerDTO.status()
                        : es.urjc.ecomostoles.backend.model.OfferStatus.ACTIVE);

        Offer saved = offerService.save(newOffer);
        log.info("[API] POST /api/v1/offers -- saved with ID: {}", saved.getId());

        // Build Location header: /api/v1/offers/{id}  -- CRITICAL for HTTP 201 compliance
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()           // base = current POST URL
                .path("/{id}")                  // append /{id} path segment
                .buildAndExpand(saved.getId())  // substitute {id} with the new PK
                .toUri();

        return ResponseEntity
                .created(location)              // HTTP 201 + Location header set
                .body(offerMapper.toDto(saved));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/offers/{id}
    // -------------------------------------------------------------------------

    /**
     * Updates the editable fields of an existing offer.
     * Company ownership, publicationDate and visits counter are preserved from the DB record.
     */
    @Operation(
            summary     = "Update an existing offer",
            description = "Replaces the editable fields of an offer. " +
                          "Company ownership, visit count and publication date are preserved. " +
                          "Returns 404 if the offer does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer updated successfully",
                    content = @Content(schema = @Schema(implementation = OfferDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<OfferDTO> updateOffer(
            @Parameter(description = "Database primary key of the offer to update", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New offer values (company, visits and publicationDate are ignored)",
                    required    = true)
            @Valid @RequestBody OfferDTO offerDTO) {

        log.info("[API] PUT /api/v1/offers/{} -- updating fields", id);

        Offer existing = offerService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Offer not found with id: " + id));

        // Apply only editable fields; preserve audit/ownership data
        existing.setTitle(offerDTO.title());
        existing.setDescription(offerDTO.description());
        if (offerDTO.wasteCategory() != null) {
            existing.setWasteCategory(offerDTO.wasteCategory());
        }
        existing.setQuantity(offerDTO.quantity());
        existing.setUnit(offerDTO.unit());
        existing.setPrice(offerDTO.price());
        existing.setAvailability(offerDTO.availability());
        if (offerDTO.status() != null) {
            existing.setStatus(offerDTO.status());
        }

        Offer updated = offerService.save(existing);
        log.info("[API] PUT /api/v1/offers/{} -- update committed", id);

        return ResponseEntity.ok(offerMapper.toDto(updated));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/offers/{id}
    // -------------------------------------------------------------------------

    /**
     * Permanently deletes an offer.
     * Returns 204 NO CONTENT on success; 404 if the offer does not exist (prevents ghost-delete).
     */
    @Operation(
            summary     = "Delete an offer",
            description = "Permanently removes an offer from the platform. " +
                          "Returns 204 NO CONTENT on success. " +
                          "Returns 404 if the offer does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Offer deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(
            @Parameter(description = "Database primary key of the offer to delete", example = "1")
            @PathVariable Long id) {

        log.info("[API] DELETE /api/v1/offers/{}", id);

        // Existence check -- prevents a misleading 204 on a non-existent resource
        offerService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Offer not found with id: " + id));

        offerService.delete(id);
        log.info("[API] DELETE /api/v1/offers/{} -- offer removed", id);

        return ResponseEntity.noContent().build();   // HTTP 204 -- no body
    }
}
