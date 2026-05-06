package es.urjc.ecomostoles.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record DemandDTO(
        Long id,

        @NotBlank(message = "El título es obligatorio") String title,

        @NotBlank(message = "Debes seleccionar un tipo de residuo") String wasteCategory,

        @NotBlank(message = "La descripción es obligatoria") String description,

        @NotNull(message = "La cantidad es obligatoria") @PositiveOrZero(message = "La cantidad no puede ser negativa") Double quantity,

        @NotBlank(message = "La unidad es obligatoria") String unit,

        String urgency,

        @NotNull(message = "El presupuesto es obligatorio") @PositiveOrZero(message = "El presupuesto no puede ser negativo") Double maxBudget,

        String pickupZone,
        String validity,
        es.urjc.ecomostoles.backend.model.DemandStatus status,
        LocalDateTime createdAt,
        LocalDateTime publicationDate,
        LocalDateTime expiryDate,
        int visits,
        CompanyDTO company,
        boolean owned) {
    public String getFormattedWasteType() {
        if (this.wasteCategory == null)
            return "";
        try {
            return es.urjc.ecomostoles.backend.model.WasteCategory.valueOf(this.wasteCategory).getDisplayName();
        } catch (IllegalArgumentException e) {
            for (es.urjc.ecomostoles.backend.model.WasteCategory cat : es.urjc.ecomostoles.backend.model.WasteCategory
                    .values()) {
                if (cat.getDisplayName().equalsIgnoreCase(this.wasteCategory) ||
                        cat.name().equalsIgnoreCase(this.wasteCategory)) {
                    return cat.getDisplayName();
                }
            }
            return this.wasteCategory;
        }
    }

    public String getFormattedQuantity() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.format(this.quantity);
    }

    public String getFormattedBudget() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.formatCurrency(this.maxBudget);
    }

    public String getFormattedPickupZone() {
        if (this.pickupZone == null || this.pickupZone.isEmpty())
            return "";
        return this.pickupZone.substring(0, 1).toUpperCase() + this.pickupZone.substring(1).toLowerCase();
    }

    public String getFormattedValidity() {
        if (this.validity == null)
            return "No definido";
        return switch (this.validity) {
            case "7" -> "7 días";
            case "15" -> "15 días";
            case "30" -> "30 días";
            case "90" -> "90 días";
            case "0" -> "Indefinido / Consultar";
            default -> this.validity;
        };
    }

    public String getFormattedPublicationDate() {
        if (this.publicationDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.publicationDate);
    }

    public boolean isExpired() {
        if (this.expiryDate == null)
            return false;
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public Long getDaysRemaining() {
        if (this.expiryDate == null)
            return null;
        long days = java.time.Duration.between(LocalDateTime.now(), this.expiryDate).toDays();
        return days < 0 ? 0 : days;
    }

    public DemandDTO(es.urjc.ecomostoles.backend.model.Demand demand, String currentUserEmail) {
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
                demand.getStatus(),
                demand.getCreatedAt(),
                demand.getPublicationDate(),
                demand.getExpiryDate(),
                demand.getVisits(),
                demand.getCompany() != null ? new CompanyDTO(demand.getCompany()) : null,
                demand.getCompany() != null && currentUserEmail != null
                        && demand.getCompany().getContactEmail().equals(currentUserEmail));
    }

    public DemandDTO(es.urjc.ecomostoles.backend.model.Demand demand) {
        this(demand, null);
    }
}
