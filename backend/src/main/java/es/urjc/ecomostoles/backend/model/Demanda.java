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
 * Represents a Demanda (Demand/Request) in the domain model.
 * Each demand is associated with an Empresa (Company) that requests it.
 */
@Entity
public class Demanda {

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
    private String titulo;

    /**
     * Category of the requested material.
     */
    @NotBlank(message = "Debes seleccionar una categoría de material")
    private String categoriaMaterial;

    /**
     * Detailed description of the demand.
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Quantity needed.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor que cero")
    private Double cantidad;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    private String unidad;

    /**
     * Urgency level of the demand.
     */
    private String urgencia;

    /**
     * Maximum budget for this demand.
     */
    @NotNull(message = "El presupuesto máximo es obligatorio")
    @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
    private Double presupuestoMaximo;

    /**
     * Zone or area for pickup/delivery.
     */
    private String zonaRecogida;

    /**
     * Validity period of the demand.
     */
    private String vigencia;

    /**
     * Status of the demand (e.g., Open, Closed).
     */
    @Enumerated(EnumType.STRING)
    private EstadoDemanda estado;

    /**
     * Date and time when the demand was published.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * The company that published this demand.
     */
    @ManyToOne
    private Empresa empresa;

    /**
     * Default constructor for JPA.
     */
    public Demanda() {
    }

    /**
     * Full constructor for Demanda.
     * 
     * @param titulo title
     * @param categoriaMaterial material category
     * @param descripcion long description
     * @param cantidad amount
     * @param unidad unit
     * @param urgencia urgency
     * @param presupuestoMaximo max budget
     * @param zonaRecogida area
     * @param vigencia validity
     * @param estado status
     * @param fechaPublicacion publication date
     * @param empresa company
     */
    public Demanda(String titulo, String categoriaMaterial, String descripcion, Double cantidad, String unidad, 
                   String urgencia, Double presupuestoMaximo, String zonaRecogida, String vigencia, EstadoDemanda estado, 
                   LocalDateTime fechaPublicacion, Empresa empresa) {
        this.titulo = titulo;
        this.categoriaMaterial = categoriaMaterial;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.urgencia = urgencia;
        this.presupuestoMaximo = presupuestoMaximo;
        this.zonaRecogida = zonaRecogida;
        this.vigencia = vigencia;
        this.estado = estado;
        this.fechaPublicacion = fechaPublicacion;
        this.empresa = empresa;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoriaMaterial() {
        return categoriaMaterial;
    }

    public void setCategoriaMaterial(String categoriaMaterial) {
        this.categoriaMaterial = categoriaMaterial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Gets the quantity formatted as a string, removing trailing decimal zeros.
     *
     * @return the formatted quantity string (e.g., 10 instead of 10.0)
     */
    public String getCantidadFormateada() {
        if (this.cantidad == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(this.cantidad);
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public Double getPresupuestoMaximo() {
        return presupuestoMaximo;
    }

    public void setPresupuestoMaximo(Double presupuestoMaximo) {
        this.presupuestoMaximo = presupuestoMaximo;
    }

    /**
     * Gets the maximum budget formatted as a currency string according to Spanish locale.
     *
     * @return the formatted budget string (e.g., 500,00 €)
     */
    public String getPresupuestoFormateado() {
        if (this.presupuestoMaximo == null) {
            return "0,00 €";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
        return formatter.format(this.presupuestoMaximo);
    }

    public String getZonaRecogida() {
        return zonaRecogida;
    }

    public void setZonaRecogida(String zonaRecogida) {
        this.zonaRecogida = zonaRecogida;
    }

    public String getVigencia() {
        return vigencia;
    }

    public void setVigencia(String vigencia) {
        this.vigencia = vigencia;
    }

    public EstadoDemanda getEstado() {
        return estado;
    }

    public void setEstado(EstadoDemanda estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    // Format zonaRecogida to capitalize the first letter
    public String getZonaRecogidaFormateada() {
        if (this.zonaRecogida == null || this.zonaRecogida.isEmpty()) return "";
        return this.zonaRecogida.substring(0, 1).toUpperCase() + this.zonaRecogida.substring(1).toLowerCase();
    }

    public String getFechaPublicacionFormateada() {
        if (this.fechaPublicacion == null) return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.fechaPublicacion);
    }
}
