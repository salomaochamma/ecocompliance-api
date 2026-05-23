package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgendamentoRequestDTO(

        @NotNull(message = "O ID da empresa é obrigatório")
        @Positive
        Long empresaId,

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150)
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 500)
        String descricao,

        @NotBlank(message = "O tipo de iniciativa é obrigatório")
        @Size(max = 50)
        String tipoIniciativa,

        @NotNull(message = "A meta de redução de CO2 é obrigatória")
        @Positive(message = "A meta de CO2 deve ser positiva")
        BigDecimal metaCo2,

        @NotNull(message = "A data de início é obrigatória")
        @FutureOrPresent(message = "A data de início não pode ser no passado")
        LocalDate dataInicio,

        @NotNull(message = "A data de fim é obrigatória")
        @Future(message = "A data de fim deve ser futura")
        LocalDate dataFim,

        StatusAgendamento status
) {}
