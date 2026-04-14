package es.urjc.ecomostoles.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to handle email communications.
 * Simulated implementation using logs to avoid external dependencies (SMTP)
 * in development environments, while maintaining professional business logic.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Simulates sending a password recovery email.
     * 
     * @param emailDestino destination email address.
     */
    public void enviarEmailRecuperacion(String emailDestino) {
        log.info("Iniciando proceso de recuperación para: {}", emailDestino);
        // Simulación lógica de generación de token y enlace
        String mockResetLink = "https://ecomostoles.urjc.es/reset?token=a1b2c3d4e5f6";
        
        log.info("Enviando email de recuperación de contraseña a: {} con enlace de reseteo temporal: {}", 
                 emailDestino, mockResetLink);
        
        log.info("Email enviado satisfactoriamente a la cola de mensajería simulada.");
    }
}
