package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.NivelRisco;

import java.math.BigDecimal;

public record RelatorioEmpresaDTO(
        EmpresaResponseDTO empresa,
        BigDecimal totalEmissoesCarbono,
        BigDecimal totalCarbonoCompensado,
        Long quantidadeLicencasAtivas,
        Long quantidadeLicencasVencidas,
        Long quantidadeAuditorias,
        NivelRisco maiorNivelRisco
) {}
