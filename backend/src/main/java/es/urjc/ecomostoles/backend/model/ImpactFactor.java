package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.*;

/**
 * Entity representing environmental impact factors for different waste
 * categories.
 * Allows dynamic management of CO2 multipliers without code redeployment.
 */
@Entity
public class ImpactFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String category;

    private double multiplier;

    public ImpactFactor() {
    }

    public ImpactFactor(String category, double multiplier) {
        this.category = category;
        this.multiplier = multiplier;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
