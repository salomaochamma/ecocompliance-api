package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.NivelRisco;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AuditoriaRequestDTO(

        @NotNull(message = "O ID da empresa é obrigatório")
        @Positive
        Long empresaId,

        @NotNull(message = "A data da auditoria é obrigatória")
        @PastOrPresent(message = "A data da auditoria deve ser passada ou presente")
        LocalDate dataAuditoria,

        @NotBlank(message = "O resultado é obrigatório")
        @Size(max = 200)
        String resultado,

        @Size(max = 1000)
        String observacoes,

        @NotNull(message = "O nível de risco é obrigatório")
        NivelRisco nivelRisco
) {}
