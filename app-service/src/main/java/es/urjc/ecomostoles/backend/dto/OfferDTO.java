package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Offer;
import java.time.LocalDateTime;

public record OfferDTO(
        Long id,
        String title,
        String description,
        String wasteCategory,
        Double quantity,
        String unit,
        Double price,
        String availability,
        String status,
        LocalDateTime publicationDate,
        int visits,
        CompanyDTO company
) {
    public OfferDTO(Offer offer) {
        this(
                offer.getId(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getWasteCategory(),
                offer.getQuantity(),
                offer.getUnit(),
                offer.getPrice(),
                offer.getAvailability(),
                offer.getStatus() != null ? offer.getStatus().name() : null,
                offer.getPublicationDate(),
                offer.getVisits(),
                offer.getCompany() != null ? new CompanyDTO(offer.getCompany()) : null
        );
    }
}
