package br.com.fiap.ecocompliance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmissaoResponseDTO(
        Long id,
        Long empresaId,
        String empresaNome,
        BigDecimal quantidadeCo2,
        String fonteEmissao,
        LocalDate dataRegistro,
        String unidadeMedida
) {}
