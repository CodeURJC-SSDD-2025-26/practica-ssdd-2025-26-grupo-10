package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Agreement;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AgreementDTO(
        Long id,
        String exchangedMaterial,
        Double quantity,
        String unit,
        Double agreedPrice,
        LocalDate pickupDate,
        es.urjc.ecomostoles.backend.model.AgreementStatus status,
        String notes,
        LocalDateTime registrationDate,
        CompanyDTO originCompany,
        CompanyDTO destinationCompany,
        Long offerId,
        Long demandId,
        Double platformCommission,
        Double co2Impact
) {
    public String getFormattedAgreedPrice() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.formatCurrency(this.agreedPrice);
    }
    public String getFormattedPlatformCommission() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.formatCurrency(this.platformCommission);
    }
    public String getFormattedCo2Impact() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.format(this.co2Impact);
    }
    public String getFormattedQuantity() {
        return es.urjc.ecomostoles.backend.utils.NumberFormatter.format(this.quantity);
    }
    public String getFormattedRegistrationDate() {
        if (this.registrationDate == null) return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.registrationDate);
    }
    public boolean isDeletable() {
        return !es.urjc.ecomostoles.backend.model.AgreementStatus.COMPLETED.equals(this.status);
    }
    public AgreementDTO(Agreement agreement) {
        this(
                agreement.getId(),
                agreement.getExchangedMaterial(),
                agreement.getQuantity(),
                agreement.getUnit(),
                agreement.getAgreedPrice(),
                agreement.getPickupDate(),
                agreement.getStatus(),
                agreement.getNotes(),
                agreement.getRegistrationDate(),
                agreement.getOriginCompany() != null ? new CompanyDTO(agreement.getOriginCompany()) : null,
                agreement.getDestinationCompany() != null ? new CompanyDTO(agreement.getDestinationCompany()) : null,
                agreement.getOffer() != null ? agreement.getOffer().getId() : null,
                agreement.getDemand() != null ? agreement.getDemand().getId() : null,
                agreement.getPlatformCommission(),
                agreement.getCo2Impact()
        );
    }
}
