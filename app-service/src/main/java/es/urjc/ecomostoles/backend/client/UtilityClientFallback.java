package es.urjc.ecomostoles.backend.client;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UtilityClientFallback implements UtilityClient {

    @Override
    public ResponseEntity<byte[]> generateCertificate(AgreementDTO agreement) {
        // Fallback: If utility-service is down or slow, 
        // prevent cascading failure by returning a controlled response.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new byte[0]);
    }
}
