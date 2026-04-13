package es.urjc.ecomostoles.backend.security;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
                    return new UsernameNotFoundException("Empresa no encontrada");
                });

        System.out.println("✅ Usuario encontrado. Generando sesión para: " + empresa.getNombreComercial());
        
        return User.builder()
                .username(empresa.getEmailContacto())
                .password(empresa.getPassword())
                .roles("EMPRESA")
                .build();
    }
}
