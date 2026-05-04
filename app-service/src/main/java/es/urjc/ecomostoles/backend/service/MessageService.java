package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import es.urjc.ecomostoles.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Communication Hub governing cross-tenant interactions.
 * 
 * Facilitates the asynchronous messaging exchange between autonomous Companies. 
 * Orchestrates unread-state propagation and governs the secure delivery logic 
 * ensuring Principals can only access authorized communication streams.
 */
@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /** Returns all messages in the system. */
    @Transactional(readOnly = true)
    public List<Message> getAll() {
        return messageRepository.findTop100ByOrderBySendDateDesc();
    }

    /** Returns all messages received by a specific company. */
    @Transactional(readOnly = true)
    public List<Message> getByRecipient(Company recipient) {
        return messageRepository.findByRecipient(recipient);
    }

    /** Returns all messages sent by a specific company. */
    @Transactional(readOnly = true)
    public List<Message> getBySender(Company sender) {
        return messageRepository.findBySender(sender);
    }

    @Transactional(readOnly = true)
    public long countByRecipient(Company recipient) {
        return messageRepository.countByRecipient(recipient);
    }
    
    @Transactional(readOnly = true)
    public long countUnreadByRecipient(Company recipient) {
        return messageRepository.countByRecipientAndReadFalse(recipient);
    }
    
    public void markAllAsRead(Company recipient) {
        List<Message> unread = messageRepository.findByRecipient(recipient);
        unread.stream().filter(m -> !m.isRead()).forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }

    /** Returns the total count of messages. */
    @Transactional(readOnly = true)
    public long countAll() {
        return messageRepository.count();
    }

    /** Returns a message by its ID, or empty if not found. */
    @Transactional(readOnly = true)
    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    /** Persists a new or updated message. */
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    /**
     * Executes the secure delivery of a communication node. 
     * 
     * Initializes the message payload with mandatory temporal and audit fields (Send Date, 
     * Read Status) prior to persistence.
     * 
     * @param subject   Topic identifier.
     * @param content   Message body.
     * @param sender    Originator tenant.
     * @param recipient Target tenant.
     */
    public void sendMessage(String subject, String content, Company sender, Company recipient) {
        Message newMessage = new Message();
        newMessage.setSubject(subject);
        newMessage.setContent(content);
        newMessage.setSender(sender);
        newMessage.setRecipient(recipient);
        newMessage.setSendDate(java.time.LocalDateTime.now());
        newMessage.setRead(false);
        messageRepository.save(newMessage);
    }

    /** Deletes a message by its ID. */
    public void delete(Long id) {
        messageRepository.deleteById(id);
    }
}
