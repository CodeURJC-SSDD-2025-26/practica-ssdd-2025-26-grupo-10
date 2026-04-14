package es.urjc.ecomostoles.backend.config;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import es.urjc.ecomostoles.backend.repository.DemandaRepository;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private OfertaRepository  ofertaRepository;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private AcuerdoRepository acuerdoRepository;
    @Autowired private PasswordEncoder   passwordEncoder;

    // ─────────────────────────────────────────────────────────────────────────
    // Lee un fichero de static/img/ y devuelve sus bytes (StreamUtils = Spring)
    // ─────────────────────────────────────────────────────────────────────────
    private byte[] cargarImagen(String nombreFichero) {
        String ruta = "static/img/" + nombreFichero;
        try {
            ClassPathResource res = new ClassPathResource(ruta);
            if (!res.exists()) {
                System.err.println("⚠️  Imagen no encontrada: " + ruta);
                return null;
            }
            try (InputStream is = res.getInputStream()) {
                byte[] bytes = StreamUtils.copyToByteArray(is);
                System.out.println("   📷 '" + nombreFichero + "' → " + bytes.length + " bytes");
                return bytes;
            }
        } catch (IOException e) {
            System.err.println("❌ Error leyendo '" + ruta + "': " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Crea o actualiza una empresa con la información dada
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Crea o actualiza una empresa buscando POR CIF (campo único),
     * no por email, para evitar DataIntegrityViolationException cuando
     * la BD ya tiene un registro con ese CIF de una ejecución anterior.
     */
    private Empresa upsertEmpresa(String email, String nombre, String cif,
                                   String sector, String direccion, String tel,
                                   String descripcion, String logoFichero,
                                   List<String> roles) {
        // ← Búsqueda por CIF (unique key real), no por email
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
        System.out.println("════════════════════════════════════════════");
        System.out.println("  DataInitializer — Práctica 2 SSDD");
        System.out.println("════════════════════════════════════════════");

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
            "logo.jfif",
            List.of("ADMIN")          // ← rol ADMIN
        );
        System.out.println("✅ ADMIN creado/actualizado → " + admin.getEmailContacto()
                + " | password: 1234 | roles: " + admin.getRoles());

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
            "logo.jfif",
            List.of("EMPRESA")        // ← rol USER/EMPRESA
        );
        System.out.println("✅ Empresa 1 → " + empresa1.getEmailContacto()
                + " | roles: " + empresa1.getRoles());

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
            "logo.jfif",
            List.of("EMPRESA")
        );
        System.out.println("✅ Empresa 2 → " + empresa2.getEmailContacto()
                + " | roles: " + empresa2.getRoles());

        // ══════════════════════════════════════════
        // 4. Ofertas de prueba con imágenes BLOB
        // ══════════════════════════════════════════
        var ofertasEmpresa1 = ofertaRepository.findByEmpresa(empresa1);

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

            System.out.println("✅ 2 ofertas creadas para Empresa 1.");
        } else {
            // Reparar imágenes vacías si existen
            ofertasEmpresa1.stream()
                .filter(o -> o.getImagen() == null || o.getImagen().length == 0)
                .forEach(o -> {
                    o.setImagen(cargarImagen("virutas.webp"));
                    ofertaRepository.save(o);
                    System.out.println("   🔄 Imagen reparada en: " + o.getTitulo());
                });
        }

        var ofertasEmpresa2 = ofertaRepository.findByEmpresa(empresa2);
        if (ofertasEmpresa2.isEmpty()) {
            crearOferta(empresa2,
                "Retales de PVC Industrial",
                "Recortes de PVC rígido (2-10mm). Ideales para reciclaje en piezas pequeñas.",
                "Plástico", 80.0, "kg", 0.20, "Consultar",
                LocalDateTime.now().minusDays(1), "retales-pvc.webp");

            System.out.println("✅ 1 oferta creada para Empresa 2.");
        }

        // ══════════════════════════════════════════
        // 5. Demandas y Acuerdos de prueba 
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
            d.setEstado("Activa");
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
            d2.setEstado("Activa");
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
            "logo.jfif",
            List.of("EMPRESA")
        );
        System.out.println("✅ Empresa 3 → " + empresa3.getEmailContacto()
                + " | roles: " + empresa3.getRoles());

        if (demandaRepository.findByEmpresa(empresa3).isEmpty()) {
            Demanda dPaco = new Demanda();
            dPaco.setTitulo("Virutas de Acero");
            dPaco.setDescripcion("Buscamos virutas de acero limpias para reciclado industrial.");
            dPaco.setCategoriaMaterial("Metal");
            dPaco.setCantidad(1500.0);
            dPaco.setUnidad("kg");
            dPaco.setPresupuestoMaximo(1.20);
            dPaco.setEstado("Activa");
            dPaco.setFechaPublicacion(LocalDateTime.now());
            dPaco.setEmpresa(empresa3);
            demandaRepository.save(dPaco);
            System.out.println("✅ Demand created for Empresa 3 (Paco).");
        }

        if (acuerdoRepository.count() == 0) {
            Acuerdo a = new Acuerdo();
            a.setMaterialIntercambiado("Viruta de Acero Inoxidable");
            a.setCantidad(500.0);
            a.setUnidad("kg");
            a.setPrecioAcordado(1200.0);
            a.setEstado("Completado");
            a.setFechaRegistro(LocalDateTime.now().minusDays(2));
            a.setEmpresaOrigen(empresa1);
            a.setEmpresaDestino(empresa2);
            acuerdoRepository.save(a);
        }

        System.out.println("════════════════════════════════════════════");
        System.out.println("  Sample credentials (password: 1234 for all):");
        System.out.println("  ADMIN  → admin@ecomostoles.es");
        System.out.println("  USER 1 → contacto@metalesdelsur.es");
        System.out.println("  USER 2 → reciclajes@ecosur.es");
        System.out.println("  USER 3 → paco@reciclajes.es");
        System.out.println("════════════════════════════════════════════");
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
        o.setEstado("Activa");
        o.setFechaPublicacion(fecha);
        o.setEmpresa(empresa);
        o.setImagen(cargarImagen(imagenFichero));
        ofertaRepository.save(o);
    }
}
