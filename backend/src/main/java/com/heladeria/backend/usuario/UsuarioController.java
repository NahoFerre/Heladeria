package com.heladeria.backend.usuario;

import com.heladeria.backend.auditoria.AuditoriaService;
import com.heladeria.backend.auditoria.TipoAccionAuditoria;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    // Cualquier usuario autenticado puede consultar su propio rol (lo usa el frontend para armar el menú);
    // como la autenticación es Basic stateless (sin sesión), esta es la única llamada que el frontend hace
    // una sola vez justo después de un login exitoso, así que sirve como proxy del evento "login".
    @GetMapping("/yo")
    public UsuarioResponse yo(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.LOGIN, "USUARIO", usuario.getId(), null);
        return UsuarioResponse.desde(usuario);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRequest request, Authentication authentication) {
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Ya existe un usuario con ese nombre"));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        Usuario guardado = usuarioRepository.save(usuario);

        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.USUARIO_CREADO, "USUARIO", guardado.getId(),
                guardado.getUsername() + " (" + guardado.getRol() + ")");

        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(guardado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario objetivo = usuarioRepository.findById(id).orElse(null);
        if (objetivo == null) {
            return ResponseEntity.notFound().build();
        }
        if (objetivo.getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "No podés eliminar tu propio usuario"));
        }
        if (objetivo.getRol() == RolUsuario.ADMIN && usuarioRepository.countByRol(RolUsuario.ADMIN) <= 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "No se puede eliminar al único administrador"));
        }
        String nombreEliminado = objetivo.getUsername();
        usuarioRepository.deleteById(id);
        auditoriaService.registrar(authentication.getName(), TipoAccionAuditoria.USUARIO_ELIMINADO, "USUARIO", id, nombreEliminado);
        return ResponseEntity.noContent().build();
    }
}
