package es.urjc.ecomostoles.utility.controller;

import es.urjc.ecomostoles.utility.dto.AgreementReportRequest;
import es.urjc.ecomostoles.utility.service.PdfGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for PDF generation requests.
 * Delegates to the premium PdfGeneratorService to ensure high-quality, 
 * aesthetic corporate reports.
 */
@RestController
@RequestMapping("/api/v1/pdfs")
@Tag(name = "PDF Generation API", description = "Endpoints for generating premium PDF reports")
public class PdfRestController {

    private static final Logger log = LoggerFactory.getLogger(PdfRestController.class);
    private final PdfGeneratorService pdfGeneratorService;

    public PdfRestController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    /**
     * Generates a premium PDF certificate for an industrial agreement.
     * 
     * @param request The full data payload required for the report.
     * @return 200 OK with the PDF byte array.
     */
    @Operation(summary = "Generate Agreement PDF", description = "Generates a premium PDF certificate for an industrial agreement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generated successfully", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error during PDF generation", content = @Content)
    })
    @PostMapping("/certificate")
    public ResponseEntity<byte[]> generateCertificate(@RequestBody AgreementReportRequest request) {
        log.info("[API] Request received to generate premium PDF for agreement ID: {}", request.agreementId());
        
        byte[] pdfBytes = pdfGeneratorService.generateAgreementReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        // Use a generic filename; the caller (app-service) usually overrides this 
        // with Content-Disposition in its own response.
        headers.setContentDispositionFormData("attachment", "certificate.pdf");

        log.info("[API] PDF generated successfully ({} bytes)", pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
