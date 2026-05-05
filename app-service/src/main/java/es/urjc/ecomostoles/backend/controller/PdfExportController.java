package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import es.urjc.ecomostoles.backend.mapper.AgreementMapper;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * PDF export controller — delegates PDF generation to utility-service via HTTP.
 *
 * <p>This controller is the integration boundary between app-service and the
 * utility-service microservice. All OpenPDF logic has been removed from this
 * service; the only responsibility here is:
 * <ol>
 *   <li>Authenticate and authorise the requesting company (IDOR protection).</li>
 *   <li>Project the {@link AgreementDTO} onto the JSON payload expected by
 *       {@code POST /api/v1/pdf/generate-agreement}.</li>
 *   <li>Relay the PDF byte[] returned by utility-service to the browser with
 *       the correct {@code Content-Type} and {@code Content-Disposition} headers.</li>
 * </ol>
 *
 * <p>The utility-service base URL is externalised to {@code application.properties}
 * via {@code utility.service.url} so it can be overridden per environment
 * (dev → localhost:8081, prod → internal DNS name).</p>
 */
@Controller
public class PdfExportController {

    private static final Logger log = LoggerFactory.getLogger(PdfExportController.class);

    // ── Date formatter matching the format expected by AgreementReportRequest ──
    private static final DateTimeFormatter PDF_DATE_FMT =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm", Locale.of("es", "ES"));

    private final AgreementService agreementService;
    private final CompanyService   companyService;
    private final AgreementMapper  agreementMapper;

    /**
     * Base URL of the utility-service, injected from application.properties.
     * Default: {@code http://localhost:8081} for local development.
     */
    @Value("${utility.service.url:http://localhost:8081}")
    private String utilityServiceUrl;

    public PdfExportController(AgreementService agreementService,
                               CompanyService companyService,
                               AgreementMapper agreementMapper) {
        this.agreementService = agreementService;
        this.companyService   = companyService;
        this.agreementMapper  = agreementMapper;
    }

    /**
     * Triggers PDF generation for a single agreement and streams the result to
     * the browser as an attachment download.
     *
     * <p>Access is restricted to parties directly involved in the agreement
     * (origin or destination company) or platform ADMIN accounts.</p>
     *
     * @param id        the agreement's primary key.
     * @param principal the authenticated Spring Security principal (email).
     * @return {@code 200 OK} with {@code application/pdf} binary body,
     *         or an appropriate HTTP error status on failure.
     */
    @GetMapping("/acuerdo/{id}/pdf")
    public ResponseEntity<byte[]> generateAgreementPdf(
            @PathVariable Long id, Principal principal) {

        // ── 1. Auth guard ───────────────────────────────────────────────
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var agreement = agreementService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agreement not found: " + id));

        var loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean isAdmin = loggedCompany.getRoles() != null
                && loggedCompany.getRoles().contains("ADMIN");
        boolean isOwner = (agreement.getOriginCompany() != null
                && agreement.getOriginCompany().getContactEmail().equals(principal.getName()))
                || (agreement.getDestinationCompany() != null
                && agreement.getDestinationCompany().getContactEmail().equals(principal.getName()));

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to export this agreement");
        }

        // ── 2. Project AgreementDTO → JSON payload for utility-service ──
        AgreementDTO dto = agreementMapper.toDto(agreement);

        // Build the exact field map that AgreementReportRequest expects.
        // Using a plain Map<String, Object> avoids introducing a utility-service
        // compile dependency in app-service — the JSON contract is the interface.
        Map<String, Object> requestBody = Map.ofEntries(
                Map.entry("agreementId",            dto.id()),
                Map.entry("status",                 dto.status() != null ? dto.status().getDisplayName() : "N/A"),
                Map.entry("originCompanyName",      dto.originCompany()      != null ? dto.originCompany().getCommercialName()      : "N/A"),
                Map.entry("destinationCompanyName", dto.destinationCompany() != null ? dto.destinationCompany().getCommercialName() : "N/A"),
                Map.entry("exchangedMaterial",      dto.exchangedMaterial() != null ? dto.exchangedMaterial() : "N/A"),
                Map.entry("quantity",               dto.quantity()           != null ? dto.quantity()           : 0.0),
                Map.entry("unit",                   dto.unit()               != null ? dto.unit()               : ""),
                Map.entry("agreedPrice",            dto.agreedPrice()        != null ? dto.agreedPrice()        : 0.0),
                Map.entry("platformCommission",     dto.platformCommission() != null ? dto.platformCommission() : 0.0),
                Map.entry("co2Impact",              dto.co2Impact()          != null ? dto.co2Impact()          : 0.0),
                Map.entry("pickupDate",             dto.pickupDate()         != null ? dto.pickupDate().toString() : ""),
                Map.entry("registrationDate",       dto.getFormattedRegistrationDate()),
                Map.entry("notes",                  dto.notes()              != null ? dto.notes()              : "")
        );

        // ── 3. Call utility-service with RestClient ─────────────────────
        log.info("[PDF] Delegating PDF generation for agreement #{} to utility-service", id);

        try {
            byte[] pdfBytes = RestClient.create(utilityServiceUrl)
                    .post()
                    .uri("/api/v1/pdf/generate-agreement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_PDF)
                    .body(requestBody)
                    .retrieve()
                    .body(byte[].class);

            if (pdfBytes == null || pdfBytes.length == 0) {
                log.error("[PDF] utility-service returned empty body for agreement #{}", id);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            log.info("[PDF] Received {} bytes from utility-service for agreement #{}", pdfBytes.length, id);

            // ── 4. Forward PDF to browser with correct headers ──────────
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "albaran_acuerdo_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RestClientException ex) {
            log.error("[PDF] utility-service call failed for agreement #{}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
