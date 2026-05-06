package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.mapper.CompanyMapper;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import es.urjc.ecomostoles.backend.controller.api.exception.GlobalRestControllerAdvice;

import java.security.Principal;
import java.util.NoSuchElementException;

/**
 * REST API controller for the Company resource.
 *
 * <p>
 * Provides paginated discovery and full CRUD lifecycle for corporate tenant
 * entities. All responses are strictly projected through {@link CompanyDTO}
 * ensuring that sensitive fields (raw password hash, binary logo bytes,
 * internal
 * role lists) are never serialised into the JSON payload.
 * </p>
 *
 * <p>
 * Error handling is fully delegated to
 * {@link GlobalRestControllerAdvice}
 * — no try/catch blocks are needed here.
 * </p>
 *
 * <p>
 * Base path: {@code /api/v1/companies}
 * </p>
 */
@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "CRUD operations for corporate tenant management")
public class CompanyRestController {

        private static final Logger log = LoggerFactory.getLogger(CompanyRestController.class);
        private static final String NOT_FOUND_MSG = "Company not found with id: ";

        private final CompanyService companyService;
        private final CompanyMapper companyMapper;

        public CompanyRestController(CompanyService companyService, CompanyMapper companyMapper) {
                this.companyService = companyService;
                this.companyMapper = companyMapper;
        }

