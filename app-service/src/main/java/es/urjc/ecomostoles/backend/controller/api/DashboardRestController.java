package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.ChartDataDTO;
import es.urjc.ecomostoles.backend.dto.AdminDashboardStatsDTO;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.model.AgreementStatus;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import es.urjc.ecomostoles.backend.utils.NumberFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST API controller for exporting analytical dashboard data.
 *
 * <p>
 * Base path: {@code /api/v1/charts}
 * </p>
 */
@RestController
@RequestMapping("/api/v1/charts")
@Tag(name = "Dashboard", description = "Analytical data exports for charting libraries")
public class DashboardRestController {

    private static final Logger log = LoggerFactory.getLogger(DashboardRestController.class);

    private final OfferService offerService;
    private final DemandService demandService;
    private final AgreementService agreementService;
    private final CompanyService companyService;

    public DashboardRestController(OfferService offerService, DemandService demandService,
            AgreementService agreementService, CompanyService companyService) {
        this.offerService = offerService;
        this.demandService = demandService;
        this.agreementService = agreementService;
        this.companyService = companyService;
    }

    @Operation(summary = "Get global impact statistics", description = "Returns structured chart data arrays for platform-wide distributions.")
    @ApiResponse(responseCode = "200", description = "Data successfully fetched", content = @Content(schema = @Schema(implementation = ChartDataDTO.class)))
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


    @Operation(summary = "Get administrative platform statistics", description = "Returns all platform-wide KPIs for the administrative dashboard.")
    @ApiResponse(responseCode = "200", description = "Admin stats fetched successfully", content = @Content(schema = @Schema(implementation = es.urjc.ecomostoles.backend.dto.AdminDashboardStatsDTO.class)))
    @GetMapping("/admin-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<es.urjc.ecomostoles.backend.dto.AdminDashboardStatsDTO> getAdminStats() {
        log.info("[Admin Dashboard] GET /charts/admin-stats — Collecting KPIs...");
        try {
            long users = companyService.countAll();
            long offers = offerService.countAll();
            long demands = demandService.countAll();
            long agreements = agreementService.countAll();

            long pending = agreementService.countByStatus(AgreementStatus.PENDING, null);
            long reported = offerService.countByStatus(OfferStatus.REPORTED);
            long completed = agreementService.countByStatus(AgreementStatus.COMPLETED, null);

            String co2 = NumberFormatter.format(agreementService.calculateCO2Saved());
            String commission = NumberFormatter.formatCurrency(agreementService.getTotalCommission());

            log.info("[Admin Dashboard] Success -> KPIs collected (Users: {}, Agreements: {})", users, agreements);

            return ResponseEntity.ok(new AdminDashboardStatsDTO(
                    users, offers, demands, agreements, pending, reported, completed, co2, commission));
        } catch (Exception e) {
            log.error("[Admin Dashboard] Critical failure during stats collection: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error calculating dashboard stats: " + e.getMessage());
        }
    }
}
