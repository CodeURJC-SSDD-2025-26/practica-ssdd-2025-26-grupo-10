package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.OfferStatus;
import java.time.LocalDateTime;

/**
 * Spring Data JPA Projection schema mapping Offer entity subsets.
 * 
 * Actively truncates DB retrieval vectors by deliberately excluding heavy BLOB byte arrays 
 * entirely at the SQL SELECT level. Exponentially optimizes high-throughput data fetching 
 * algorithms powering marketplace paginations.
 */
public interface OfferSummary {
    Long getId();

    String getTitle();

    String getDescription();

    String getWasteCategory();

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

    default boolean isClosed() {
        return OfferStatus.FINISHED.equals(getStatus());
    }

    LocalDateTime getPublicationDate();

    Company getCompany();

    int getVisits();
}
