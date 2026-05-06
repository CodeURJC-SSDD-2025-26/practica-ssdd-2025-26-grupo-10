package es.urjc.ecomostoles.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for public platform-wide settings.
 */
@Schema(description = "Structured representation of active platform parameters")
public record GlobalConfigDTO(
        @Schema(description = "The official name of the platform", example = "EcoMóstoles B2B")
        String platformName,

        @Schema(description = "Public contact email for support", example = "soporte@ecomostoles.es")
        String contactEmail,

        @Schema(description = "The city where the platform is physically established", example = "Móstoles")
        String platformCity
) {}
