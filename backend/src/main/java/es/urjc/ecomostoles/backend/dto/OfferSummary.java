package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import java.time.LocalDateTime;

/**
 * Spring Data Projection for Offer entity to avoid loading BLOB images in
 * listings.
 * Compatible with existing Mustache templates.
 */
public interface OfferSummary {
    Long getId();

    String getTitle();

    String getDescription();

    String getWasteType();

    @org.springframework.beans.factory.annotation.Value("#{target.formattedWasteType}")
    String getFormattedWasteType();

    @org.springframework.beans.factory.annotation.Value("#{target.formattedWasteType}")
    String getCategory();

    Double getQuantity();

    String getUnit();

    @org.springframework.beans.factory.annotation.Value("#{target.formattedQuantity}")
    String getFormattedQuantity();

    Double getPrice();

    @org.springframework.beans.factory.annotation.Value("#{target.formattedPrice}")
    String getFormattedPrice();

    String getAvailability();

    OfferStatus getStatus();

    LocalDateTime getPublicationDate();

    Company getCompany();

    int getVisits();
}
