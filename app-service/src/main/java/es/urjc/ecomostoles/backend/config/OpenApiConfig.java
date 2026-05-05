package es.urjc.ecomostoles.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Global OpenAPI / Swagger UI configuration for the EcoMóstoles B2B REST API.
 *
 * <p>Exposes interactive documentation at:
 * <ul>
 *   <li>Swagger UI  → {@code /swagger-ui.html}</li>
 *   <li>OpenAPI JSON → {@code /v3/api-docs}</li>
 * </ul>
 *
 * <p>Only controllers annotated with {@code @RestController} inside the
 * {@code es.urjc.ecomostoles.backend.controller.api} package are scanned
 * (configured via {@code springdoc.packagesToScan} in application.properties).
 * Classic MVC controllers that serve Mustache views are intentionally excluded
 * so they do not appear in the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8443}")
    private String serverPort;

    /**
     * Defines the global OpenAPI metadata bean rendered by Swagger UI.
     *
     * @return a fully-configured {@link OpenAPI} instance.
     */
    @Bean
    public OpenAPI ecoMostolesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EcoMóstoles B2B API")
                        .version("v1.0")
                        .description("""
                                API REST para la plataforma de intercambio de materiales sostenibles EcoMóstoles.
                                
                                Permite a empresas gestionar ofertas de materiales residuales, solicitudes de \
                                demanda, acuerdos comerciales y mensajes B2B a través de una interfaz RESTful \
                                estandarizada.
                                
                                **Autenticación:** HTTP Basic (mismas credenciales que la aplicación web).
                                
                                **Formato:** JSON en todos los endpoints.
                                """)
                        .contact(new Contact()
                                .name("EcoMóstoles Platform Team")
                                .email("soporte@ecomostoles.es")
                                .url("https://ecomostoles.es"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://localhost:" + serverPort)
                                .description("Servidor de desarrollo local (HTTPS)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local (HTTP — sin SSL)")
                ));
    }
}
