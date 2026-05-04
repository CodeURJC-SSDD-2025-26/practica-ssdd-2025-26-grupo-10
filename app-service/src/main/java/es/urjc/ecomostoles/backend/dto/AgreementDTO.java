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
        String status,
        String notes,
        LocalDateTime registrationDate,
        CompanyDTO originCompany,
        CompanyDTO destinationCompany,
        Long offerId,
        Long demandId,
        Double platformCommission,
        Double co2Impact
) {
    public AgreementDTO(Agreement agreement) {
        this(
                agreement.getId(),
                agreement.getExchangedMaterial(),
                agreement.getQuantity(),
                agreement.getUnit(),
                agreement.getAgreedPrice(),
                agreement.getPickupDate(),
                agreement.getStatus() != null ? agreement.getStatus().name() : null,
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
