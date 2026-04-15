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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents a transaction agreement (Acuerdo) between two companies.
 * This entity tracks the exchange of materials, quantities, and prices.
 */
@Entity
public class Acuerdo {

    /**
     * Unique identifier for the agreement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Description of the material being exchanged.
     */
    @NotBlank(message = "El material intercambiado es obligatorio")
    private String materialIntercambiado;

    /**
     * Quantity of the material.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @jakarta.validation.constraints.Min(value = 1, message = "La cantidad debe ser de al menos 1 unidad")
    private Double cantidad;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidad;

    /**
     * The price agreed upon for the transaction.
     */
    @NotNull(message = "El precio acordado es obligatorio")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double precioAcordado;

    /**
     * Scheduled date for the material pickup.
     */
    private LocalDate fechaRecogida;

    /**
     * Current status of the agreement (e.g., Pending, Completed).
     */
    @NotNull(message = "El estado del acuerdo es obligatorio")
    @Enumerated(EnumType.STRING)
    private EstadoAcuerdo estado;

    /**
     * Additional notes regarding the agreement.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notas;

    /**
     * Date and time when the agreement was registered in the system.
     */
    private LocalDateTime fechaRegistro;

    /**
     * The company providing the material.
     */
    @ManyToOne(optional = false)
    private Empresa empresaOrigen;

    /**
     * The company receiving the material.
     */
    @ManyToOne
    private Empresa empresaDestino;

    /**
     * The original offer that led to this agreement (if applicable).
     */
    @ManyToOne
    private Oferta oferta;

    /**
     * The original demand that led to this agreement (if applicable).
     */
    @ManyToOne
    private Demanda demanda;

    /**
     * Profit earned by the platform for this transaction (commission).
     */
    private Double beneficioPlataforma;

    /**
     * Calculated CO2 impact (tons) saved by this agreement.
     */
    private Double impactoCO2;

    /**
     * Default constructor for JPA.
     */
    public Acuerdo() {
    }

    /**
     * Full constructor for Acuerdo.
     */
    public Acuerdo(String materialIntercambiado, Double cantidad, String unidad, Double precioAcordado,
                   LocalDate fechaRecogida, EstadoAcuerdo estado, String notas, LocalDateTime fechaRegistro,
                   Empresa empresaOrigen, Empresa empresaDestino, Oferta oferta, Demanda demanda, Double beneficioPlataforma) {
        this.materialIntercambiado = materialIntercambiado;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioAcordado = precioAcordado;
        this.fechaRecogida = fechaRecogida;
        this.estado = estado;
        this.notas = notas;
        this.fechaRegistro = fechaRegistro;
        this.empresaOrigen = empresaOrigen;
        this.empresaDestino = empresaDestino;
        this.oferta = oferta;
        this.demanda = demanda;
        this.beneficioPlataforma = beneficioPlataforma;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialIntercambiado() {
        return materialIntercambiado;
    }

    public void setMaterialIntercambiado(String materialIntercambiado) {
        this.materialIntercambiado = materialIntercambiado;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getPrecioAcordado() {
        return precioAcordado;
    }

    public void setPrecioAcordado(Double precioAcordado) {
        this.precioAcordado = precioAcordado;
    }

    public Double getBeneficioPlataforma() {
        return beneficioPlataforma;
    }

    public void setBeneficioPlataforma(Double beneficioPlataforma) {
        this.beneficioPlataforma = beneficioPlataforma;
    }

    public Double getImpactoCO2() {
        return impactoCO2;
    }

    public void setImpactoCO2(Double impactoCO2) {
        this.impactoCO2 = impactoCO2;
    }

    public String getBeneficioPlataformaFormateado() {
        if (this.beneficioPlataforma == null) return "0,00 €";
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
        return formatoMoneda.format(this.beneficioPlataforma);
    }

    public LocalDate getFechaRecogida() {
        return fechaRecogida;
    }

    public void setFechaRecogida(LocalDate fechaRecogida) {
        this.fechaRecogida = fechaRecogida;
    }

    public EstadoAcuerdo getEstado() {
        return estado;
    }

    public void setEstado(EstadoAcuerdo estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Empresa getEmpresaOrigen() {
        return empresaOrigen;
    }

    public void setEmpresaOrigen(Empresa empresaOrigen) {
        this.empresaOrigen = empresaOrigen;
    }

    public Empresa getEmpresaDestino() {
        return empresaDestino;
    }

    public void setEmpresaDestino(Empresa empresaDestino) {
        this.empresaDestino = empresaDestino;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public void setOferta(Oferta oferta) {
        this.oferta = oferta;
    }

    public Demanda getDemanda() {
        return demanda;
    }

    public void setDemanda(Demanda demanda) {
        this.demanda = demanda;
    }

    public String getPrecioAcordadoFormateado() {
        if (this.precioAcordado == null) return "0,00 €";
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
        return formatoMoneda.format(this.precioAcordado);
    }

    public String getFechaRegistroFormateada() {
        if (this.fechaRegistro == null) return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.fechaRegistro);
    }
}
