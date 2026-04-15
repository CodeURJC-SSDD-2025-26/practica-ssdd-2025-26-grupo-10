package es.urjc.ecomostoles.backend.config;

import es.urjc.ecomostoles.backend.model.*;
import es.urjc.ecomostoles.backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Single data initializer for the application.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CompanyRepository companyRepository;
    private final OfferRepository offerRepository;
    private final DemandRepository demandRepository;
    private final AgreementRepository agreementRepository;
    private final ImpactFactorRepository impactFactorRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;
    private final es.urjc.ecomostoles.backend.component.SustainabilityEngine sustainabilityEngine;

    public DataInitializer(CompanyRepository companyRepository,
            OfferRepository offerRepository,
            DemandRepository demandRepository,
            AgreementRepository agreementRepository,
            ImpactFactorRepository impactFactorRepository,
            MessageRepository messageRepository,
            PasswordEncoder passwordEncoder,
            es.urjc.ecomostoles.backend.component.SustainabilityEngine sustainabilityEngine) {
        this.companyRepository = companyRepository;
        this.offerRepository = offerRepository;
        this.demandRepository = demandRepository;
        this.agreementRepository = agreementRepository;
        this.impactFactorRepository = impactFactorRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
        this.sustainabilityEngine = sustainabilityEngine;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reads a file from static/img/ and returns its bytes (StreamUtils = Spring)
    // ─────────────────────────────────────────────────────────────────────────
    private byte[] loadImage(String fileName) {
        String path = "static/img/" + fileName;
        try {
            ClassPathResource res = new ClassPathResource(path);
            if (!res.exists()) {
                log.warn("⚠️  Image not found: {}", path);
                return null;
            }
            try (InputStream is = res.getInputStream()) {
                byte[] bytes = StreamUtils.copyToByteArray(is);
                log.info("   📷 '{}' → {} bytes", fileName, bytes.length);
                return bytes;
            }
        } catch (IOException e) {
            log.error("❌ Error reading '{}': {}", path, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Creates or updates a company with the given information
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates or updates a company searching BY tax ID (unique field),
     * not by email, to avoid DataIntegrityViolationException when
     * the DB already has a record with that tax ID from a previous run.
     */
    private Company upsertCompany(String email, String name, String taxId,
            String sector, String address, String phone,
            String description, String logoFile,
            List<String> roles) {
        // ← Search by tax ID (real unique key), not by email
        Optional<Company> optCompany = companyRepository.findByTaxId(taxId);
        Company company = optCompany.orElseGet(Company::new);

        company.setContactEmail(email);
        company.setCommercialName(name);
        company.setTaxId(taxId);
        company.setIndustrialSector(sector);
        company.setAddress(address);
        company.setPhone(phone);
        company.setDescription(description);
        company.setPassword(passwordEncoder.encode("1234"));
        company.setRoles(roles);

        if (company.getLogo() == null || company.getLogo().length == 0) {
            company.setLogo(loadImage(logoFile));
        }

        return companyRepository.save(company);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("════════════════════════════════════════════");
        log.info("  DataInitializer — Practice 2 SSDD");
        log.info("════════════════════════════════════════════");

        // ══════════════════════════════════════════
        // 0. Environmental Impact Factors (Sustainability)
        // ══════════════════════════════════════════
        if (impactFactorRepository.count() == 0) {
            log.info("🌱 Seeding environmental impact factors...");
            impactFactorRepository.save(new ImpactFactor("plastic", 1.5));
            impactFactorRepository.save(new ImpactFactor("metal", 2.0));
            impactFactorRepository.save(new ImpactFactor("aluminum", 2.0));
            impactFactorRepository.save(new ImpactFactor("steel", 2.0));
            impactFactorRepository.save(new ImpactFactor("wood", 0.8));
            impactFactorRepository.save(new ImpactFactor("glass", 1.2));
            impactFactorRepository.save(new ImpactFactor("paper/cardboard", 1.1));
            log.info("✅ {} factors created.", impactFactorRepository.count());
        }

        // ══════════════════════════════════════════
        // 1. System ADMIN
        // ══════════════════════════════════════════
        Company admin = upsertCompany(
                "admin@ecomostoles.es",
                "Administración EcoMóstoles",
                "A00000000",
                "Administración",
                "Sede Central — Móstoles",
                "916 000 000",
                "Cuenta de administración de la plataforma EcoMóstoles.",
                "logo.webp",
                List.of("ADMIN") // ← ADMIN role
        );
        log.info("✅ ADMIN created/updated → {} | password: 1234 | roles: {}", admin.getContactEmail(),
                admin.getRoles());

        // ══════════════════════════════════════════
        // 2. Company 1 — Metales del Sur (USER)
        // ══════════════════════════════════════════
        Company company1 = upsertCompany(
                "contacto@metalesdelsur.es",
                "Metales del Sur S.L.",
                "B12345678",
                "Metalurgia",
                "Polígono Industrial Norte, Nave 7 — Móstoles",
                "916 123 456",
                "Gestión y comercialización de residuos metálicos en el área de Móstoles.",
                "logo.webp",
                List.of("COMPANY") // ← USER/COMPANY role
        );
        log.info("✅ Company 1 → {} | roles: {}", company1.getContactEmail(), company1.getRoles());

        // ══════════════════════════════════════════
        // 3. Company 2 — EcoSur Reciclajes (USER)
        // ══════════════════════════════════════════
        Company company2 = upsertCompany(
                "reciclajes@ecosur.es",
                "EcoSur Reciclajes S.A.",
                "B87654321",
                "Reciclaje y Medio Ambiente",
                "Polígono Las Nieves, Nave 12 — Móstoles",
                "916 654 321",
                "Centro de reciclaje especializado en plásticos y metales no ferrosos.",
                "logo.webp",
                List.of("COMPANY"));
        log.info("✅ Company 2 → {} | roles: {}", company2.getContactEmail(), company2.getRoles());

        // ══════════════════════════════════════════
        // 4. Test offers with BLOB images
        // ══════════════════════════════════════════
        List<Offer> offersCompany1 = offerRepository.findByCompany(company1, Offer.class);

        if (offersCompany1.isEmpty()) {
            createOffer(company1,
                    "Virutas de Acero Inoxidable",
                    "Virutas limpias de acero inoxidable 316L libres de aceite. Listas para fundición.",
                    WasteCategory.METAL_WASTE, 500.0, "kg", 0.45, "Inmediata",
                    LocalDateTime.now().minusDays(5), "virutas.webp");

            createOffer(company1,
                    "Bobinas de Cobre Recuperado",
                    "Bobinas de cobre de alta pureza (99.2%) recuperadas de transformadores.",
                    WasteCategory.METAL_WASTE, 120.0, "kg", 4.80, "Esta semana",
                    LocalDateTime.now().minusDays(2), "bobinas-cobre.webp");

            log.info("✅ 2 offers created for Company 1.");
        } else {
            // Repair empty images if they exist
            offersCompany1.stream()
                    .filter(o -> o.getImage() == null || o.getImage().length == 0)
                    .forEach(o -> {
                        o.setImage(loadImage("virutas.webp"));
                        offerRepository.save(o);
                        log.info("   🔄 Image repaired in: {}", o.getTitle());
                    });
        }

        List<Offer> offersCompany2 = offerRepository.findByCompany(company2, Offer.class);
        if (offersCompany2.isEmpty()) {
            createOffer(company2,
                    "Retales de PVC Industrial",
                    "Recortes de PVC rígido (2-10mm). Ideales para reciclaje en piezas pequeñas.",
                    WasteCategory.PLASTIC_WASTE, 80.0, "kg", 0.20, "Consultar",
                    LocalDateTime.now().minusDays(1), "retales-pvc.webp");

            log.info("✅ 1 offer created for Company 2.");
        }

        // ══════════════════════════════════════════
        // 5. Test Demands and Agreements
        // ══════════════════════════════════════════
        if (demandRepository.count() == 0) {
            // Demand 1: from company1 (Metalurgia) — visible to other Metalurgia companies
            // via Smart Matching
            Demand demand1 = new Demand();
            demand1.setTitle("Se necesita Aluminio para inyección");
            demand1.setDescription("Requerimos aluminio puro para moldes.");
            demand1.setWasteCategory(WasteCategory.METAL_WASTE);
            demand1.setQuantity(200.0);
            demand1.setUnit("kg");
            demand1.setMaxBudget(1.50);
            demand1.setUrgency("Inmediata");
            demand1.setStatus(DemandStatus.ACTIVE);
            demand1.setPublicationDate(LocalDateTime.now().minusDays(1));
            demand1.setValidity("30");
            demand1.setPickupZone("Polígono Industrial Sur — Móstoles");
            demand1.setCompany(company1);
            demandRepository.save(demand1);

            // Demand 2: from company (Reciclaje y Medio Ambiente)
            Demand demand2 = new Demand();
            demand2.setTitle("Plásticos Mixtos para procesar");
            demand2.setDescription("Recortes de plásticos mixtos listos para reprocesado industrial.");
            demand2.setWasteCategory(WasteCategory.PLASTIC_WASTE);
            demand2.setQuantity(150.0);
            demand2.setUnit("kg");
            demand2.setMaxBudget(2.0);
            demand2.setUrgency("Consultar");
            demand2.setStatus(DemandStatus.ACTIVE);
            demand2.setPublicationDate(LocalDateTime.now());
            demand2.setValidity("0");
            demand2.setPickupZone("Móstoles Central");
            demand2.setCompany(company2);
            demandRepository.save(demand2);
        }

        // ══════════════════════════════════════════
        // 6. Company 3 — Reciclajes Paco (same sector as company1 for Smart Matching)
        // ══════════════════════════════════════════
        Company company3 = upsertCompany(
                "paco@reciclajes.es",
                "Reciclajes Paco S.L.",
                "B77777777",
                "Metalurgia",
                "Polígono Industrial, Nave 44 — Móstoles",
                "916 777 777",
                "Specialists in non-ferrous metal recycling.",
                "logo.webp",
                List.of("COMPANY"));
        log.info("✅ Company 3 → {} | roles: {}", company3.getContactEmail(), company3.getRoles());

        if (demandRepository.findByCompany(company3).isEmpty()) {
            Demand pacoDemand = new Demand();
            pacoDemand.setTitle("Virutas de Acero");
            pacoDemand.setDescription("Buscamos virutas de acero limpias para reciclado industrial.");
            pacoDemand.setWasteCategory(WasteCategory.METAL_WASTE);
            pacoDemand.setQuantity(1500.0);
            pacoDemand.setUnit("kg");
            pacoDemand.setMaxBudget(1.20);
            pacoDemand.setUrgency("En 1 semana");
            pacoDemand.setStatus(DemandStatus.ACTIVE);
            pacoDemand.setPublicationDate(LocalDateTime.now());
            pacoDemand.setValidity("15");
            pacoDemand.setPickupZone("Sector IV — Móstoles");
            pacoDemand.setCompany(company3);
            demandRepository.save(pacoDemand);
            log.info("✅ Demand created for Company 3 (Paco).");
        }

        if (agreementRepository.count() == 0) {
            Agreement agreement1 = new Agreement();
            agreement1.setExchangedMaterial("Viruta de Acero Inoxidable");
            agreement1.setQuantity(500.0);
            agreement1.setUnit("kg");
            agreement1.setAgreedPrice(1200.0);
            agreement1.setStatus(AgreementStatus.COMPLETED);
            agreement1.setRegistrationDate(LocalDateTime.now().minusDays(2));
            agreement1.setPickupDate(java.time.LocalDate.now().minusDays(1));
            agreement1.setOriginCompany(company1);
            agreement1.setDestinationCompany(company2);
            // Calculate platform commission (2.5% default)
            agreement1.setPlatformCommission(Math.round(1200.0 * 0.025 * 100.0) / 100.0);

            // Calculate initial CO2 impact (Steel/Metal ~ 2.0)
            agreement1.setCo2Impact(
                    sustainabilityEngine.calculateCo2Impact(agreement1.getQuantity(), WasteCategory.METAL_WASTE));

            agreementRepository.save(agreement1);
        }

        // ══════════════════════════════════════════
        // 7. Bulk Data Seeding for "Metales del Sur S.L." (Pagination Testing)
        // ══════════════════════════════════════════
        if (offerRepository.countByCompany(company1) < 5) {
            log.info("📊 Injecting bulk sample data for Metales del Sur S.L. (Pagination testing)...");

            String[] metals = { "Cobre", "Aluminio", "Acero Corten", "Latón", "Plomo", "Zinc", "Retales de Hierro",
                    "Virutas de Bronce" };
            String[] availabilities = { "Inmediata", "En 1 semana", "Consultar" };
            String[] images = { "virutas.webp", "bobinas-cobre.webp", "retales-pvc.webp" };

            java.util.Random random = new java.util.Random();

            for (int i = 1; i <= 10; i++) {
                String material = metals[random.nextInt(metals.length)];
                double qty = 50 + random.nextInt(1951);
                double price = 0.10 + (14.90 * random.nextDouble());
                String availability = availabilities[random.nextInt(availabilities.length)];
                String image = images[random.nextInt(images.length)];
                OfferStatus status = (i <= 7) ? OfferStatus.ACTIVE : OfferStatus.PAUSED;

                Offer bulkOffer = new Offer();
                bulkOffer.setTitle("Lote de " + material + " (" + i + ")");
                bulkOffer.setDescription("Suministro industrial de " + material.toLowerCase()
                        + " reciclado de alta calidad, procesado mecánicamente.");
                bulkOffer.setWasteCategory(WasteCategory.METAL_WASTE);
                bulkOffer.setQuantity(qty);
                bulkOffer.setUnit("kg");
                bulkOffer.setPrice(Math.round(price * 100.0) / 100.0);
                bulkOffer.setAvailability(availability);
                bulkOffer.setStatus(status);
                bulkOffer.setPublicationDate(LocalDateTime.now().minusHours(i));
                bulkOffer.setCompany(company1);
                bulkOffer.setImage(loadImage(image));
                offerRepository.save(bulkOffer);
            }

            for (int i = 1; i <= 10; i++) {
                String material = metals[random.nextInt(metals.length)];
                double qty = 100 + random.nextInt(2001);
                double budget = 0.50 + (10.0 * random.nextDouble());
                String urgency = availabilities[random.nextInt(availabilities.length)];

                Demand bulkDemand = new Demand();
                bulkDemand.setTitle("Buscamos " + material + " para fundir (" + i + ")");
                bulkDemand.setDescription("Necesitamos suministro recurrente de " + material.toLowerCase()
                        + " para nuestros nuevos procesos de fabricación circular.");
                bulkDemand.setWasteCategory(WasteCategory.METAL_WASTE);
                bulkDemand.setQuantity(qty);
                bulkDemand.setUnit("kg");
                bulkDemand.setMaxBudget(Math.round(budget * 100.0) / 100.0);
                bulkDemand.setUrgency(urgency);
                bulkDemand.setStatus(DemandStatus.ACTIVE);
                bulkDemand.setPublicationDate(LocalDateTime.now().minusHours(i));
                bulkDemand.setValidity("30");
                bulkDemand.setPickupZone("Polígono Norte — Móstoles");
                bulkDemand.setCompany(company1);
                demandRepository.save(bulkDemand);
            }
            log.info("✅ 20 bulk records injected for Metales del Sur S.L.");
        }

        // ══════════════════════════════════════════
        // 8. Bulk Agreement Seeding for "Metales del Sur S.L." (Pagination Testing)
        // ══════════════════════════════════════════
        if (agreementRepository.count() < 10) {
            log.info("🤝 Injecting 20 bulk agreements for Metales del Sur S.L. (Pagination testing)...");
            java.util.Random random = new java.util.Random();
            AgreementStatus[] statuses = {AgreementStatus.PENDING, AgreementStatus.ACCEPTED, AgreementStatus.COMPLETED};
            String[] materials = {"Cobre Reciclado", "Virutas de Aluminio", "Acero Inoxidable", "Plástico Triturado"};
            
            for (int i = 1; i <= 20; i++) {
                Agreement bulkAgreement = new Agreement();
                bulkAgreement.setExchangedMaterial(materials[random.nextInt(materials.length)] + " (" + i + ")");
                bulkAgreement.setQuantity(100.0 + random.nextInt(900));
                bulkAgreement.setUnit("kg");
                
                double price = 200.0 + random.nextInt(3000);
                bulkAgreement.setAgreedPrice(price);
                
                // Status alternation/random
                bulkAgreement.setStatus(statuses[random.nextInt(statuses.length)]);
                
                bulkAgreement.setRegistrationDate(LocalDateTime.now().minusDays(i));
                bulkAgreement.setPickupDate(java.time.LocalDate.now().plusDays(random.nextInt(10)));
                
                bulkAgreement.setOriginCompany(company1);
                bulkAgreement.setDestinationCompany(company3);
                
                // Commission (2.5%)
                bulkAgreement.setPlatformCommission(Math.round(price * 0.025 * 100.0) / 100.0);
                
                // CO2 impact
                bulkAgreement.setCo2Impact(sustainabilityEngine.calculateCo2Impact(bulkAgreement.getQuantity(), WasteCategory.METAL_WASTE));
                
                agreementRepository.save(bulkAgreement);
            }
            log.info("✅ 20 bulk agreements injected.");
        }

        log.info("════════════════════════════════════════════");
        // ══════════════════════════════════════════
        // 9. Sample Message Seeding (Mailbox Testing)
        // ══════════════════════════════════════════
        if (messageRepository.count() == 0) {
            log.info("📧 Seeding sample messages for mailbox UI testing...");

            // Message 1: EcoSur -> Metales del Sur (Read)
            messageRepository.save(new Message(
                "Consulta sobre virutas",
                "Hola, ¿seguís teniendo stock de las virutas de acero inox? Necesitamos 300kg.",
                LocalDateTime.now().minusDays(2),
                true,
                company2,
                company1
            ));

            // Message 2: EcoSur -> Metales del Sur (Unread)
            messageRepository.save(new Message(
                "Factura pendiente",
                "Te adjunto la factura proforma del último acuerdo. Confírmame recepción.",
                LocalDateTime.now().minusHours(5),
                false,
                company2,
                company1
            ));

            // Message 3: Paco -> Metales del Sur (Unread)
            messageRepository.save(new Message(
                "¡Oportunidad de Aluminio!",
                "Paco aquí. Acabamos de recibir un lote de aluminio que te puede interesar.",
                LocalDateTime.now().minusMinutes(30),
                false,
                company3,
                company1
            ));

            // Message 4: Metales del Sur -> Paco (Sent)
            messageRepository.save(new Message(
                "RE: Oportunidad de Aluminio",
                "Hola Paco, pásame fotos del material y lo cerramos mañana.",
                LocalDateTime.now().minusMinutes(10),
                true,
                company1,
                company3
            ));

            // Message 5: Paco -> Metales del Sur (Read)
            messageRepository.save(new Message(
                "Horario de recogida",
                "Nuestros camiones pasarán el jueves a las 09:00h. Saludos.",
                LocalDateTime.now().minusDays(1),
                true,
                company3,
                company1
            ));

            log.info("✅ 5 sample messages injected.");
        }

        log.info("  Sample credentials (password: 1234 for all):");
        log.info("  ADMIN  → admin@ecomostoles.es");
        log.info("  USER 1 → contacto@metalesdelsur.es");
        log.info("  USER 2 → reciclajes@ecosur.es");
        log.info("  USER 3 → paco@reciclajes.es");
        log.info("════════════════════════════════════════════");
    }

    private void createOffer(Company company, String title, String description,
            WasteCategory wasteCategory, Double quantity, String unit,
            Double price, String availability,
            LocalDateTime date, String imageFile) {
        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setWasteCategory(wasteCategory);
        offer.setQuantity(quantity);
        offer.setUnit(unit);
        offer.setPrice(price);
        offer.setAvailability(availability);
        offer.setStatus(OfferStatus.ACTIVE);
        offer.setPublicationDate(date);
        offer.setCompany(company);
        offer.setImage(loadImage(imageFile));
        offerRepository.save(offer);
    }
}
