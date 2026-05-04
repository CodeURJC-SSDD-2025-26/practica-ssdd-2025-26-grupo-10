package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;

/**
 * Foundational tenant actor within the security and domain topology.
 * 
 * Represents an authenticated organization. Consolidates multi-directional mapping for Offers, 
 * Demands, Messages, and Agreements. Functions as the root principal for Spring Security RBAC 
 * validations and IDOR protection sweeps.
 */
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String commercialName;

    @NotBlank(message = "El CIF es obligatorio")
    @jakarta.validation.constraints.Pattern(regexp = "^[ABCDEFGHJKLMNPQSVW][0-9]{7}[0-9A-J]$" , message = "El formato del CIF no es válido (ej. A12345678)")
    @Column(unique = true)
    private String taxId;

    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true)
    private String contactEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    @JsonIgnore
    private String password;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "El sector industrial es obligatorio")
    private String industrialSector;

    @NotBlank(message = "La descripción de la empresa es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logo;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    private boolean verified = true;

    // Relationships: Cascade deletion ensures orphan states never pollute the database upon tenant destruction.
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offer> offers = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Demand> demands = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> sentMessages = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> receivedMessages = new ArrayList<>();

    @OneToMany(mappedBy = "originCompany", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agreement> agreementsAsOrigin = new ArrayList<>();

    @OneToMany(mappedBy = "destinationCompany", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agreement> agreementsAsDestination = new ArrayList<>();

    public Company() {
    }

    public Company(String commercialName, String taxId, String contactEmail, String password, String address,
            String phone, String industrialSector, String description, byte[] logo, List<String> roles) {
        this.commercialName = commercialName;
        this.taxId = taxId;
        this.contactEmail = contactEmail;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.industrialSector = industrialSector;
        this.description = description;
        this.logo = logo;
        this.roles = roles;
    }

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

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0);
        }
        return "USER";
    }

    @Transient
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    @Transient
    public boolean isClient() {
        return !isAdmin();
    }

    @Transient
    public String getInitial() {
        if (this.commercialName == null || this.commercialName.isEmpty()) {
            return "?";
        }
        return this.commercialName.substring(0, 1).toUpperCase();
    }
}
