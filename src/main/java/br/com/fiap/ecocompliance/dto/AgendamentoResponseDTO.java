package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.StatusAgendamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
        Long id,
        Long empresaId,
        String empresaNome,
        String titulo,
        String descricao,
        String tipoIniciativa,
        BigDecimal metaCo2,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusAgendamento status,
        LocalDateTime dataCriacao
) {}
