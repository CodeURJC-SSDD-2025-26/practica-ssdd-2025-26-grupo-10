package es.urjc.ecomostoles.backend.repository;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByDestinatario(Empresa destinatario);

    long countByDestinatario(Empresa destinatario);

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.remitente JOIN FETCH m.destinatario ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findTop100ByOrderByFechaEnvioDesc();
}
