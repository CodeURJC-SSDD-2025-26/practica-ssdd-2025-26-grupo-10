package es.urjc.ecomostoles.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for a company's cumulative environmental performance.
 */
@Schema(description = "Aggregated sustainability metrics for a specific company")
public record CompanyImpactDTO(
        @Schema(description = "Total CO2 savings accumulated across all completed agreements", example = "1250.5")
        Double totalCo2Saved,

        @Schema(description = "Total quantity of waste material reintroduced into the circular economy", example = "5000.0")
        Double totalMaterialsManaged,

        @Schema(description = "The unit used for material weight (usually kg)", example = "kg")
        String unit
) {}
