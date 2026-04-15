package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
 * Represents a Demand (Demand/Request) in the domain model.
 * Each demand is associated with an Company (Company) that requests it.
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
     * Category of the requested material.
     */
    @NotBlank(message = "Debes seleccionar una categoría de material")
    private String materialCategory;

    /**
     * Detailed description of the demand.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Quantity needed.
     */
    @NotNull(message = "La quantity es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La quantity debe ser mayor que cero")
    private Double quantity;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    private String unit;

    /**
     * Urgency level of the demand.
     */
    private String urgency;

    /**
     * Maximum budget for this demand.
     */
    @NotNull(message = "El presupuesto máximo es obligatorio")
    @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
    private Double maxBudget;

    /**
     * Zone or area for pickup/delivery.
     */
    private String pickupZone;

    /**
     * Validity period of the demand.
     */
    private String validity;

    /**
     * Status of the demand (e.g., Open, Closed).
     */
    @Enumerated(EnumType.STRING)
    private DemandStatus status;

    /**
     * Date and time when the demand was published.
     */
    private LocalDateTime publicationDate;

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
     * @param title            title
     * @param materialCategory material category
     * @param description      long description
     * @param quantity         amount
     * @param unit             unit
     * @param urgency          urgency
     * @param maxBudget        max budget
     * @param pickupZone       area
     * @param validity         validity
     * @param status           status
     * @param publicationDate  publication date
     * @param company          company
     */
    public Demand(String title, String materialCategory, String description, Double quantity, String unit,
            String urgency, Double maxBudget, String pickupZone, String validity, DemandStatus status,
            LocalDateTime publicationDate, Company company) {
        this.title = title;
        this.materialCategory = materialCategory;
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
    }

    // Getters and Setters

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

    public String getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialCategory(String materialCategory) {
        this.materialCategory = materialCategory;
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
