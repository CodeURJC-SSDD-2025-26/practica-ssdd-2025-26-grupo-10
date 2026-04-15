package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.*;

/**
 * Entity representing environmental impact factors for different waste categories.
 * Allows dynamic management of CO2 multipliers without code redeployment.
 */
@Entity
public class FactorImpacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String categoria;

    private double multiplicador;

    public FactorImpacto() {
    }

    public FactorImpacto(String categoria, double multiplicador) {
        this.categoria = categoria;
        this.multiplicador = multiplicador;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getMultiplicador() {
        return multiplicador;
    }

    public void setMultiplicador(double multiplicador) {
        this.multiplicador = multiplicador;
    }
}
