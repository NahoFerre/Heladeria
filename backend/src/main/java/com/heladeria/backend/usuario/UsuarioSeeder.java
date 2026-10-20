package com.heladeria.backend.usuario;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea el primer usuario si la tabla está vacía, para no arrancar con el sistema sin login posible
@Component
public class UsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario usuario = new Usuario();
            usuario.setUsername("root");
            usuario.setPasswordHash(passwordEncoder.encode("heladeria123"));
            usuario.setRol(RolUsuario.ADMIN);
            usuarioRepository.save(usuario);
        }
    }
}
