package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.StatusLicenca;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record LicencaRequestDTO(

        @NotNull(message = "O ID da empresa é obrigatório")
        @Positive(message = "O ID da empresa deve ser positivo")
        Long empresaId,

        @NotBlank(message = "O tipo de licença é obrigatório")
        @Size(max = 100)
        String tipoLicenca,

        @NotBlank(message = "O número da licença é obrigatório")
        @Size(max = 50)
        String numeroLicenca,

        @NotNull(message = "A data de emissão é obrigatória")
        @PastOrPresent(message = "A data de emissão deve ser passada ou presente")
        LocalDate dataEmissao,

        @NotNull(message = "A data de validade é obrigatória")
        LocalDate dataValidade,

        @NotNull(message = "O status é obrigatório")
        StatusLicenca status,

        @NotBlank(message = "O órgão emissor é obrigatório")
        @Size(max = 100)
        String orgaoEmissor
) {}
