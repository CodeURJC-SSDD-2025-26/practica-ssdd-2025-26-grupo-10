package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Persistent schema detailing material necessities published by Tenants.
 * 
 * Implements JSR 380 structural constraints internally. Triggers custom @PrePersist lifecycle 
 * hooks ensuring temporal invariants (expiration calculations) are strictly synchronized 
 * before yielding to the database transaction context.
 */
@Entity
public class Demand {

    /**
     * Unique identifier for the demand.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the demand.
     */
    @NotBlank(message = "El título es obligatorio")
    private String title;

    /**
     * Category of waste or material.
     */
    @NotBlank(message = "Debes seleccionar una categoría de material")
    private String wasteCategory;

    /**
     * Detailed description of the demand.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Quantity needed.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor que cero")
    private Double quantity;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unit;

    /**
     * Urgency level of the demand.
     */
    @NotBlank(message = "El nivel de urgencia es obligatorio")
    private String urgency;

    /**
     * Maximum budget for this demand.
     */
    @NotNull(message = "El presupuesto máximo es obligatorio")
    @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
    private Double maxBudget;

    @NotBlank(message = "La zona de recogida es obligatoria")
    private String pickupZone;

    /**
     * Validity period of the demand.
     */
    @NotBlank(message = "La vigencia es obligatoria")
    private String validity;

    /**
     * Status of the demand (e.g., Open, Closed).
     */
    @Enumerated(EnumType.STRING)
    private DemandStatus status;

    /**
     * Internal timestamp for entity creation.
     */
    private LocalDateTime createdAt;

    /**
     * Date and time when the demand was published.
     */
    private LocalDateTime publicationDate;

    /**
     * Calculated expiration date for efficient filtering.
     */
    private LocalDateTime expiryDate;

    /**
     * Lifecycle management hook.
     * Intercepts transient entities prior to flush, aligning intrinsic properties and establishing calculated bounds.
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.publicationDate == null) {
            this.publicationDate = this.createdAt;
        }
        updateExpiryDate();
    }

    /**
     * Synchronizes expiryDate with current validity days.
     */
    public void updateExpiryDate() {
        if (publicationDate == null || validity == null || "0".equals(validity)) {
            this.expiryDate = null;
        } else {
            try {
                long days = Long.parseLong(validity);
                this.expiryDate = publicationDate.plusDays(days);
            } catch (NumberFormatException e) {
                this.expiryDate = null;
            }
        }
    }

    /**
     * Checks if the demand has expired.
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Calculates days remaining until expiration.
     */
    public Long getDaysRemaining() {
        if (expiryDate == null) return null;
        long days = java.time.Duration.between(LocalDateTime.now(), expiryDate).toDays();
        return days < 0 ? 0 : days;
    }

    /**
     * Number of times the demand has been viewed by users.
     */
    private int visits = 0;

    /**
     * The company that published this demand.
     */
    @ManyToOne
    private Company company;

    /**
     * Default constructor for JPA.
     */
    public Demand() {
    }

    /**
     * Full constructor for Demand.
     * 
     * @param title           title
     * @param wasteCategory   waste category
     * @param description     long description
     * @param quantity        amount
     * @param unit            unit
     * @param urgency         urgency
     * @param maxBudget       max budget
     * @param pickupZone      area
     * @param validity        validity
     * @param status          status
     * @param publicationDate publication date
     * @param company         company
     */
    public Demand(String title, String wasteCategory, String description, Double quantity, String unit,
            String urgency, Double maxBudget, String pickupZone, String validity, DemandStatus status,
            LocalDateTime publicationDate, Company company) {
        this.title = title;
        this.wasteCategory = wasteCategory;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.urgency = urgency;
        this.maxBudget = maxBudget;
        this.pickupZone = pickupZone;
        this.validity = validity;
        this.status = status;
        this.publicationDate = publicationDate;
        this.company = company;
        updateExpiryDate();
    }

    // Getters and Setters

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWasteCategory() {
        return wasteCategory;
    }

    public void setWasteCategory(String wasteCategory) {
        this.wasteCategory = wasteCategory;
    }

    public WasteCategory getWasteCategoryEnum() {
        if (this.wasteCategory == null) return null;
        try {
            return WasteCategory.valueOf(this.wasteCategory);
        } catch (IllegalArgumentException e) {
            // Match by display name
            for (WasteCategory cat : WasteCategory.values()) {
                if (cat.getDisplayName().equalsIgnoreCase(this.wasteCategory) || 
                    cat.name().equalsIgnoreCase(this.wasteCategory)) {
                    return cat;
                }
            }
            return null;
        }
    }

    // Helper for Mustache templates
    public String getFormattedWasteType() {
        WasteCategory cat = getWasteCategoryEnum();
        if (cat != null) return cat.getDisplayName();
        return this.wasteCategory != null ? this.wasteCategory : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the quantity formatted as a string, removing trailing decimal zeros.
     *
     * @return the formatted quantity string (e.g., 10 instead of 10.0)
     */
    public String getFormattedQuantity() {
        if (this.quantity == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(this.quantity);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    /**
     * Gets the maximum budget formatted as a currency string according to Spanish
     * locale.
     *
     * @return the formatted budget string (e.g., 500,00 €)
     */
    public String getFormattedBudget() {
        if (this.maxBudget == null) {
            return "0,00 €";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
        return formatter.format(this.maxBudget);
    }

    public String getPickupZone() {
        return pickupZone;
    }

    public void setPickupZone(String pickupZone) {
        this.pickupZone = pickupZone;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
        updateExpiryDate();
    }

    /**
     * Helper for Mustache templates to display validity as a readable string.
     */
    public String getFormattedValidity() {
        if (this.validity == null) return "No definido";
        return switch (this.validity) {
            case "7" -> "7 días";
            case "15" -> "15 días";
            case "30" -> "30 días";
            case "90" -> "90 días";
            case "0" -> "Indefinido / Consultar";
            default -> this.validity;
        };
    }

    public DemandStatus getStatus() {
        return status;
    }

    public void setStatus(DemandStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
        updateExpiryDate();
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    // Format pickupZone to capitalize the first letter
    public String getFormattedPickupZone() {
        if (this.pickupZone == null || this.pickupZone.isEmpty())
            return "";
        return this.pickupZone.substring(0, 1).toUpperCase() + this.pickupZone.substring(1).toLowerCase();
    }

    public String getFormattedPublicationDate() {
        if (this.publicationDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.publicationDate);
    }
}
