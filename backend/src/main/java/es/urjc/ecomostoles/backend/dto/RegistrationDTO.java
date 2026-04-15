package es.urjc.ecomostoles.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object for company registration.
 * Includes Jakarta Bean Validation annotations.
 */
public class RegistrationDTO {

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String commercialName;

    @NotBlank(message = "El CIF es obligatorio")
    private String taxId;

    @NotBlank(message = "La ubicación es obligatoria")
    private String address;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String sector;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "La descripción de la empresa es obligatoria")
    private String description;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String contactEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmPassword;

    private MultipartFile logoFile;

    private String otherAddress;

    public RegistrationDTO() {
    }

    // Method for cross-field validation
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null)
            return true; // Let @NotBlank handle nulls
        return password.equals(confirmPassword);
    }

    // Getters and Setters
    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public MultipartFile getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(MultipartFile logoFile) {
        this.logoFile = logoFile;
    }

    public String getOtherAddress() {
        return otherAddress;
    }

    public void setOtherAddress(String otherAddress) {
        this.otherAddress = otherAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
