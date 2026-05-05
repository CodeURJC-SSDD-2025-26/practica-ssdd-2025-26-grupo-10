package es.urjc.ecomostoles.utility.controller;

import es.urjc.ecomostoles.utility.dto.AgreementReportRequest;
import es.urjc.ecomostoles.utility.service.PdfGenerationException;
import es.urjc.ecomostoles.utility.service.PdfGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing PDF generation capabilities for the EcoMóstoles platform.
 *
 * This controller is the public API surface of utility-service. It accepts
 * structured JSON payloads from app-service (or any trusted caller), delegates
 * all rendering work to {@link PdfGeneratorService}, and returns the resulting
 * binary PDF with the correct HTTP headers.
 *
 * Versioning: /api/v1 prefix follows REST best practices and allows future
 * non-breaking evolution via /api/v2 alongside existing consumers.
 *
 * Security note: this service is intended to run behind the internal network.
 * No authentication is enforced here; callers are trusted microservices.
 */
@RestController
@RequestMapping("/api/v1/pdf")
public class PdfRestController {

    private static final Logger log = LoggerFactory.getLogger(PdfRestController.class);

    private final PdfGeneratorService pdfGeneratorService;

    public PdfRestController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    /**
     * Generates a branded PDF report for a single commercial agreement.
     *
     * <p><b>Request:</b> {@code POST /api/v1/pdf/generate-agreement}</p>
     * <p><b>Content-Type:</b> {@code application/json}</p>
     * <p><b>Response:</b> {@code application/pdf} binary stream</p>
     *
     * <p>The caller must project its internal AgreementDTO onto the
     * {@link AgreementReportRequest} record before dispatching the request.
     * No database access occurs here; all data must be supplied in the body.</p>
     *
     * @param request JSON payload containing all agreement data for the PDF.
     * @return {@code 200 OK} with PDF binary and attachment disposition headers,
     *         or {@code 500 Internal Server Error} if OpenPDF fails.
     */
    @PostMapping("/generate-agreement")
    public ResponseEntity<byte[]> generateAgreementPdf(
            @RequestBody AgreementReportRequest request) {

        log.info("[PdfRestController] Received PDF generation request for agreement ID: {}",
                request.agreementId());

        try {
            byte[] pdfBytes = pdfGeneratorService.generateAgreementReport(request);

            // ── HTTP Headers ─────────────────────────────────────────────
            HttpHeaders headers = new HttpHeaders();

            // Tell the browser / HTTP client this is a PDF file
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Force download with a meaningful filename
            String filename = "agreement_report_" + request.agreementId() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            // Explicit content length avoids chunked transfer issues
            headers.setContentLength(pdfBytes.length);

            log.info("[PdfRestController] PDF ready — {} bytes, filename: {}", pdfBytes.length, filename);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (PdfGenerationException ex) {
            log.error("[PdfRestController] PDF generation failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
