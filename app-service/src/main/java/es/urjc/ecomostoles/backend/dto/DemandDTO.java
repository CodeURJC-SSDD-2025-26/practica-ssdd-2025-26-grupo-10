package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Demand;
import java.time.LocalDateTime;

public record DemandDTO(
        Long id,
        String title,
        String wasteCategory,
        String description,
        Double quantity,
        String unit,
        String urgency,
        Double maxBudget,
        String pickupZone,
        String validity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime publicationDate,
        LocalDateTime expiryDate,
        int visits,
        CompanyDTO company
) {
    public DemandDTO(Demand demand) {
        this(
                demand.getId(),
                demand.getTitle(),
                demand.getWasteCategory(),
                demand.getDescription(),
                demand.getQuantity(),
                demand.getUnit(),
                demand.getUrgency(),
                demand.getMaxBudget(),
                demand.getPickupZone(),
                demand.getValidity(),
                demand.getStatus() != null ? demand.getStatus().name() : null,
                demand.getCreatedAt(),
                demand.getPublicationDate(),
                demand.getExpiryDate(),
                demand.getVisits(),
                demand.getCompany() != null ? new CompanyDTO(demand.getCompany()) : null
        );
    }
}
