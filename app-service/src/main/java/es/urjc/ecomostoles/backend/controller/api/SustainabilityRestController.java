package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;
import es.urjc.ecomostoles.backend.model.WasteCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sustainability")
@Tag(name = "Sustainability", description = "Environmental impact calculations")
public class SustainabilityRestController {

    private final SustainabilityEngine sustainabilityEngine;

    public SustainabilityRestController(SustainabilityEngine sustainabilityEngine) {
        this.sustainabilityEngine = sustainabilityEngine;
    }

    @Operation(summary = "Calculate estimated CO2 impact", description = "Calculates the CO2 reduction in Kg based on material quantity and category.")
    @ApiResponse(responseCode = "200", description = "Calculation successful")
    @GetMapping("/calculate-impact")
    public ResponseEntity<Map<String, Object>> calculateImpact(
            @Parameter(description = "Quantity of material", example = "500")
            @RequestParam Double quantity,
            @Parameter(description = "Waste category", example = "METAL_WASTE")
            @RequestParam String category) {

        double co2 = sustainabilityEngine.calculateCo2Impact(quantity, category);
        
        return ResponseEntity.ok(Map.of(
            "quantity", quantity,
            "category", category,
            "co2SavedKg", co2,
            "unit", "Kg"
        ));
    }
}
