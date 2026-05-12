package br.com.fiap.ecocompliance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompensacaoResponseDTO(
        Long id,
        Long empresaId,
        String empresaNome,
        String tipoCompensacao,
        String descricao,
        BigDecimal quantidadeCompensada,
        LocalDate dataAcao
) {}
