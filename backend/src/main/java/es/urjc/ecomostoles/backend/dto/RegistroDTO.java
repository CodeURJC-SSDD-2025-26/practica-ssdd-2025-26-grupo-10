package es.urjc.ecomostoles.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object for company registration.
 * Includes Jakarta Bean Validation annotations.
 */
public class RegistroDTO {

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;

    @NotBlank(message = "El CIF es obligatorio")
    private String cif;

    @NotBlank(message = "La ubicación es obligatoria")
    private String direccion;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String sector;
    
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "La descripción de la empresa es obligatoria")
    private String descripcion;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String emailContacto;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmPassword;

    private MultipartFile logoFile;

    private String direccionOtro;

    public RegistroDTO() {}

    // Method for cross-field validation
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null) return true; // Let @NotBlank handle nulls
        return password.equals(confirmPassword);
    }

    // Getters and Setters
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public MultipartFile getLogoFile() { return logoFile; }
    public void setLogoFile(MultipartFile logoFile) { this.logoFile = logoFile; }

    public String getDireccionOtro() { return direccionOtro; }
    public void setDireccionOtro(String direccionOtro) { this.direccionOtro = direccionOtro; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
