package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;

    @NotBlank(message = "El CIF es obligatorio")
    @Column(unique = true)
    private String cif;

    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true)
    private String emailContacto;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private String telefono;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String sectorIndustrial;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logo;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    // Default constructor
    public Empresa() {
    }

    // Constructor with parameters
    public Empresa(String nombreComercial, String cif, String emailContacto, String password, String direccion,
                   String telefono, String sectorIndustrial, String descripcion, byte[] logo, List<String> roles) {
        this.nombreComercial = nombreComercial;
        this.cif = cif;
        this.emailContacto = emailContacto;
        this.password = password;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sectorIndustrial = sectorIndustrial;
        this.descripcion = descripcion;
        this.logo = logo;
        this.roles = roles;
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

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
