package es.urjc.ecomostoles.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for environmental impact calculation results.
 */
@Schema(description = "Detailed breakdown of the environmental impact for a material quantity")
public record SustainabilityImpactDTO(
        @Schema(description = "Input quantity of material", example = "500.0")
        Double quantity,

        @Schema(description = "Input waste category", example = "METAL_WASTE")
        String category,

        @Schema(description = "Calculated CO2 reduction in Kg", example = "225.0")
        Double co2SavedKg,

        @Schema(description = "Measurement unit for the saved impact", example = "Kg")
        String unit
) {}
