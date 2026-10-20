package com.heladeria.backend.usuario;

public record UsuarioResponse(Long id, String username, RolUsuario rol) {

    static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getRol());
    }
}
