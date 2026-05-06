package es.urjc.ecomostoles.backend.controller.api;

import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.dto.DemandDTO;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.mapper.DemandMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for anonymous/unauthenticated public data access.
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public Access", description = "Endpoints accessible without authentication")
public class PublicRestController {

    private final OfferService offerService;
    private final DemandService demandService;
    private final DemandMapper demandMapper;

    public PublicRestController(OfferService offerService, DemandService demandService, DemandMapper demandMapper) {
        this.offerService = offerService;
        this.demandService = demandService;
        this.demandMapper = demandMapper;
    }

    @Operation(summary = "List public offers", description = "Returns a paginated list of active offers.")
    @GetMapping("/offers")
    public ResponseEntity<Page<OfferSummary>> getPublicOffers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(offerService.getAllPaginated(pageable));
    }

    @Operation(summary = "List public demands", description = "Returns a paginated list of active demands.")
    @GetMapping("/demands")
    public ResponseEntity<Page<DemandDTO>> getPublicDemands(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(demandService.getAllPaginated(pageable).map(demandMapper::toDto));
    }
}
