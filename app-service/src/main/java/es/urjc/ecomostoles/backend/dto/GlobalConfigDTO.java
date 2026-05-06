package es.urjc.ecomostoles.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for comprehensive platform-wide settings.
 * 
 * Synchronized with the administrative web panel to allow management 
 * of business rules (commissions) and taxonomy constants (lists).
 */
@Schema(description = "Structured representation of active platform parameters and business constants")
public record GlobalConfigDTO(
        @Schema(description = "The official name of the platform", example = "EcoMóstoles B2B")
        String platformName,

        @Schema(description = "Public contact email for support", example = "soporte@ecomostoles.es")
        String contactEmail,

        @Schema(description = "The city where the platform is physically established", example = "Móstoles")
        String platformCity,

        @Schema(description = "The current platform commission percentage applied to agreements", example = "5.0")
        String platformCommission,

        @Schema(description = "Comma-separated list of allowed waste categories")
        String categoryList,

        @Schema(description = "Comma-separated list of allowed measurement units")
        String unitList,

        @Schema(description = "Comma-separated list of allowed availability/urgency statuses")
        String availabilityList,

        @Schema(description = "Comma-separated list of industrial sectors for company profiles")
        String sectorList
) {}
