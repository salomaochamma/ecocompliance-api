package br.com.fiap.ecocompliance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmissaoRequestDTO(

        @NotNull(message = "O ID da empresa é obrigatório")
        @Positive
        Long empresaId,

        @NotNull(message = "A quantidade de CO2 é obrigatória")
        @Positive(message = "A quantidade de CO2 deve ser positiva")
        BigDecimal quantidadeCo2,

        @NotBlank(message = "A fonte de emissão é obrigatória")
        @Size(max = 150)
        String fonteEmissao,

        @NotNull(message = "A data de registro é obrigatória")
        @PastOrPresent(message = "A data de registro deve ser passada ou presente")
        LocalDate dataRegistro,

        @NotBlank(message = "A unidade de medida é obrigatória")
        @Size(max = 20)
        String unidadeMedida
) {}
