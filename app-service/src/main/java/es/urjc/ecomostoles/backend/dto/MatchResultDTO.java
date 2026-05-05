package es.urjc.ecomostoles.backend.dto;

public record MatchResultDTO(
        OfferDTO offer,
        double matchScore,
        String matchReason
) {}
