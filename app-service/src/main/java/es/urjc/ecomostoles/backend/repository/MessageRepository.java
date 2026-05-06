package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT m FROM Message m WHERE m.recipient = :company OR m.sender = :company ORDER BY m.sendDate DESC")
    Page<Message> findByCompanyPaginated(@Param("company") Company company, Pageable pageable);

    @Query("SELECT m FROM Message m ORDER BY m.sendDate DESC")
    Page<Message> findAllPaginated(Pageable pageable);
}
