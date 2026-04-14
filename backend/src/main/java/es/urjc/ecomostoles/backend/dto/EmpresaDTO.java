package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Empresa;

/**
 * Data Transfer Object (DTO) for Empresa.
 * Used to avoid exposing sensitive information like password hashes to the view layer.
 */
public class EmpresaDTO {

    private final Long id;
    private final String nombreComercial;
    private final String emailContacto;
    private final String cif;
    private final String direccion;
    private final String telefono;
    private final String sectorIndustrial;
    private final String descripcion;
    private final String rol;
    private Double co2Ahorrado = 0.0;

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

    public Long getId() {
        return id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public String getCif() {
        return cif;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getSectorIndustrial() {
        return sectorIndustrial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getRol() {
        return rol;
    }

    public Double getCo2Ahorrado() {
        return co2Ahorrado;
    }

    public void setCo2Ahorrado(Double co2Ahorrado) {
        this.co2Ahorrado = co2Ahorrado;
    }
}