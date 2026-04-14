package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Empresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for Empresa.
 * Used for form binding and to avoid exposing sensitive information like password hashes.
 */
public class EmpresaDTO {

    private Long id;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;

    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String emailContacto;

    @NotBlank(message = "El CIF es obligatorio")
    private String cif;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private String telefono;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String sectorIndustrial;

    private String descripcion;
    private String rol;
    private Double co2Ahorrado = 0.0;
    private Integer ranking;

    // Default constructor for Spring binding/Mustache
    public EmpresaDTO() {
    }

    public EmpresaDTO(Empresa empresa) {
        this.id = empresa.getId();
        this.nombreComercial = empresa.getNombreComercial();
        this.emailContacto = empresa.getEmailContacto();
        this.cif = empresa.getCif();
        this.direccion = empresa.getDireccion();
        this.telefono = empresa.getTelefono();
        this.sectorIndustrial = empresa.getSectorIndustrial();
        this.descripcion = empresa.getDescripcion();

        if (empresa.getRoles() != null && !empresa.getRoles().isEmpty()) {
            this.rol = empresa.getRoles().get(0);
        } else {
            this.rol = "USER";
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSectorIndustrial() {
        return sectorIndustrial;
    }

    public void setSectorIndustrial(String sectorIndustrial) {
        this.sectorIndustrial = sectorIndustrial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Double getCo2Ahorrado() {
        return co2Ahorrado;
    }

    public void setCo2Ahorrado(Double co2Ahorrado) {
        this.co2Ahorrado = co2Ahorrado;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }
}