package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Dynamic parameter dictionary mapping persistent deployment settings.
 * 
 * Enables runtime configuration of core platform logic (structural dropdown options) 
 * without requiring recompilation or deployment cycles. Consumed actively during 
 * bootstrap and UI layout composition.
 */
@Entity
public class GlobalConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String key;

    private String value;

    public GlobalConfiguration() {
    }

    public GlobalConfiguration(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