        // ─────────────────────────────────────────────────────────────────────────
        // GET /api/v1/companies
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Returns a paginated list of companies.
         *
         * <p>
         * Always returns a paginated JSON object (Page) to ensure consistency
         * with the web application requirements.
         * </p>
         *
         * @param search   Optional search filter.
         * @param pageable Pagination and sorting metadata.
         * @return a {@link org.springframework.data.domain.Page} of {@link CompanyDTO}.
         */
        @Operation(summary = "List companies (Paginated)", description = "Returns a paginated object containing companies. Metadata includes totalElements and totalPages.")
        @ApiResponse(responseCode = "200", description = "Companies returned successfully")
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<CompanyDTO>> getAllCompanies(
                        @RequestParam(required = false) String search,
                        @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

                log.debug("[API] GET /api/v1/companies — Search: {}, Pageable: {}", search, pageable);

                Page<CompanyDTO> resultPage;
                if (search != null && !search.isBlank()) {
                        resultPage = companyService.searchClientsPaginated(search, pageable)
                                        .map(companyMapper::toDto);
                } else {
                        resultPage = companyService.findAllPaginated(pageable)
                                        .map(companyMapper::toDto);
                }

                return ResponseEntity.ok(resultPage);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // GET /api/v1/companies/me
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Returns the profile of the currently authenticated company.
         *
         * @param principal the authenticated user (JWT principal).
         * @return the matching {@link CompanyDTO}.
         */
        @Operation(summary = "Get MY company profile", description = "Retrieves the full profile of the currently authenticated company.")
        @ApiResponse(responseCode = "200", description = "Company found", content = @Content(schema = @Schema(implementation = CompanyDTO.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT required", content = @Content)
        @ApiResponse(responseCode = "404", description = "Company not found", content = @Content)
        @GetMapping("/me")
        public ResponseEntity<CompanyDTO> getMyCompanyProfile(Principal principal) {
                if (principal == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
                }

                log.debug("[API] GET /api/v1/companies/me — Principal: {}", principal.getName());

                Company company = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Authenticated company not found: " + principal.getName()));

                return ResponseEntity.ok(companyMapper.toDto(company));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // GET /api/v1/companies/{id}
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Returns the public profile of a single company by its primary key.
         *
         * <p>
         * If no company exists with the given {@code id}, a
         * {@link NoSuchElementException} is thrown and intercepted by
         * {@code GlobalRestControllerAdvice} → HTTP 404.
         * </p>
         *
         * @param id the company's database primary key.
         * @return the matching {@link CompanyDTO}.
         */
        @Operation(summary = "Get company by ID", description = "Retrieves the public-safe profile of a single company. "
                        +
                        "Returns 404 if the company does not exist.")
        @ApiResponse(responseCode = "200", description = "Company found", content = @Content(schema = @Schema(implementation = CompanyDTO.class)))
        @ApiResponse(responseCode = "404", description = "Company not found", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        @GetMapping("/{id}")
        public ResponseEntity<CompanyDTO> getCompanyById(
                        @Parameter(description = "Database primary key of the company", example = "1") @PathVariable Long id,
                        java.security.Principal principal) {

                log.debug("[API] GET /api/v1/companies/{}", id);

                Company company = companyService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_MSG + id));

                // IDOR Protection: Only the profile owner or an ADMIN can see the full detail
                // via API
                boolean isAdmin = principal != null && SecurityContextHolder.getContext()
                                .getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (principal == null || (!company.getContactEmail().equals(principal.getName()) && !isAdmin)) {
                        log.warn("[SECURITY] Access denied to company detail: User {} attempted to view profile {}",
                                        principal != null ? principal.getName() : "anonymous", id);
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN, "Access denied: You can only view your own profile");
                }

                return ResponseEntity.ok(companyMapper.toDto(company));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PUT /api/v1/companies/me
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Updates the profile of the currently authenticated company.
         *
         * @param updateForm inbound DTO validated by Bean Validation.
         * @param principal  authenticated user.
         * @return the updated {@link CompanyDTO}.
         */
        @Operation(summary = "Update MY company profile", description = "Allows the authenticated company to update its own public fields.")
        @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(schema = @Schema(implementation = CompanyDTO.class)))
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
        @PutMapping("/me")
        public ResponseEntity<CompanyDTO> updateMyProfile(
                        @RequestBody CompanyDTO updateForm,
                        Principal principal) {

                if (principal == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
                }

                log.info("[API] PUT /api/v1/companies/me — Partial update for: {}", principal.getName());

                Company existing = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Company not found: " + principal.getName()));

                // Partial Update: Only apply fields that are not null in the request
                if (updateForm.getCommercialName() != null)
                        existing.setCommercialName(updateForm.getCommercialName());
                if (updateForm.getContactEmail() != null)
                        existing.setContactEmail(updateForm.getContactEmail());
                if (updateForm.getTaxId() != null)
                        existing.setTaxId(updateForm.getTaxId());
                if (updateForm.getAddress() != null)
                        existing.setAddress(updateForm.getAddress());
                if (updateForm.getPhone() != null)
                        existing.setPhone(updateForm.getPhone());
                if (updateForm.getIndustrialSector() != null)
                        existing.setIndustrialSector(updateForm.getIndustrialSector());
                if (updateForm.getDescription() != null)
                        existing.setDescription(updateForm.getDescription());

                Company updated = companyService.save(existing);
                return ResponseEntity.ok(companyMapper.toDto(updated));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PUT /api/v1/companies/{id}
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Updates the editable fields of an existing company.
         *
         * <p>
         * Only the fields exposed by {@link CompanyDTO} (commercial name, email,
         * address, phone, industrial sector, description) are accepted and applied.
         * Sensitive fields (password, logo, roles) are intentionally ignored — they
         * have dedicated, security-guarded endpoints in the web layer.
         * </p>
         *
         * <p>
         * Returns {@code 200 OK} with the updated DTO on success,
         * or {@code 404 NOT FOUND} if the ID does not match any company.
         * </p>
         *
         * @param id         the primary key of the company to update.
         * @param updateForm inbound DTO validated by Bean Validation.
         * @return the updated {@link CompanyDTO}.
         */
        @Operation(summary = "Update a company", description = "Applies changes to the public-editable fields of an existing company. "
                        +
                        "Sensitive fields (password hash, logo, role list) are immutable via this endpoint.")
        @ApiResponse(responseCode = "200", description = "Company updated successfully", content = @Content(schema = @Schema(implementation = CompanyDTO.class)))
        @ApiResponse(responseCode = "400", description = "Validation failed — check request body", content = @Content)
        @ApiResponse(responseCode = "404", description = "Company not found", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        @PutMapping("/{id}")
        public ResponseEntity<CompanyDTO> updateCompany(
                        @Parameter(description = "Database primary key of the company to update", example = "1") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update (only non-null fields are applied)", required = true) @RequestBody CompanyDTO updateForm,
                        Principal principal) {

                log.info("[API] PUT /api/v1/companies/{} — partial update attempt", id);

                Company existing = companyService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

                // IDOR Protection: Verify that the authenticated user is the owner of this
                // company profile OR an ADMIN
                boolean isAdmin = principal != null && SecurityContextHolder.getContext()
                                .getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (principal == null || (!existing.getContactEmail().equals(principal.getName()) && !isAdmin)) {
                        log.warn("[SECURITY] IDOR attempt blocked: User {} tried to update company profile {}",
                                        principal != null ? principal.getName() : "anonymous", id);
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN, "You are not authorized to update this company profile");
                }

                // Apply only the non-null fields provided in the DTO
                if (updateForm.getCommercialName() != null)
                        existing.setCommercialName(updateForm.getCommercialName());
                if (updateForm.getContactEmail() != null)
                        existing.setContactEmail(updateForm.getContactEmail());
                if (updateForm.getTaxId() != null)
                        existing.setTaxId(updateForm.getTaxId());
                if (updateForm.getAddress() != null)
                        existing.setAddress(updateForm.getAddress());
                if (updateForm.getPhone() != null)
                        existing.setPhone(updateForm.getPhone());
                if (updateForm.getIndustrialSector() != null)
                        existing.setIndustrialSector(updateForm.getIndustrialSector());
                if (updateForm.getDescription() != null)
                        existing.setDescription(updateForm.getDescription());

                Company updated = companyService.save(existing);
                log.info("[API] PUT /api/v1/companies/{} — update committed", id);

                return ResponseEntity.ok(companyMapper.toDto(updated));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // DELETE /api/v1/companies/{id}
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Permanently deletes a company from the platform.
         *
         * <p>
         * Returns {@code 204 NO CONTENT} on success (idiomatic REST — no body
         * returned after deletion). Returns {@code 404 NOT FOUND} if the ID does
         * not match any existing company, preventing ghost deletes from producing
         * misleading success responses.
         * </p>
         *
         * @param id the primary key of the company to delete.
         * @return {@code 204 NO CONTENT} with an empty body.
         */
        @Operation(summary = "Delete a company", description = "Permanently removes a company from the system. " +
                        "Returns 204 NO CONTENT on success. " +
                        "Returns 404 if the company does not exist.")
        @ApiResponse(responseCode = "204", description = "Company deleted successfully", content = @Content)
        @ApiResponse(responseCode = "404", description = "Company not found", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteCompany(
                        @Parameter(description = "Database primary key of the company to delete", example = "1") @PathVariable Long id) {

                log.info("[API] DELETE /api/v1/companies/{}", id);

                // Existence check — prevents a 204 on a ghost delete
                companyService.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

                companyService.delete(id);
                log.info("[API] DELETE /api/v1/companies/{} — company removed", id);

                return ResponseEntity.noContent().build(); // HTTP 204 — no body, idiomatic REST
        }
}
