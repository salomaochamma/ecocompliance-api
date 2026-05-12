package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.Role;
import jakarta.validation.constraints.*;

public record RegisterRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 150)
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String senha,

        @NotNull(message = "A role é obrigatória (ADMIN ou USER)")
        Role role
) {}
