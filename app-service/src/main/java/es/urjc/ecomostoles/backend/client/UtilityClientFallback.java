package es.urjc.ecomostoles.backend.client;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UtilityClientFallback implements UtilityClient {

    @Override
    public ResponseEntity<byte[]> generateCertificate(AgreementDTO agreement) {
        // Fallback: Si utility-service cae o responde con lentitud, 
        // evitamos el fallo en cascada devolviendo una respuesta controlada.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new byte[0]);
    }
}
