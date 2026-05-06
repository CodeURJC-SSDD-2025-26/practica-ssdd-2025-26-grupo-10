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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * REST API controller for the Company resource.
 *
 * <p>Provides paginated discovery and full CRUD lifecycle for corporate tenant
 * entities. All responses are strictly projected through {@link CompanyDTO}
 * ensuring that sensitive fields (raw password hash, binary logo bytes, internal
 * role lists) are never serialised into the JSON payload.</p>
 *
 * <p>Error handling is fully delegated to
 * {@link es.urjc.ecomostoles.backend.controller.api.exception.GlobalRestControllerAdvice}
 * — no try/catch blocks are needed here.</p>
 *
 * <p>Base path: {@code /api/v1/companies}</p>
 */
@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "CRUD operations for corporate tenant management")
public class CompanyRestController {

    private static final Logger log = LoggerFactory.getLogger(CompanyRestController.class);

    private final CompanyService companyService;
    private final CompanyMapper  companyMapper;

    public CompanyRestController(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyMapper  = companyMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/companies
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a list of companies with dynamic response format.
     *
     * <p>If 'page' and 'size' are omitted, it returns a direct JSON array (List).
     * If they are provided, it returns a paginated JSON object (Page).</p>
     *
     * @param page Optional page index.
     * @param size Optional page size.
     * @return a {@link java.util.List} or {@link org.springframework.data.domain.Page} of {@link CompanyDTO}.
     */
    @Operation(
            summary     = "List companies (Dynamic format)",
            description = "Returns all companies as a direct array if no params provided, or a paginated object if page/size are set."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Companies returned successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        log.debug("[API] GET /api/v1/companies — Requested Page: {}, Size: {}", page, size);

        // Case 1: No pagination parameters -> Return flat JSON Array
        if (page == null || size == null) {
            java.util.List<CompanyDTO> list = companyService.findAllList()
                    .stream()
                    .map(companyMapper::toDto)
                    .toList();
            return ResponseEntity.ok(list);
        }

        // Case 2: Pagination requested -> Return Spring Page Object
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, Sort.by("id").ascending());

        Page<CompanyDTO> resultPage = companyService
                .findAllPaginated(pageable)
                .map(companyMapper::toDto);

        return ResponseEntity.ok(resultPage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/companies/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the public profile of a single company by its primary key.
     *
     * <p>If no company exists with the given {@code id}, a
     * {@link NoSuchElementException} is thrown and intercepted by
     * {@code GlobalRestControllerAdvice} → HTTP 404.</p>
     *
     * @param id the company's database primary key.
     * @return the matching {@link CompanyDTO}.
     */
    @Operation(
            summary     = "Get company by ID",
            description = "Retrieves the public-safe profile of a single company. " +
                          "Returns 404 if the company does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Company not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(
            @Parameter(description = "Database primary key of the company", example = "1")
            @PathVariable Long id) {

        log.debug("[API] GET /api/v1/companies/{}", id);

        Company company = companyService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/companies/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the editable fields of an existing company.
     *
     * <p>Only the fields exposed by {@link CompanyDTO} (commercial name, email,
     * address, phone, industrial sector, description) are accepted and applied.
     * Sensitive fields (password, logo, roles) are intentionally ignored — they
     * have dedicated, security-guarded endpoints in the web layer.</p>
     *
     * <p>Returns {@code 200 OK} with the updated DTO on success,
     * or {@code 404 NOT FOUND} if the ID does not match any company.</p>
     *
     * @param id         the primary key of the company to update.
     * @param updateForm inbound DTO validated by Bean Validation.
     * @return the updated {@link CompanyDTO}.
     */
    @Operation(
            summary     = "Update a company",
            description = "Applies changes to the public-editable fields of an existing company. " +
                          "Sensitive fields (password hash, logo, role list) are immutable via this endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated successfully",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — check request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Company not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(
            @Parameter(description = "Database primary key of the company to update", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update (only public-safe fields are applied)",
                    required = true)
            @Valid @RequestBody CompanyDTO updateForm) {

        log.info("[API] PUT /api/v1/companies/{} — updating fields: commercialName='{}', email='{}'",
                id, updateForm.getCommercialName(), updateForm.getContactEmail());

        Company existing = companyService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

        // Apply only the fields exposed in CompanyDTO — never touch password/logo/roles
        existing.setCommercialName(updateForm.getCommercialName());
        existing.setContactEmail(updateForm.getContactEmail());
        existing.setTaxId(updateForm.getTaxId());
        existing.setAddress(updateForm.getAddress());
        existing.setPhone(updateForm.getPhone());
        existing.setIndustrialSector(updateForm.getIndustrialSector());
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
     * <p>Returns {@code 204 NO CONTENT} on success (idiomatic REST — no body
     * returned after deletion). Returns {@code 404 NOT FOUND} if the ID does
     * not match any existing company, preventing ghost deletes from producing
     * misleading success responses.</p>
     *
     * @param id the primary key of the company to delete.
     * @return {@code 204 NO CONTENT} with an empty body.
     */
    @Operation(
            summary     = "Delete a company",
            description = "Permanently removes a company from the system. " +
                          "Returns 204 NO CONTENT on success. " +
                          "Returns 404 if the company does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Company deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Company not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompany(
            @Parameter(description = "Database primary key of the company to delete", example = "1")
            @PathVariable Long id) {

        log.info("[API] DELETE /api/v1/companies/{}", id);

        // Existence check — prevents a 204 on a ghost delete
        companyService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

        companyService.delete(id);
        log.info("[API] DELETE /api/v1/companies/{} — company removed", id);

        return ResponseEntity.noContent().build();   // HTTP 204 — no body, idiomatic REST
    }
}
