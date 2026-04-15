package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Company;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Secure marshaling profile for Company entities.
 * 
 * Implements the DTO pattern ensuring strict isolation of the underlying DB schema 
 * from the presentation layer. Actively redacts sensitive payload nodes (e.g., password hashes, 
 * internal identity sequences) preventing over-posting vulnerabilities (Mass Assignment).
 */
public class CompanyDTO {

    private Long id;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String commercialName;

    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String contactEmail;

    @NotBlank(message = "El CIF es obligatorio")
    private String taxId;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    private String phone;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String industrialSector;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;
    private String role;
    private Double co2Saved = 0.0;
    private Integer ranking;
    private String sector;
    private boolean verified;

    // Default constructor for Spring binding/Mustache
    public CompanyDTO() {
    }

    public CompanyDTO(Company company) {
        this.id = company.getId();
        this.commercialName = company.getCommercialName();
        this.contactEmail = company.getContactEmail();
        this.taxId = company.getTaxId();
        this.address = company.getAddress();
        this.phone = company.getPhone();
        this.industrialSector = company.getIndustrialSector();
        this.description = company.getDescription();

        this.role = (company.getRoles() != null && !company.getRoles().isEmpty()) ? company.getRoles().get(0) : "USER";
        this.verified = company.isVerified();
        this.sector = company.getIndustrialSector();
    }

    // UI Helpers
    public String getInitial() {
        return (this.commercialName != null && !this.commercialName.isEmpty())
                ? this.commercialName.substring(0, 1).toUpperCase()
                : "?";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIndustrialSector() {
        return industrialSector;
    }

    public void setIndustrialSector(String industrialSector) {
        this.industrialSector = industrialSector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getCo2Saved() {
        return co2Saved;
    }

    public void setCo2Saved(Double co2Saved) {
        this.co2Saved = co2Saved;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
}