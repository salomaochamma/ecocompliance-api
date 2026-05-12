package br.com.fiap.ecocompliance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompensacaoRequestDTO(

        @NotNull(message = "O ID da empresa é obrigatório")
        @Positive
        Long empresaId,

        @NotBlank(message = "O tipo de compensação é obrigatório")
        @Size(max = 100)
        String tipoCompensacao,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 500)
        String descricao,

        @NotNull(message = "A quantidade compensada é obrigatória")
        @Positive(message = "A quantidade compensada deve ser positiva")
        BigDecimal quantidadeCompensada,

        @NotNull(message = "A data da ação é obrigatória")
        @PastOrPresent(message = "A data da ação deve ser passada ou presente")
        LocalDate dataAcao
) {}
