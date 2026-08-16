package br.com.fiap.ecocompliance.dto;

import java.math.BigDecimal;

public record IndicadoresSustentabilidadeDTO(
        long totalEmpresas,
        BigDecimal totalEmissoesCo2,
        BigDecimal totalCarbonoCompensado,
        BigDecimal saldoLiquidoCarbono,
        long licencasAtivas,
        long licencasVencidas,
        long totalAuditorias,
        long auditoriasAltoRisco,
        long agendamentosEmAndamento,
        long agendamentosPendentes
) {}
