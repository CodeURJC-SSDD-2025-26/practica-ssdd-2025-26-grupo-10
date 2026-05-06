package es.urjc.ecomostoles.backend.client;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "utility-service", url = "${utility.service.url}", fallback = UtilityClientFallback.class)
public interface UtilityClient {

    @PostMapping("/api/v1/pdfs/certificate")
    ResponseEntity<byte[]> generateCertificate(@RequestBody AgreementDTO agreement);
}
