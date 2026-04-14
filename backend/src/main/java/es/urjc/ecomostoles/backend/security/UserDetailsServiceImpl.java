package es.urjc.ecomostoles.backend.security;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de UserDetailsService que carga un usuario desde la entidad Empresa.
 *
 * Los roles se leen directamente de Empresa.roles (campo @ElementCollection).
 * Si la lista está vacía o es nula, se asigna EMPRESA por defecto.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Intentando loguear al usuario: " + email);

        Empresa empresa = empresaRepository.findByEmailContacto(email)
                .orElseThrow(() -> {
                    System.err.println("❌ Usuario no encontrado en la BBDD: " + email);
                    return new UsernameNotFoundException("Empresa no encontrada: " + email);
                });

        // Leer roles de la entidad; si no tiene, asignar EMPRESA por defecto
        List<String> roles = empresa.getRoles();
        String[] rolesArray = (roles != null && !roles.isEmpty())
                ? roles.toArray(new String[0])
                : new String[]{"EMPRESA"};

        System.out.println("✅ Sesión iniciada: " + empresa.getNombreComercial()
                + " | Roles: " + List.of(rolesArray));

        return User.builder()
                .username(empresa.getEmailContacto())
                .password(empresa.getPassword())
                .roles(rolesArray)   // Spring añade automáticamente el prefijo ROLE_
                .build();
    }
}
