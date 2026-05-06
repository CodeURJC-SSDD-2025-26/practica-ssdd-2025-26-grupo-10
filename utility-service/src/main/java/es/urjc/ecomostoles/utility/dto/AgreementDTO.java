package es.urjc.ecomostoles.utility.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgreementDTO {

    private Long id;

    @JsonAlias({"commercialName", "companyName"})
    private String companyName;

    @JsonAlias({"exchangedMaterial", "wasteCategory"})
    private String wasteCategory;
    
    private Double quantity;
    
    @JsonAlias({"co2Impact", "co2Saved"})
    private Double co2Saved;

    public AgreementDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWasteCategory() {
        return wasteCategory;
    }

    public void setWasteCategory(String wasteCategory) {
        this.wasteCategory = wasteCategory;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getCo2Saved() {
        return co2Saved;
    }

    public void setCo2Saved(Double co2Saved) {
        this.co2Saved = co2Saved;
    }
}
