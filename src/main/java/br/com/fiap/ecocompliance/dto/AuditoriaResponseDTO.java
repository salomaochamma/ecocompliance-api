package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.NivelRisco;

import java.time.LocalDate;

public record AuditoriaResponseDTO(
        Long id,
        Long empresaId,
        String empresaNome,
        LocalDate dataAuditoria,
        String resultado,
        String observacoes,
        NivelRisco nivelRisco
) {}
