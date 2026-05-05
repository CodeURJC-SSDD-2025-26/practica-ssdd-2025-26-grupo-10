package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.ChartDataDTO;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for exporting analytical dashboard data.
 *
 * <p>Base path: {@code /api/v1/charts}</p>
 */
@RestController
@RequestMapping("/api/v1/charts")
@Tag(name = "Dashboard", description = "Analytical data exports for charting libraries")
public class DashboardRestController {

    private final OfferService offerService;
    private final DemandService demandService;
    private final AgreementService agreementService;

    public DashboardRestController(OfferService offerService, DemandService demandService, AgreementService agreementService) {
        this.offerService = offerService;
        this.demandService = demandService;
        this.agreementService = agreementService;
    }

    @Operation(summary = "Get global impact statistics", description = "Returns structured chart data arrays for platform-wide distributions.")
    @ApiResponse(responseCode = "200", description = "Data successfully fetched",
            content = @Content(schema = @Schema(implementation = ChartDataDTO.class)))
    @GetMapping("/impact")
    public ResponseEntity<ChartDataDTO> getImpactChartData() {

        // Collecting data using the core services
        double totalOffers = offerService.countAll();
        double totalDemands = demandService.countAll();
        double activeAgreements = agreementService.countAll();

        // Building the parallel arrays for Chart.js or equivalent frontend libraries
        List<String> labels = List.of("Ofertas Publicadas", "Demandas Publicadas", "Acuerdos Totales");
        List<Double> data = List.of(totalOffers, totalDemands, activeAgreements);

        ChartDataDTO chartDataDTO = new ChartDataDTO(labels, data);

        return ResponseEntity.ok(chartDataDTO);
    }
}
