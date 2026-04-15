package es.urjc.ecomostoles.backend.config;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import es.urjc.ecomostoles.backend.model.EstadoDemanda;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;

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
 *
 * Creates sample data on startup (only when the DB is empty):
 *   - 1 ADMIN account  → admin@ecomostoles.es     / 1234  (BCrypt-encoded)
 *   - 3 company users  → contacto@metalesdelsur.es / 1234  (BCrypt-encoded)
 *                      → reciclajes@ecosur.es       / 1234  (BCrypt-encoded)
 *                      → paco@reciclajes.es          / 1234  (BCrypt-encoded)
 *   - 3 offers with real BLOB images
 *   - 3 demands
 *   - 1 agreement
 *
 * All passwords are hashed via {@link org.springframework.security.crypto.password.PasswordEncoder}.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository  ofertaRepository;
    private final DemandaRepository demandaRepository;
    private final AcuerdoRepository acuerdoRepository;
    private final PasswordEncoder   passwordEncoder;

    public DataInitializer(EmpresaRepository empresaRepository,
                           OfertaRepository ofertaRepository,
                           DemandaRepository demandaRepository,
                           AcuerdoRepository acuerdoRepository,
                           PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository  = ofertaRepository;
        this.demandaRepository = demandaRepository;
        this.acuerdoRepository = acuerdoRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reads a file from static/img/ and returns its bytes (StreamUtils = Spring)
    // ─────────────────────────────────────────────────────────────────────────
    private byte[] cargarImagen(String nombreFichero) {
        String ruta = "static/img/" + nombreFichero;
        try {
            ClassPathResource res = new ClassPathResource(ruta);
            if (!res.exists()) {
                log.warn("⚠️  Image not found: {}", ruta);
                return null;
            }
            try (InputStream is = res.getInputStream()) {
                byte[] bytes = StreamUtils.copyToByteArray(is);
                log.info("   📷 '{}' → {} bytes", nombreFichero, bytes.length);
                return bytes;
            }
        } catch (IOException e) {
            log.error("❌ Error reading '{}': {}", ruta, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Creates or updates a company with the given information
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates or updates a company searching BY CIF (unique field),
     * not by email, to avoid DataIntegrityViolationException when
     * the DB already has a record with that CIF from a previous run.
     */
    private Empresa upsertEmpresa(String email, String nombre, String cif,
                                   String sector, String direccion, String tel,
                                   String descripcion, String logoFichero,
                                   List<String> roles) {
        // ← Search by CIF (real unique key), not by email
        Optional<Empresa> opt = empresaRepository.findByCif(cif);
        Empresa e = opt.orElseGet(Empresa::new);

        e.setEmailContacto(email);
        e.setNombreComercial(nombre);
        e.setCif(cif);
        e.setSectorIndustrial(sector);
        e.setDireccion(direccion);
        e.setTelefono(tel);
        e.setDescripcion(descripcion);
        e.setPassword(passwordEncoder.encode("1234"));
        e.setRoles(roles);

        if (e.getLogo() == null || e.getLogo().length == 0) {
            e.setLogo(cargarImagen(logoFichero));
        }

        return empresaRepository.save(e);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("════════════════════════════════════════════");
        log.info("  DataInitializer — Práctica 2 SSDD");
        log.info("════════════════════════════════════════════");

        // ══════════════════════════════════════════
        // 1. ADMIN del sistema
        // ══════════════════════════════════════════
        Empresa admin = upsertEmpresa(
            "admin@ecomostoles.es",
            "Administración EcoMóstoles",
            "A00000000",
            "Administración",
            "Sede Central — Móstoles",
            "916 000 000",
            "Cuenta de administración de la plataforma EcoMóstoles.",
            "logo.webp",
            List.of("ADMIN")          // ← rol ADMIN
        );
        log.info("✅ ADMIN created/updated → {} | password: 1234 | roles: {}", admin.getEmailContacto(), admin.getRoles());

        // ══════════════════════════════════════════
        // 2. Empresa 1 — Metales del Sur (USER)
        // ══════════════════════════════════════════
        Empresa empresa1 = upsertEmpresa(
            "contacto@metalesdelsur.es",
            "Metales del Sur S.L.",
            "B12345678",
            "Metalurgia",
            "Polígono Industrial Norte, Nave 7 — Móstoles",
            "916 123 456",
            "Gestión y comercialización de residuos metálicos en el área de Móstoles.",
            "logo.webp",
            List.of("EMPRESA")        // ← rol USER/EMPRESA
        );
        log.info("✅ Company 1 → {} | roles: {}", empresa1.getEmailContacto(), empresa1.getRoles());

        // ══════════════════════════════════════════
        // 3. Empresa 2 — EcoSur Reciclajes (USER)
        // ══════════════════════════════════════════
        Empresa empresa2 = upsertEmpresa(
            "reciclajes@ecosur.es",
            "EcoSur Reciclajes S.A.",
            "B87654321",
            "Reciclaje y Medio Ambiente",
            "Polígono Las Nieves, Nave 12 — Móstoles",
            "916 654 321",
            "Centro de reciclaje especializado en plásticos y metales no ferrosos.",
            "logo.webp",
            List.of("EMPRESA")
        );
        log.info("✅ Company 2 → {} | roles: {}", empresa2.getEmailContacto(), empresa2.getRoles());

        // ══════════════════════════════════════════
        // 4. Test offers with BLOB images
        // ══════════════════════════════════════════
        List<Oferta> ofertasEmpresa1 = ofertaRepository.findByEmpresa(empresa1, Oferta.class);

        if (ofertasEmpresa1.isEmpty()) {
            crearOferta(empresa1,
                "Virutas de Acero Inoxidable",
                "Virutas limpias de acero inoxidable 316L libres de aceite. Listas para fundición.",
                "Metal", 500.0, "kg", 0.45, "Inmediata",
                LocalDateTime.now().minusDays(5), "virutas.webp");

            crearOferta(empresa1,
                "Bobinas de Cobre Recuperado",
                "Bobinas de cobre de alta pureza (99.2%) recuperadas de transformadores.",
                "Metal", 120.0, "kg", 4.80, "Esta semana",
                LocalDateTime.now().minusDays(2), "bobinas-cobre.webp");

            log.info("✅ 2 offers created for Company 1.");
        } else {
            // Repair empty images if they exist
            ofertasEmpresa1.stream()
                .filter(o -> o.getImagen() == null || o.getImagen().length == 0)
                .forEach(o -> {
                    o.setImagen(cargarImagen("virutas.webp"));
                    ofertaRepository.save(o);
                    log.info("   🔄 Image repaired in: {}", o.getTitulo());
                });
        }

        List<Oferta> ofertasEmpresa2 = ofertaRepository.findByEmpresa(empresa2, Oferta.class);
        if (ofertasEmpresa2.isEmpty()) {
            crearOferta(empresa2,
                "Retales de PVC Industrial",
                "Recortes de PVC rígido (2-10mm). Ideales para reciclaje en piezas pequeñas.",
                "Plástico", 80.0, "kg", 0.20, "Consultar",
                LocalDateTime.now().minusDays(1), "retales-pvc.webp");

            log.info("✅ 1 offer created for Company 2.");
        }

        // ══════════════════════════════════════════
        // 5. Test Demands and Agreements 
        // ══════════════════════════════════════════
        if (demandaRepository.count() == 0) {
            // Demand 1: from empresa1 (Metalurgia) — visible to other Metalurgia companies via Smart Matching
            Demanda d = new Demanda();
            d.setTitulo("Se necesita Aluminio para inyección");
            d.setDescripcion("Requerimos aluminio puro para moldes.");
            d.setCategoriaMaterial("Metal");
            d.setCantidad(200.0);
            d.setUnidad("kg");
            d.setPresupuestoMaximo(1.50);
            d.setUrgencia("Alta");
            d.setEstado(EstadoDemanda.ACTIVA);
            d.setFechaPublicacion(LocalDateTime.now().minusDays(1));
            d.setEmpresa(empresa1);
            demandaRepository.save(d);

            // Demand 2: from empresa2 (Reciclaje y Medio Ambiente)
            Demanda d2 = new Demanda();
            d2.setTitulo("Plásticos Mixtos para procesar");
            d2.setDescripcion("Recortes de plásticos mixtos listos para reprocesado industrial.");
            d2.setCategoriaMaterial("Plástico");
            d2.setCantidad(150.0);
            d2.setUnidad("kg");
            d2.setPresupuestoMaximo(2.0);
            d2.setEstado(EstadoDemanda.ACTIVA);
            d2.setFechaPublicacion(LocalDateTime.now());
            d2.setEmpresa(empresa2);
            demandaRepository.save(d2);
        }

        // ══════════════════════════════════════════
        // 6. Empresa 3 — Reciclajes Paco (same sector as empresa1 for Smart Matching)
        // ══════════════════════════════════════════
        Empresa empresa3 = upsertEmpresa(
            "paco@reciclajes.es",
            "Reciclajes Paco S.L.",
            "B77777777",
            "Metalurgia",
            "Polígono Industrial, Nave 44 — Móstoles",
            "916 777 777",
            "Specialists in non-ferrous metal recycling.",
            "logo.webp",
            List.of("EMPRESA")
        );
        log.info("✅ Company 3 → {} | roles: {}", empresa3.getEmailContacto(), empresa3.getRoles());

        if (demandaRepository.findByEmpresa(empresa3).isEmpty()) {
            Demanda dPaco = new Demanda();
            dPaco.setTitulo("Virutas de Acero");
            dPaco.setDescripcion("Buscamos virutas de acero limpias para reciclado industrial.");
            dPaco.setCategoriaMaterial("Metal");
            dPaco.setCantidad(1500.0);
            dPaco.setUnidad("kg");
            dPaco.setPresupuestoMaximo(1.20);
            dPaco.setEstado(EstadoDemanda.ACTIVA);
            dPaco.setFechaPublicacion(LocalDateTime.now());
            dPaco.setEmpresa(empresa3);
            demandaRepository.save(dPaco);
            log.info("✅ Demand created for Empresa 3 (Paco).");
        }

        if (acuerdoRepository.count() == 0) {
            Acuerdo a = new Acuerdo();
            a.setMaterialIntercambiado("Viruta de Acero Inoxidable");
            a.setCantidad(500.0);
            a.setUnidad("kg");
            a.setPrecioAcordado(1200.0);
            a.setEstado(EstadoAcuerdo.COMPLETADO);
            a.setFechaRegistro(LocalDateTime.now().minusDays(2));
            a.setEmpresaOrigen(empresa1);
            a.setEmpresaDestino(empresa2);
            acuerdoRepository.save(a);
        }

        log.info("════════════════════════════════════════════");
        log.info("  Sample credentials (password: 1234 for all):");
        log.info("  ADMIN  → admin@ecomostoles.es");
        log.info("  USER 1 → contacto@metalesdelsur.es");
        log.info("  USER 2 → reciclajes@ecosur.es");
        log.info("  USER 3 → paco@reciclajes.es");
        log.info("════════════════════════════════════════════");
    }

    private void crearOferta(Empresa empresa, String titulo, String descripcion,
                              String tipoResiduo, Double cantidad, String unidad,
                              Double precio, String disponibilidad,
                              LocalDateTime fecha, String imagenFichero) {
        Oferta o = new Oferta();
        o.setTitulo(titulo);
        o.setDescripcion(descripcion);
        o.setTipoResiduo(tipoResiduo);
        o.setCantidad(cantidad);
        o.setUnidad(unidad);
        o.setPrecio(precio);
        o.setDisponibilidad(disponibilidad);
        o.setEstado(EstadoOferta.ACTIVA);
        o.setFechaPublicacion(fecha);
        o.setEmpresa(empresa);
        o.setImagen(cargarImagen(imagenFichero));
        ofertaRepository.save(o);
    }
}
