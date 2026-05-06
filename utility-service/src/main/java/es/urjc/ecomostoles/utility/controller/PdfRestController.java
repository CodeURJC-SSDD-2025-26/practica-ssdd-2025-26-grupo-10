package es.urjc.ecomostoles.utility.controller;

import es.urjc.ecomostoles.utility.dto.AgreementDTO;
import es.urjc.ecomostoles.utility.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pdfs")
public class PdfRestController {

    private final PdfGenerationService pdfGenerationService;

    public PdfRestController(PdfGenerationService pdfGenerationService) {
        this.pdfGenerationService = pdfGenerationService;
    }

    @PostMapping("/certificate")
    public ResponseEntity<byte[]> generateCertificate(@RequestBody AgreementDTO agreement) {
        byte[] pdfBytes = pdfGenerationService.generateCertificate(agreement);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
