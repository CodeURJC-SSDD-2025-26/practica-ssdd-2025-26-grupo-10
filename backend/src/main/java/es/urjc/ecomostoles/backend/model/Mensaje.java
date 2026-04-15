package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asunto;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String contenido;

    private LocalDateTime fechaEnvio;

    private boolean leido;

    @ManyToOne
    private Empresa remitente;

    @ManyToOne
    private Empresa destinatario;

    public Mensaje() {}

    public Mensaje(String asunto, String contenido, LocalDateTime fechaEnvio, boolean leido, Empresa remitente, Empresa destinatario) {
        this.asunto = asunto;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
        this.remitente = remitente;
        this.destinatario = destinatario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public Empresa getRemitente() {
        return remitente;
    }

    public void setRemitente(Empresa remitente) {
        this.remitente = remitente;
    }

    public Empresa getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Empresa destinatario) {
        this.destinatario = destinatario;
    }

    public String getFechaEnvioFormateada() {
        if (this.fechaEnvio == null) return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.fechaEnvio);
    }
}
