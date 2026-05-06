package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API controller for high-level reporting and analytical exports.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Endpoints for platform auditing and data exports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportRestController {

    private final ReportService reportService;
    private final CompanyService companyService;
    private final OfferService offerService;
    private final AgreementService agreementService;

    public ReportRestController(ReportService reportService, CompanyService companyService, 
                                OfferService offerService, AgreementService agreementService) {
        this.reportService = reportService;
        this.companyService = companyService;
        this.offerService = offerService;
        this.agreementService = agreementService;
    }

    @Operation(summary = "Export companies as CSV", description = "Generates a CSV file containing all registered companies. Admin only.")
    @GetMapping("/companies")
    public ResponseEntity<byte[]> exportCompanies() {
        byte[] csv = reportService.generateUsersCsv(companyService.getAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(summary = "Export offers as CSV", description = "Generates a CSV file containing all platform offers. Admin only.")
    @GetMapping("/offers")
    public ResponseEntity<byte[]> exportOffers() {
        byte[] csv = reportService.generateOffersCsv(offerService.getAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_ofertas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(summary = "Get CO2 impact ranking", description = "Returns a list of companies ordered by their CO2 savings impact.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking returned successfully")
    })
    @GetMapping("/ranking")
    public ResponseEntity<List<CompanyDTO>> getCo2Ranking() {
        Map<Long, Double> co2Map = agreementService.getCO2Ranking();
        
        List<CompanyDTO> topCompanies = companyService.getAll().stream().map(e -> {
            CompanyDTO dto = new CompanyDTO(e);
            dto.setCo2Saved(co2Map.getOrDefault(e.getId(), 0.0));
            dto.setSector(e.getIndustrialSector() != null ? e.getIndustrialSector() : "Industria");
            return dto;
        }).sorted((a, b) -> b.getCo2Saved().compareTo(a.getCo2Saved()))
          .collect(Collectors.toList());

        for (int i = 0; i < topCompanies.size(); i++) {
            topCompanies.get(i).setRanking(i + 1);
        }

        return ResponseEntity.ok(topCompanies);
    }
}
