package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API for exposing system-wide configuration constants.
 */
@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "Global Configuration", description = "System-wide parameters and architectural constants")
public class GlobalConfigurationRestController {

    private final ConfigurationService configurationService;

    public GlobalConfigurationRestController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Operation(summary = "Get platform configuration", description = "Returns active platform parameters like contact email, city, and name.")
    @GetMapping
    public ResponseEntity<Map<String, String>> getGlobalConfig() {
        return ResponseEntity.ok(Map.of(
            "platformName", configurationService.getAutoValue("platformName"),
            "contactEmail", configurationService.getAutoValue("contactEmail"),
            "platformCity", configurationService.getAutoValue("platformCity")
        ));
    }
}
