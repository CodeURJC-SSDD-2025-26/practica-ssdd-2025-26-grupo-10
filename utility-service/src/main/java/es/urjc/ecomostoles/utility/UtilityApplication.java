package es.urjc.ecomostoles.utility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the utility-service microservice.
 *
 * This service is intentionally lightweight and fully isolated from the
 * persistence layer. Its sole responsibility is to expose auxiliary REST
 * endpoints (e.g., PDF generation) that can be consumed by other services
 * in the EcoMóstoles platform ecosystem.
 *
 * Design decisions:
 *  - No JPA / MySQL: avoids any database coupling.
 *  - No Spring Security: callers are trusted internal services.
 *  - No Mustache: responses are binary (PDF) or JSON payloads.
 */
@SpringBootApplication
public class UtilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityApplication.class, args);
    }
}
