package es.urjc.ecomostoles.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El asunto no puede estar vacío")
    private String subject;

    @NotBlank(message = "El contenido no puede estar vacío")
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sendDate;

    private boolean read;

    @ManyToOne
    private Company sender;

    @ManyToOne
    private Company recipient;

    public Message() {
    }

    public Message(String subject, String content, LocalDateTime sendDate, boolean read, Company sender,
            Company recipient) {
        this.subject = subject;
        this.content = content;
        this.sendDate = sendDate;
        this.read = read;
        this.sender = sender;
        this.recipient = recipient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Company getSender() {
        return sender;
    }

    public void setSender(Company sender) {
        this.sender = sender;
    }

    public Company getRecipient() {
        return recipient;
    }

    public void setRecipient(Company recipient) {
        this.recipient = recipient;
    }

    public String getFormattedSendDate() {
        if (this.sendDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.sendDate);
    }
}
