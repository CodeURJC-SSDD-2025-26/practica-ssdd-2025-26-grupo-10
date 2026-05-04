package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import es.urjc.ecomostoles.backend.utils.NumberFormatter;

/**
 * Core commercial entity mapping finalized B2B transactions.
 * 
 * Orchestrates the persistent state of environmental material transfers. Embeds relational 
 * links bridging original Offers and Demands, calculates platform commissions, and anchors 
 * the statistical CO2 reduction metrics for dashboard reporting.
 */
@Entity
public class Agreement {

    /**
     * Unique identifier for the agreement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Description of the material being exchanged.
     */
    private String exchangedMaterial;

    /**
     * Quantity of the material.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @jakarta.validation.constraints.Min(value = 1, message = "La cantidad debe ser de al menos 1 unidad")
    private Double quantity;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unit;

    /**
     * The price agreed upon for the transaction.
     */
    @NotNull(message = "El precio acordado es obligatorio")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double agreedPrice;

    /**
     * Scheduled date for the material pickup.
     */
    @NotNull(message = "La fecha de recogida es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate pickupDate;

    /**
     * Current status of the agreement (e.g., Pending, Completed).
     */
    @NotNull(message = "El estado del acuerdo es obligatorio")
    @Enumerated(EnumType.STRING)
    private AgreementStatus status;

    /**
     * Additional notes regarding the agreement.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Date and time when the agreement was registered in the system.
     */
    private LocalDateTime registrationDate;

    /**
     * The company providing the material.
     */
    @ManyToOne(optional = false)
    private Company originCompany;

    /**
     * The company receiving the material.
     */
    @ManyToOne
    private Company destinationCompany;

    /**
     * The original offer that led to this agreement (if applicable).
     */
    @ManyToOne
    private Offer offer;

    /**
     * The original demand that led to this agreement (if applicable).
     */
    @ManyToOne
    private Demand demand;

    /**
     * Profit earned by the platform for this transaction (commission).
     */
    private Double platformCommission;

    /**
     * Calculated CO2 impact (tons) saved by this agreement.
     */
    private Double co2Impact;

    /**
     * Default constructor for JPA.
     */
    public Agreement() {
    }

    /**
     * Full constructor for Agreement.
     */
    public Agreement(String exchangedMaterial, Double quantity, String unit, Double agreedPrice,
            LocalDate pickupDate, AgreementStatus status, String notes, LocalDateTime registrationDate,
            Company originCompany, Company destinationCompany, Offer offer, Demand demand, Double platformCommission) {
        this.exchangedMaterial = exchangedMaterial;
        this.quantity = quantity;
        this.unit = unit;
        this.agreedPrice = agreedPrice;
        this.pickupDate = pickupDate;
        this.status = status;
        this.notes = notes;
        this.registrationDate = registrationDate;
        this.originCompany = originCompany;
        this.destinationCompany = destinationCompany;
        this.offer = offer;
        this.demand = demand;
        this.platformCommission = platformCommission;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExchangedMaterial() {
        return exchangedMaterial;
    }

    public void setExchangedMaterial(String exchangedMaterial) {
        this.exchangedMaterial = exchangedMaterial;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(Double agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public Double getPlatformCommission() {
        return platformCommission;
    }
    public void setPlatformCommission(Double platformCommission) {
        this.platformCommission = platformCommission;
    }

    public Double getCo2Impact() {
        return co2Impact;
    }

    public void setCo2Impact(Double co2Impact) {
        this.co2Impact = co2Impact;
    }

    public String getFormattedPlatformCommission() {
        return NumberFormatter.formatCurrency(this.platformCommission);
    }

    public String getFormattedCo2Impact() {
        return NumberFormatter.format(this.co2Impact);
    }
    
    public String getFormattedQuantity() {
        return NumberFormatter.format(this.quantity);
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public AgreementStatus getStatus() {
        return status;
    }

    public void setStatus(AgreementStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Company getOriginCompany() {
        return originCompany;
    }

    public void setOriginCompany(Company originCompany) {
        this.originCompany = originCompany;
    }

    public Company getDestinationCompany() {
        return destinationCompany;
    }

    public void setDestinationCompany(Company destinationCompany) {
        this.destinationCompany = destinationCompany;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Demand getDemand() {
        return demand;
    }

    public void setDemand(Demand demand) {
        this.demand = demand;
    }

    public String getFormattedAgreedPrice() {
        return NumberFormatter.formatCurrency(this.agreedPrice);
    }

    public String getFormattedRegistrationDate() {
        if (this.registrationDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.registrationDate);
    }

    public boolean isDeletable() {
        return !AgreementStatus.COMPLETED.equals(this.status);
    }
}
