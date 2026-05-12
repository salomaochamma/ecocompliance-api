package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.EmpresaResponseDTO;
import br.com.fiap.ecocompliance.dto.RelatorioEmpresaDTO;
import br.com.fiap.ecocompliance.entity.AuditoriaAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import br.com.fiap.ecocompliance.repository.AuditoriaAmbientalRepository;
import br.com.fiap.ecocompliance.repository.CompensacaoAmbientalRepository;
import br.com.fiap.ecocompliance.repository.EmissaoCarbonoRepository;
import br.com.fiap.ecocompliance.repository.LicencaAmbientalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final EmpresaService empresaService;
    private final EmissaoCarbonoRepository emissaoRepository;
    private final CompensacaoAmbientalRepository compensacaoRepository;
    private final LicencaAmbientalRepository licencaRepository;
    private final AuditoriaAmbientalRepository auditoriaRepository;

    @Transactional(readOnly = true)
    public RelatorioEmpresaDTO gerarRelatorioEmpresa(Long empresaId) {
        Empresa empresa = empresaService.buscarEntidade(empresaId);

        BigDecimal totalEmissoes = emissaoRepository.somaEmissoesPorEmpresa(empresaId);
        BigDecimal totalCompensado = compensacaoRepository.somaCompensacoesPorEmpresa(empresaId);

        long licencasAtivas = licencaRepository.countByEmpresaIdAndStatus(empresaId, StatusLicenca.ATIVA);
        long licencasVencidas = licencaRepository.countByEmpresaIdAndStatus(empresaId, StatusLicenca.VENCIDA);
        long qtdAuditorias = auditoriaRepository.countByEmpresaId(empresaId);

        List<AuditoriaAmbiental> auditorias = auditoriaRepository.findByEmpresaId(empresaId);
        NivelRisco maiorRisco = auditorias.stream()
                .map(AuditoriaAmbiental::getNivelRisco)
                .max(Comparator.comparingInt(this::pesoRisco))
                .orElse(null);

        EmpresaResponseDTO empresaDto = new EmpresaResponseDTO(
                empresa.getId(), empresa.getNome(), empresa.getCnpj(),
                empresa.getSetor(), empresa.getEmailResponsavel(), empresa.getCreatedAt()
        );

        return new RelatorioEmpresaDTO(
                empresaDto,
                totalEmissoes,
                totalCompensado,
                licencasAtivas,
                licencasVencidas,
                qtdAuditorias,
                maiorRisco
        );
    }

    private int pesoRisco(NivelRisco r) {
        return switch (r) {
            case BAIXO -> 1;
            case MEDIO -> 2;
            case ALTO -> 3;
        };
    }
}
