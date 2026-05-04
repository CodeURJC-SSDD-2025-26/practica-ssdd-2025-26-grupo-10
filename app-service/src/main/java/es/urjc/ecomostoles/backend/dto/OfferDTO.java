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
        es.urjc.ecomostoles.backend.model.OfferStatus status,
        LocalDateTime publicationDate,
        int visits,
        CompanyDTO company
) {
    public String getFormattedPrice() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.formatCurrency(this.price);
    }
    public String getFormattedQuantity() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.format(this.quantity);
    }
    public String getCategory() {
        return getFormattedWasteType();
    }
    public String getFormattedWasteType() {
        if (this.wasteCategory == null) return "";
        try {
            return es.urjc.ecomostoles.backend.model.WasteCategory.valueOf(this.wasteCategory).getDisplayName();
        } catch (IllegalArgumentException e) {
            for (es.urjc.ecomostoles.backend.model.WasteCategory cat : es.urjc.ecomostoles.backend.model.WasteCategory.values()) {
                if (cat.getDisplayName().equalsIgnoreCase(this.wasteCategory) || 
                    cat.name().equalsIgnoreCase(this.wasteCategory)) {
                    return cat.getDisplayName();
                }
            }
            return this.wasteCategory;
        }
    }
    public String getFormattedPublicationDate() {
        if (this.publicationDate == null) return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.publicationDate);
    }
    public boolean isClosed() {
        return es.urjc.ecomostoles.backend.model.OfferStatus.FINISHED.equals(this.status);
    }
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
                offer.getStatus(),
                offer.getPublicationDate(),
                offer.getVisits(),
                offer.getCompany() != null ? new CompanyDTO(offer.getCompany()) : null
        );
    }
}
