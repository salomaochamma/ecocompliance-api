package br.com.fiap.ecocompliance.dto;

import java.time.LocalDateTime;

public record EmpresaResponseDTO(
        Long id,
        String nome,
        String cnpj,
        String setor,
        String emailResponsavel,
        LocalDateTime createdAt
) {}
