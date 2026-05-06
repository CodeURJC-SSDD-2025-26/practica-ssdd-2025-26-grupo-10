package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import es.urjc.ecomostoles.backend.model.OfferStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API controller for managing binary image data (BLOBs) for Offers and Companies.
 *
 * <p>Base path: {@code /api/v1/images}</p>
 */
@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Images", description = "Endpoints for downloading and uploading images and logos")
public class ImageRestController {

    private static final Logger log = LoggerFactory.getLogger(ImageRestController.class);

    private final OfferService offerService;
    private final CompanyService companyService;

    public ImageRestController(OfferService offerService, CompanyService companyService) {
        this.offerService = offerService;
        this.companyService = companyService;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/images/offers/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Get offer image", description = "Retrieves the image binary data for a specific offer. " +
            "Only active offers are visible to everyone; non-active offers require ownership or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image returned successfully", content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "403", description = "Forbidden - Offer is not active", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer or image not found", content = @Content)
    })
    @GetMapping("/offers/{id}")
    public ResponseEntity<byte[]> getOfferImage(
            @Parameter(description = "ID of the offer", example = "1")
            @PathVariable Long id,
            Principal principal) {
        log.debug("[API] GET /api/v1/images/offers/{}", id);

        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Offer not found with id: " + id));

        // Security check: Only ACTIVE offers are public. Non-active require ownership or ADMIN.
        boolean isActive = offer.getStatus() == OfferStatus.ACTIVE;
        boolean isOwner = principal != null && offer.getCompany() != null &&
                offer.getCompany().getContactEmail().equals(principal.getName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isActive && !isOwner && !isAdmin) {
            log.warn("[SECURITY] Privacy breach attempt: User {} tried to view image of non-active offer {}",
                    principal != null ? principal.getName() : "anonymous", id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: This offer is no longer active and you are not the owner.");
        }

        if (offer.getImage() == null || offer.getImage().length == 0) {
            throw new NoSuchElementException("No image found for offer id: " + id);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(offer.getImage());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/images/companies/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Get company logo", description = "Retrieves the logo binary data for a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logo returned successfully", content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "404", description = "Company or logo not found", content = @Content)
    })
    @GetMapping("/companies/{id}")
    public ResponseEntity<byte[]> getCompanyLogo(
            @Parameter(description = "ID of the company", example = "1")
            @PathVariable Long id) {
        log.debug("[API] GET /api/v1/images/companies/{}", id);

        Company company = companyService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

        if (company.getLogo() == null || company.getLogo().length == 0) {
            throw new NoSuchElementException("No logo found for company id: " + id);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(company.getLogo());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/images/offers/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Upload offer image", description = "Uploads and saves a new image for a specific offer.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image uploaded successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid file or empty upload", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content)
    })
    @PostMapping(value = "/offers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadOfferImage(
            @Parameter(description = "ID of the offer", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Image file to upload")
            @RequestParam("imageFile") MultipartFile imageFile,
            Principal principal) throws IOException {

        log.info("[API] POST /api/v1/images/offers/{}", id);

        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("The provided image file is empty.");
        }

        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Offer not found with id: " + id));

        // IDOR Protection: Verify ownership or ADMIN role
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        boolean isOwner = offer.getCompany() != null && offer.getCompany().getContactEmail().equals(principal.getName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            log.warn("[SECURITY] IDOR attempt blocked: User {} tried to upload image for offer {} owned by {}",
                    principal.getName(), id, offer.getCompany() != null ? offer.getCompany().getContactEmail() : "N/A");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to upload images to this offer");
        }

        offer.setImage(imageFile.getBytes());
        offerService.save(offer);

        log.info("[API] Successfully updated image for offer ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/images/companies/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Upload company logo", description = "Uploads and saves a new logo for a specific company.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logo uploaded successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid file or empty upload", content = @Content),
            @ApiResponse(responseCode = "404", description = "Company not found", content = @Content)
    })
    @PostMapping(value = "/companies/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadCompanyLogo(
            @Parameter(description = "ID of the company", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Logo file to upload")
            @RequestParam("imageFile") MultipartFile imageFile,
            Principal principal) throws IOException {

        log.info("[API] POST /api/v1/images/companies/{}", id);

        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("The provided image file is empty.");
        }

        Company company = companyService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found with id: " + id));

        // IDOR Protection: Verify ownership or ADMIN role
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        boolean isOwner = company.getContactEmail().equals(principal.getName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            log.warn("[SECURITY] IDOR attempt blocked: User {} tried to upload logo for company {}",
                    principal.getName(), id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to update this company's logo");
        }

        company.setLogo(imageFile.getBytes());
        companyService.save(company);

        log.info("[API] Successfully updated logo for company ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
