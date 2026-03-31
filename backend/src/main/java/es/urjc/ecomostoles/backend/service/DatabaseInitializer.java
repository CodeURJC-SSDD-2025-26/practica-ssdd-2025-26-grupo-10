package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to initialize the database with sample data at application startup.
 */
@Service
public class DatabaseInitializer {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;
    private final DemandaRepository demandaRepository;
    private final AcuerdoRepository acuerdoRepository;

    /**
     * Constructor-based dependency injection for all repositories.
     *
     * @param empresaRepository the company repository
     * @param ofertaRepository  the offer repository
     * @param demandaRepository the demand repository
     * @param acuerdoRepository the agreement repository
     */
    public DatabaseInitializer(EmpresaRepository empresaRepository,
                               OfertaRepository ofertaRepository,
                               DemandaRepository demandaRepository,
                               AcuerdoRepository acuerdoRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
        this.demandaRepository = demandaRepository;
        this.acuerdoRepository = acuerdoRepository;
    }

    /**
     * Initializes the database if it is empty.
     * This method is executed after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        // Check if the database is already populated
        if (empresaRepository.findAll().isEmpty()) {

            // 1. EcoMóstoles Admin
            Empresa admin = new Empresa(
                "EcoMóstoles Admin",
                "A00000000",
                "admin@ecomostoles.com",
                "admin",
                "Calle Central 1, Móstoles",
                "900111222",
                "Administration",
                "Main platform administrator for oversight and management.",
                new byte[0],
                List.of("ADMIN", "USER")
            );

            // 2. Metales del Sur S.L.
            Empresa empresa1 = new Empresa(
                "Metales del Sur S.L.",
                "B12345678",
                "contacto@metalesdelsur.es",
                "pass",
                "Polígono Regordoño, Calle Industria 14",
                "912345678",
                "Metalurgia",
                "Leading company in metallurgy and metal waste recycling.",
                new byte[0],
                List.of("USER")
            );

            // 3. Plásticos Norte S.A.
            Empresa empresa2 = new Empresa(
                "Plásticos Norte S.A.",
                "C87654321",
                "info@plasticosnorte.com",
                "pass",
                "Polígono Arroyomolinos, Calle Siderurgia 5",
                "918765432",
                "Reciclaje de Plásticos",
                "Experts in polymer transformation and industrial recycling.",
                new byte[0],
                List.of("USER")
            );

            // Save initial companies
            empresaRepository.save(admin);
            empresaRepository.save(empresa1);
            empresaRepository.save(empresa2);

            // 4. Create and save 2 Offers for Empresa 1 (Metales del Sur)
            Oferta oferta1 = new Oferta(
                "Recortes de Aluminio de alta pureza",
                "Sobrantes de producción de perfiles de aluminio 6063. Ideal para reciclaje primario.",
                "Residuo Metálico",
                250.0,
                "kg",
                450.0,
                "Bajo pedido",
                "Activa",
                LocalDateTime.now(),
                new byte[0],
                empresa1
            );

            Oferta oferta2 = new Oferta(
                "Vigas de Acero recuperadas",
                "Vigas HEB de desmantelamiento industrial en buen estado. Longitudes de 3 a 5 metros.",
                "Residuo Metálico",
                1500.0,
                "kg",
                1200.0,
                "Inmediata",
                "Activa",
                LocalDateTime.now(),
                new byte[0],
                empresa1
            );

            ofertaRepository.save(oferta1);
            ofertaRepository.save(oferta2);

            // 5. Create and save 2 Demands for Empresa 2 (Plásticos Norte)
            Demanda demanda1 = new Demanda(
                "Necesidad de Palés Europeos",
                "Residuo Madera",
                "Se buscan palés europeos (EPAL) usados para rotación interna en almacén.",
                200.0,
                "uds",
                "Alta",
                500.0,
                "Móstoles y alrededores",
                "30 días",
                "Abierta",
                LocalDateTime.now(),
                empresa2
            );

            Demanda demanda2 = new Demanda(
                "Contenedores IBC de 1000L",
                "Residuo Plástico",
                "Necesitamos contenedores IBC lavados para almacenamiento de polímeros líquidos.",
                10.0,
                "uds",
                "Media",
                350.0,
                "Zona Sur de Madrid",
                "15 días",
                "Abierta",
                LocalDateTime.now(),
                empresa2
            );

            demandaRepository.save(demanda1);
            demandaRepository.save(demanda2);

            // 6. Create and save 1 Agreement between Empresa 1 and Empresa 2
            Acuerdo acuerdo = new Acuerdo(
                "Suministro mensual de Scrap Metálico",
                500.0,
                "kg",
                750.0,
                LocalDate.now(),
                "Completado",
                "Acuerdo cerrado para suministro recurrente tras inspección inicial.",
                LocalDateTime.now(),
                empresa1, // Source
                empresa2, // Destination
                oferta1,  // Optional Offer
                null      // Optional Demand (null since it came from an offer)
            );

            acuerdoRepository.save(acuerdo);

            System.out.println("Database initialization completed: 3 companies, 2 offers, 2 demands, and 1 agreement created.");
        }
    }
}
