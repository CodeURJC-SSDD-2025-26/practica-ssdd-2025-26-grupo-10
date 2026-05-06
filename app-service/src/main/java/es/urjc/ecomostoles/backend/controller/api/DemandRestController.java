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
import java.security.Principal;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API controller for the Demand resource — full CRUD.
 *
 * <p>
 * Base path: {@code /api/v1/demands}
 * </p>
 */
@RestController
@RequestMapping("/api/v1/demands")
@Tag(name = "Demands", description = "Full CRUD for material exchange demands")
public class DemandRestController {

        private static final Logger log = LoggerFactory.getLogger(DemandRestController.class);

        private final DemandService demandService;
        private final DemandMapper demandMapper;
        private final es.urjc.ecomostoles.backend.service.CompanyService companyService;

        public DemandRestController(DemandService demandService, DemandMapper demandMapper,
                        es.urjc.ecomostoles.backend.service.CompanyService companyService) {
                this.demandService = demandService;
                this.demandMapper = demandMapper;
                this.companyService = companyService;
        }

        // -------------------------------------------------------------------------
        // GET /api/v1/demands
        // -------------------------------------------------------------------------

        /**
         * Returns a paginated list of demands.
         *
         * <p>
         * Always returns a paginated JSON object (Page) to ensure consistency
         * with the web application requirements.
         * </p>
         *
         * @param pageable Pagination and sorting metadata.
         * @return a {@link org.springframework.data.domain.Page} of {@link DemandDTO}.
         */
        @Operation(summary = "List demands (Paginated)", description = "Returns a paginated object containing active demands. Metadata includes totalElements and totalPages.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Demands returned successfully"),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        })
        @GetMapping
        public ResponseEntity<Page<DemandDTO>> getAllDemands(
                        @ParameterObject @PageableDefault(size = 9, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable) {

                log.debug("[API] GET /api/v1/demands — Pageable: {}", pageable);

                // RBAC: Admins see everything; Companies only see ACTIVE demands
                boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                .getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                Page<es.urjc.ecomostoles.backend.model.Demand> resultPage;
                if (isAdmin) {
                        resultPage = demandService.getAllPaginated(pageable);
                } else {
                        resultPage = demandService.getByStatusPaginated(
                                        es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE, pageable);
                }

                Page<DemandDTO> dtoPage = resultPage.map(demandMapper::toDto);
                return ResponseEntity.ok(dtoPage);
        }

        /**
         * Returns a paginated list of demands belonging strictly to the authenticated
         * tenant.
         *
         * @param pageable  pagination metadata.
         * @param principal active session identifier.
         * @return a page of DemandDTOs owned by the user.
         */
        @Operation(summary = "List MY demands (Paginated)", description = "Returns only the demands created by the authenticated company.")
        @GetMapping("/me")
        public ResponseEntity<Page<DemandDTO>> getMyDemands(
                        @ParameterObject @PageableDefault(size = 10, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable,
                        Principal principal) {

                if (principal == null)
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

                log.debug("[API] GET /api/v1/demands/me — User: {}", principal.getName());

                es.urjc.ecomostoles.backend.model.Company company = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new NoSuchElementException("Company not found"));

                Page<DemandDTO> resultPage = demandService.getByCompanyPaginated(company, pageable)
                                .map(d -> new DemandDTO(d, principal.getName()));

                return ResponseEntity.ok(resultPage);
        }

        // -------------------------------------------------------------------------
        // GET /api/v1/demands/{id}
        // -------------------------------------------------------------------------

        @Operation(summary = "Get demand by ID", description = "Retrieves the complete detail of a single material exchange demand. "
                        +
                        "Returns 404 if the demand does not exist.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Demand found", content = @Content(schema = @Schema(implementation = DemandDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        })
        @GetMapping("/{id}")
        public ResponseEntity<DemandDTO> getDemandById(
                        @Parameter(description = "Database primary key of the demand", example = "1") @PathVariable Long id,
                        Principal principal) {

                log.debug("[API] GET /api/v1/demands/{} — User: {}", id,
                                principal != null ? principal.getName() : "Anonymous");

                Demand demand = demandService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

                // Privacy/RBAC: If not active, only owner or ADMIN can view it
                boolean isOwner = principal != null
                                && demand.getCompany().getContactEmail().equals(principal.getName());
                boolean isAdmin = principal != null
                                && org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                .getAuthentication().getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isActive = demand.getStatus() == es.urjc.ecomostoles.backend.model.DemandStatus.ACTIVE;

                if (!isActive && !isOwner && !isAdmin) {
                        log.warn("[SECURITY] Privacy breach attempt: User {} tried to view non-active demand {}",
                                        principal != null ? principal.getName() : "anonymous", id);
                        throw new org.springframework.web.server.ResponseStatusException(
                                        org.springframework.http.HttpStatus.FORBIDDEN,
                                        "Access denied: This demand is no longer active and you are not the owner.");
                }

                return ResponseEntity.ok(new DemandDTO(demand, principal != null ? principal.getName() : null));
        }

        // -------------------------------------------------------------------------
        // POST /api/v1/demands
        // -------------------------------------------------------------------------

        @Operation(summary = "Create a new demand", description = "Publishes a new material exchange demand. " +
                        "Returns 201 CREATED with a Location header pointing to the new resource. " +
                        "Fields 'id', 'publicationDate', 'expiryDate', 'createdAt' and 'visits' are set server-side.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Demand created successfully -- see Location header", content = @Content(schema = @Schema(implementation = DemandDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        })
        @PostMapping
        public ResponseEntity<DemandDTO> createDemand(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Demand data to publish (server-side fields are ignored)", required = true) @Valid @RequestBody DemandDTO demandDTO) {

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

                // Assign company from the authenticated user (JWT)
                String userEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                .getAuthentication().getName();
                es.urjc.ecomostoles.backend.model.Company company = companyService.findByEmail(userEmail)
                                .orElseThrow(() -> new java.util.NoSuchElementException(
                                                "Authenticated company not found: " + userEmail));

                newDemand.setCompany(company);

                Demand saved = demandService.save(newDemand);
                log.info("[API] POST /api/v1/demands -- saved with ID: {}", saved.getId());

                // Build Location header: /api/v1/demands/{id} -- CRITICAL for HTTP 201
                // compliance
                URI location = ServletUriComponentsBuilder
                                .fromCurrentRequest() // base = current POST URL
                                .path("/{id}") // append /{id} path segment
                                .buildAndExpand(saved.getId()) // substitute {id} with the new PK
                                .toUri();

                return ResponseEntity
                                .created(location) // HTTP 201 + Location header set
                                .body(new DemandDTO(saved, userEmail));
        }

        // -------------------------------------------------------------------------
        // PUT /api/v1/demands/{id}
        // -------------------------------------------------------------------------

        @Operation(summary = "Update an existing demand", description = "Replaces the editable fields of a demand. " +
                        "Company ownership, visit count and publication dates are preserved. " +
                        "Returns 404 if the demand does not exist.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Demand updated successfully", content = @Content(schema = @Schema(implementation = DemandDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed -- check request body", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        })
        @PutMapping("/{id}")
        public ResponseEntity<DemandDTO> updateDemand(
                        @Parameter(description = "Database primary key of the demand to update", example = "1") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New demand values (company, visits and publicationDate are ignored)", required = true) @Valid @RequestBody DemandDTO demandDTO,
                        Principal principal) {

                log.info("[API] PUT /api/v1/demands/{} -- updating fields", id);

                Demand existing = demandService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

                // Ownership check (IDOR protection) OR ADMIN override
                boolean isAdmin = principal != null
                                && org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                .getAuthentication().getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!existing.getCompany().getContactEmail().equals(principal.getName()) && !isAdmin) {
                        log.warn("[SECURITY] IDOR attempt blocked: User {} tried to update demand {} owned by {}",
                                        principal.getName(), id, existing.getCompany().getContactEmail());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "You do not have permission to modify this demand");
                }

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
                existing.setValidity(demandDTO.validity()); // Calling this will automatically trigger
                                                            // updateExpiryDate() internally

                if (demandDTO.status() != null) {
                        existing.setStatus(demandDTO.status());
                }

                Demand updated = demandService.save(existing);
                log.info("[API] PUT /api/v1/demands/{} -- update committed", id);

                return ResponseEntity.ok(new DemandDTO(updated, principal.getName()));
        }

        // -------------------------------------------------------------------------
        // DELETE /api/v1/demands/{id}
        // -------------------------------------------------------------------------

        @Operation(summary = "Delete a demand", description = "Permanently removes a demand from the platform. " +
                        "Returns 204 NO CONTENT on success. " +
                        "Returns 400 if the demand cannot be deleted (e.g. has associated agreements). " +
                        "Returns 404 if the demand does not exist.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Demand deleted successfully", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Cannot delete due to existing constraints", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Demand not found", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteDemand(
                        @Parameter(description = "Database primary key of the demand to delete", example = "1") @PathVariable Long id,
                        Principal principal) {

                log.info("[API] DELETE /api/v1/demands/{}", id);

                // Existence and ownership check -- prevents unauthorized deletion or misleading
                // 204
                Demand demand = demandService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Demand not found with id: " + id));

                // Ownership check (IDOR protection) OR ADMIN override
                boolean isAdmin = principal != null
                                && org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                .getAuthentication().getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!demand.getCompany().getContactEmail().equals(principal.getName()) && !isAdmin) {
                        log.warn("[SECURITY] IDOR attempt blocked: User {} tried to delete demand {} owned by {}",
                                        principal.getName(), id, demand.getCompany().getContactEmail());
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "You do not have permission to delete this demand");
                }

                // The demandService.delete(id) handles throwing a ResponseStatusException (400)
                // if there are existing agreements associated with this demand.
                demandService.delete(id);
                log.info("[API] DELETE /api/v1/demands/{} -- demand removed", id);

                return ResponseEntity.noContent().build(); // HTTP 204 -- no body
        }
}
