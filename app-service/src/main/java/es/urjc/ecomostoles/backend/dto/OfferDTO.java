package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Offer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record OfferDTO(
        Long id,
        
        @NotBlank(message = "El título es obligatorio")
        String title,
        
        @NotBlank(message = "La descripción es obligatoria")
        String description,
        
        @NotBlank(message = "Debes seleccionar un tipo de residuo")
        String wasteCategory,
        
        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Double quantity,
        
        @NotBlank(message = "La unidad es obligatoria")
        String unit,
        
        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        Double price,
        
        @NotBlank(message = "La disponibilidad es obligatoria")
        String availability,
        es.urjc.ecomostoles.backend.model.OfferStatus status,
        LocalDateTime publicationDate,
        int visits,
        CompanyDTO company,
        boolean owned
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
    /**
     * Constructor mapping from OfferSummary projection.
     */
    public OfferDTO(OfferSummary summary, String currentUserEmail) {
        this(
                summary.getId(),
                summary.getTitle(),
                summary.getDescription(),
                summary.getWasteCategory(),
                summary.getQuantity(),
                summary.getUnit(),
                summary.getPrice(),
                summary.getAvailability(),
                summary.getStatus(),
                summary.getPublicationDate(),
                summary.getVisits(),
                summary.getCompany() != null ? new CompanyDTO(summary.getCompany()) : null,
                summary.getCompany() != null && currentUserEmail != null && summary.getCompany().getContactEmail().equals(currentUserEmail)
        );
    }

    public OfferDTO(Offer offer, String currentUserEmail) {
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
                offer.getCompany() != null ? new CompanyDTO(offer.getCompany()) : null,
                offer.getCompany() != null && currentUserEmail != null && offer.getCompany().getContactEmail().equals(currentUserEmail)
        );
    }

    /**
     * Legacy constructor for internal mapping where ownership context is unknown.
     * Defaults 'owned' to false.
     */
    public OfferDTO(Offer offer) {
        this(offer, null);
    }
}
