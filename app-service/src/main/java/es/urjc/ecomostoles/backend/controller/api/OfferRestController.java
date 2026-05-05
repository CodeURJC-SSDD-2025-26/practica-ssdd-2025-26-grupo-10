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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * REST API controller for the Offer resource — read operations (GET).
 *
 * <h3>Pagination strategy</h3>
 * <ul>
 *   <li><b>List endpoint</b> (GET /) — uses {@link OfferSummary} Spring Data projections,
 *       which exclude BLOB image bytes at SQL level for maximum performance on large result sets.</li>
 *   <li><b>Detail endpoint</b> (GET /{id}) — fetches the full {@link Offer} entity and maps
 *       it to {@link OfferDTO} via {@link OfferMapper} for a complete representation.</li>
 * </ul>
 *
 * <h3>Keyword filtering</h3>
 * The optional {@code keyword} parameter routes the request through
 * {@code OfferService#searchFilteredOffers}, which applies a DB-level LIKE
 * predicate on title and description, returning only ACTIVE offers.
 * If {@code keyword} is absent or blank, all paginated offers are returned.
 *
 * <p>Base path: {@code /api/v1/offers}</p>
 */
@RestController
@RequestMapping("/api/v1/offers")
@Tag(name = "Offers", description = "Browse and retrieve material exchange offers")
public class OfferRestController {

    private static final Logger log = LoggerFactory.getLogger(OfferRestController.class);

    private final OfferService offerService;
    private final OfferMapper  offerMapper;

    public OfferRestController(OfferService offerService, OfferMapper offerMapper) {
        this.offerService = offerService;
        this.offerMapper  = offerMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/offers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of offers with optional keyword filtering.
     *
     * <p><b>Without keyword:</b> returns all offers regardless of status, sorted by
     * {@code publicationDate} descending.</p>
     *
     * <p><b>With keyword:</b> filters against title and description using a
     * case-insensitive LIKE predicate; only {@code ACTIVE} offers are returned.</p>
     *
     * <p>Pagination defaults: page 0, size 12, sorted by {@code publicationDate} desc.
     * Override via: {@code ?page=1&size=20&sort=title,asc}</p>
     *
     * <p>Note: the response uses the {@link OfferSummary} projection internally,
     * which excludes BLOB image bytes at SQL level. The JSON shape matches
     * {@link OfferDTO} for API consistency.</p>
     *
     * @param keyword  optional free-text search term (title / description).
     * @param pageable Spring-resolved pagination and sorting parameters.
     * @return a page of offers as lightweight JSON objects.
     */
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
                    example     = "plástico"
            )
            @RequestParam(required = false) String keyword,

            @ParameterObject
            @PageableDefault(size = 12, sort = "publicationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<OfferSummary> page;

        if (keyword != null && !keyword.isBlank()) {
            log.debug("[API] GET /api/v1/offers — keyword search: '{}', page: {}",
                    keyword, pageable.getPageNumber());
            // Delegates to DB-level LIKE predicate on title + description; ACTIVE only
            page = offerService.searchFilteredOffers(keyword, null, null, pageable);
        } else {
            log.debug("[API] GET /api/v1/offers — full listing, page: {}",
                    pageable.getPageNumber());
            page = offerService.getAllPaginated(pageable);
        }

        return ResponseEntity.ok(page);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/offers/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the complete detail of a single offer by its primary key.
     *
     * <p>Unlike the list endpoint, this fetches the full {@link Offer} entity
     * (including all fields) and maps it to {@link OfferDTO} via {@link OfferMapper},
     * ensuring sensitive or internal fields are not leaked.</p>
     *
     * <p>If no offer exists with the given {@code id}, a {@link NoSuchElementException}
     * is thrown and intercepted by {@code GlobalRestControllerAdvice} → HTTP 404.</p>
     *
     * @param id the offer's database primary key.
     * @return the full {@link OfferDTO} representation of the offer.
     */
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

        // Full entity → DTO mapping (BLOB image field excluded by OfferMapper)
        return ResponseEntity.ok(offerMapper.toDto(offer));
    }
}
