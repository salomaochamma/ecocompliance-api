package br.com.fiap.ecocompliance.dto;

import br.com.fiap.ecocompliance.entity.StatusLicenca;

import java.time.LocalDate;

public record LicencaResponseDTO(
        Long id,
        Long empresaId,
        String empresaNome,
        String tipoLicenca,
        String numeroLicenca,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        StatusLicenca status,
        String orgaoEmissor
) {}
