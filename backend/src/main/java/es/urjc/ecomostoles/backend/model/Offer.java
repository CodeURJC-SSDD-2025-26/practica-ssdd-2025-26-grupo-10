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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Represents an Offer in the domain model.
 * Each offer is associated with a specific company (Company).
 */
@Entity
public class Offer {

    /**
     * Unique identifier for the offer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the offer.
     */
    @NotBlank(message = "El título es obligatorio")
    private String title;

    /**
     * Detailed description of the offer.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Type of waste or material.
     */
    @NotBlank(message = "Debes seleccionar un tipo de residuo")
    private String wasteType;

    /**
     * Quantity of the material offered.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor que cero")
    private Double quantity;

    /**
     * Unit of measurement for the quantity (e.g., kg, ton).
     */
    private String unit;

    /**
     * Price of the offer.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double price;

    /**
     * Availability status or details.
     */
    private String availability;

    /**
     * Current state of the offer (e.g., active, ended).
     */
    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    /**
     * Date and time when the offer was published.
     */
    private LocalDateTime publicationDate;

    /**
     * Image associated with the offer, stored as a byte array.
     */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    /**
     * Number of times the offer has been viewed by users.
     */
    private int visits = 0;

    /**
     * The company that created this offer.
     */
    @ManyToOne
    private Company company;

    /**
     * Default constructor required by JPA.
     */
    public Offer() {
    }

    /**
     * Constructor with all parameters.
     *
     * @param title            title of the offer
     * @param description      detailed description
     * @param wasteType        type of waste
     * @param quantity         quantity offered
     * @param unit             unit of measurement
     * @param price            price
     * @param availability     availability status
     * @param status           current state
     * @param publicationDate  publication date and time
     * @param image            image byte array
     * @param company          associated company
     */
    public Offer(String title, String description, String wasteType, Double quantity, String unit,
            Double price, String availability, OfferStatus status, LocalDateTime publicationDate,
            byte[] image, Company company) {
        this.title = title;
        this.description = description;
        this.wasteType = wasteType;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.availability = availability;
        this.status = status;
        this.publicationDate = publicationDate;
        this.image = image;
        this.company = company;
    }

    /** GETTERS AND SETTERS **/

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    // Format the residue type to be user-friendly (e.g., "residuo_madera" ->
    // "Residuo madera")
    public String getFormattedWasteType() {
        if (this.wasteType == null || this.wasteType.isEmpty())
            return "";
        String formatted = this.wasteType.replace("_", " ");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1).toLowerCase();
    }

    public String getCategory() {
        return getFormattedWasteType();
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    // Format quantity without decimals if it's a whole number
    public String getFormattedQuantity() {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        return df.format(this.quantity);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Gets the price formatted as a currency string according to Spanish locale.
     *
     * @return the formatted price string (e.g., 1.200 € or 1.200,50 €)
     */
    public String getFormattedPrice() {
        if (this.price == null) {
            return "0,00 €";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
        return formatter.format(this.price);
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public String getFormattedPublicationDate() {
        if (this.publicationDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.publicationDate);
    }
}
