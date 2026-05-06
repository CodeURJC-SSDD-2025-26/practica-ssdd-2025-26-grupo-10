package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.MatchResultDTO;
import es.urjc.ecomostoles.backend.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for the AI-driven Smart Matching system.
 *
 * <p>Base path: {@code /api/v1/matches}</p>
 */
@RestController
@RequestMapping("/api/v1/matches")
@Tag(name = "Smart Matching", description = "Algorithmic matching system to pair demands with optimal supply")
public class MatchingRestController {

    private final MatchingService matchingService;
    private final es.urjc.ecomostoles.backend.service.DemandService demandService;

    public MatchingRestController(MatchingService matchingService, es.urjc.ecomostoles.backend.service.DemandService demandService) {
        this.matchingService = matchingService;
        this.demandService = demandService;
    }

    @Operation(summary = "Find optimal matches for a demand", description = "Executes the heuristics engine to calculate compatibility scores between a specific demand and the active market supply, returning the top 5 results sorted by score.")
    @ApiResponse(responseCode = "200", description = "Match results successfully calculated",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MatchResultDTO.class))))
    @GetMapping("/demands/{demandId}")
    public ResponseEntity<List<MatchResultDTO>> getBestMatches(@PathVariable Long demandId, java.security.Principal principal) {
        
        // IDOR Protection: Verify that the principal is the owner of the demand or an ADMIN
        es.urjc.ecomostoles.backend.model.Demand demand = demandService.findById(demandId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Demand not found"));

        boolean isOwner = demand.getCompany() != null && demand.getCompany().getContactEmail().equals(principal.getName());
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not authorized to view matches for this demand");
        }

        List<MatchResultDTO> matches = matchingService.findBestMatchesForDemand(demandId);
        
        return ResponseEntity.ok(matches);
    }
}
