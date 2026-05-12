package br.com.fiap.ecocompliance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequestDTO(

        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
        String nome,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(
                regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|\\d{14}",
                message = "CNPJ inválido. Use o formato 00.000.000/0000-00 ou apenas dígitos"
        )
        String cnpj,

        @NotBlank(message = "O setor é obrigatório")
        @Size(max = 100, message = "O setor deve ter no máximo 100 caracteres")
        String setor,

        @NotBlank(message = "O e-mail do responsável é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String emailResponsavel
) {}
