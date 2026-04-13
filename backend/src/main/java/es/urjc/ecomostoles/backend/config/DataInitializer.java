package es.urjc.ecomostoles.backend.config;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            Optional<Empresa> existeEmpresa = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");
            
            if (existeEmpresa.isEmpty()) {
                Empresa empresa = new Empresa();
                empresa.setNombreComercial("Metales del Sur S.L.");
                empresa.setEmailContacto("contacto@metalesdelsur.es");
                empresa.setPassword(passwordEncoder.encode("1234")); // Encriptada
                
                empresa.setCif("B12345678"); 
                empresa.setSectorIndustrial("Metalurgia");
                
                empresaRepository.save(empresa);
                System.out.println("✅ ÉXITO: Empresa de prueba creada y encriptada.");
            } else {
                // LA EMPRESA EXISTE: Forzamos la actualización de la contraseña a BCrypt
                Empresa empresa = existeEmpresa.get();
                empresa.setPassword(passwordEncoder.encode("1234")); // Encriptada
                empresaRepository.save(empresa);
                System.out.println("✅ INFO: Empresa existente. Contraseña forzada a BCrypt.");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR al procesar la empresa de prueba: " + e.getMessage());
        }
    }
}
