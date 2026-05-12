package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.Role;

public record AuthResponseDTO(
        String token,
        String tipo,
        String email,
        String nome,
        Role role
) {
    public static AuthResponseDTO of(String token, String email, String nome, Role role) {
        return new AuthResponseDTO(token, "Bearer", email, nome, role);
    }
}
