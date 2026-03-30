package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Represents an Offer (Oferta) in the domain model.
 * Each offer is associated with a specific company (Empresa).
 */
@Entity
public class Oferta {

    /**
     * Unique identifier for the offer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the offer.
     */
    private String titulo;

    /**
     * Detailed description of the offer.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Type of waste or material.
     */
    private String tipoResiduo;

    /**
     * Quantity of the material offered.
     */
    private Double cantidad;

    /**
     * Unit of measurement for the quantity (e.g., kg, ton).
     */
    private String unidad;

    /**
     * Price of the offer.
     */
    private Double precio;

    /**
     * Availability status or details.
     */
    private String disponibilidad;

    /**
     * Current state of the offer (e.g., active, ended).
     */
    private String estado;

    /**
     * Date and time when the offer was published.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Image associated with the offer, stored as a byte array.
     */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imagen;

    /**
     * The company that created this offer.
     */
    @ManyToOne
    private Empresa empresa;

    /**
     * Default constructor required by JPA.
     */
    public Oferta() {
    }

    /**
     * Constructor with all parameters.
     *
     * @param titulo            title of the offer
     * @param descripcion       detailed description
     * @param tipoResiduo       type of waste
     * @param cantidad          quantity offered
     * @param unidad            unit of measurement
     * @param precio            price
     * @param disponibilidad    availability status
     * @param estado            current state
     * @param fechaPublicacion  publication date and time
     * @param imagen            image byte array
     * @param empresa           associated company
     */
    public Oferta(String titulo, String descripcion, String tipoResiduo, Double cantidad, String unidad,
                  Double precio, String disponibilidad, String estado, LocalDateTime fechaPublicacion,
                  byte[] imagen, Empresa empresa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoResiduo = tipoResiduo;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precio = precio;
        this.disponibilidad = disponibilidad;
        this.estado = estado;
        this.fechaPublicacion = fechaPublicacion;
        this.imagen = imagen;
        this.empresa = empresa;
    }

    /** GETTERS AND SETTERS **/

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoResiduo() {
        return tipoResiduo;
    }

    public void setTipoResiduo(String tipoResiduo) {
        this.tipoResiduo = tipoResiduo;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
