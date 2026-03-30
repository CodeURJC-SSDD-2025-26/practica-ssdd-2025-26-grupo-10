package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String materialIntercambiado;

    /**
     * Quantity of the material.
     */
    private Double cantidad;

    /**
     * Unit of measurement (e.g., kg, uds).
     */
    private String unidad;

    /**
     * The price agreed upon for the transaction.
     */
    private Double precioAcordado;

    /**
     * Scheduled date for the material pickup.
     */
    private LocalDate fechaRecogida;

    /**
     * Current status of the agreement (e.g., Pending, Completed).
     */
    private String estado;

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
    @ManyToOne(optional = false)
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
     * Default constructor for JPA.
     */
    public Acuerdo() {
    }

    /**
     * Full constructor for Acuerdo.
     *
     * @param materialIntercambiado material description
     * @param cantidad              quantity
     * @param unidad                measurement unit
     * @param precioAcordado        final price
     * @param fechaRecogida         pickup date
     * @param estado                agreement status
     * @param notas                 extra information
     * @param fechaRegistro         registration timestamp
     * @param empresaOrigen         source company
     * @param empresaDestino        target company
     * @param oferta                associated offer (can be null)
     * @param demanda               associated demand (can be null)
     */
    public Acuerdo(String materialIntercambiado, Double cantidad, String unidad, Double precioAcordado,
                   LocalDate fechaRecogida, String estado, String notas, LocalDateTime fechaRegistro,
                   Empresa empresaOrigen, Empresa empresaDestino, Oferta oferta, Demanda demanda) {
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

    public LocalDate getFechaRecogida() {
        return fechaRecogida;
    }

    public void setFechaRecogida(LocalDate fechaRecogida) {
        this.fechaRecogida = fechaRecogida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
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
}
