package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.GlobalConfigDTO;
import es.urjc.ecomostoles.backend.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for exposing system-wide configuration constants.
 */
@RestController
@RequestMapping("/api/v1/configurations")
@Tag(name = "Global Configuration", description = "System-wide parameters and architectural constants")
public class GlobalConfigurationRestController {

    private final ConfigurationService configurationService;

    public GlobalConfigurationRestController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Operation(summary = "Get platform configuration", description = "Returns active platform parameters including business rules and taxonomies.")
    @ApiResponse(responseCode = "200", description = "Configuration returned successfully",
            content = @Content(schema = @Schema(implementation = GlobalConfigDTO.class)))
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalConfigDTO> getGlobalConfig() {
        return ResponseEntity.ok(new GlobalConfigDTO(
            configurationService.getAutoValue("platformName"),
            configurationService.getAutoValue("contactEmail"),
            configurationService.getAutoValue("platformCity"),
            configurationService.getAutoValue("platformCommission"),
            configurationService.getAutoValue("categoryList"),
            configurationService.getAutoValue("unitList"),
            configurationService.getAutoValue("availabilityList"),
            configurationService.getAutoValue("sectorList")
        ));
    }

    @Operation(summary = "Update platform configuration", description = "Updates global platform parameters. Restricted to Administrators.")
    @ApiResponse(responseCode = "200", description = "Configuration updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Error saving configuration")
    @PutMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateGlobalConfig(@org.springframework.web.bind.annotation.RequestBody GlobalConfigDTO config) {
        if (config.platformName() != null) configurationService.saveOrUpdateConfiguration("platformName", config.platformName());
        if (config.contactEmail() != null) configurationService.saveOrUpdateConfiguration("contactEmail", config.contactEmail());
        if (config.platformCity() != null) configurationService.saveOrUpdateConfiguration("platformCity", config.platformCity());
        if (config.platformCommission() != null) configurationService.saveOrUpdateConfiguration("platformCommission", config.platformCommission());
        if (config.categoryList() != null) configurationService.saveOrUpdateConfiguration("categoryList", config.categoryList());
        if (config.unitList() != null) configurationService.saveOrUpdateConfiguration("unitList", config.unitList());
        if (config.availabilityList() != null) configurationService.saveOrUpdateConfiguration("availabilityList", config.availabilityList());
        if (config.sectorList() != null) configurationService.saveOrUpdateConfiguration("sectorList", config.sectorList());
        
        return ResponseEntity.ok().build();
    }
}
