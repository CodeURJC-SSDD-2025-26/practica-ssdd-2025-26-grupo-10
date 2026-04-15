package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * Persistence layer for cross-tenant asynchronous communication.
 * 
 * Isolates mailbox streams for individual companies. Supports unread-count aggregation 
 * algorithms powering real-time UI notification badges for the messaging subsystem.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipient(Company recipient);

    List<Message> findBySender(Company sender);

    long countByRecipient(Company recipient);
    
    long countByRecipientAndReadFalse(Company recipient);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.recipient ORDER BY m.sendDate DESC")
    List<Message> findTop100ByOrderBySendDateDesc();
}
