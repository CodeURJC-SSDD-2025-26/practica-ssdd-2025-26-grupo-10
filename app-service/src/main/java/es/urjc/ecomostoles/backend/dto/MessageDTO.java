package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Message;
import java.time.LocalDateTime;

public record MessageDTO(
        Long id,
        String subject,
        String content,
        LocalDateTime sendDate,
        boolean read,
        CompanyDTO sender,
        CompanyDTO recipient
) {
    public String getFormattedSendDate() {
        if (this.sendDate == null)
            return "Fecha no disponible";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(this.sendDate);
    }
    public MessageDTO(Message message) {
        this(
                message.getId(),
                message.getSubject(),
                message.getContent(),
                message.getSendDate(),
                message.isRead(),
                message.getSender() != null ? new CompanyDTO(message.getSender()) : null,
                message.getRecipient() != null ? new CompanyDTO(message.getRecipient()) : null
        );
    }
}
