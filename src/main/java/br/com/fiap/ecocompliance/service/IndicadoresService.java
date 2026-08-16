package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.IndicadoresSustentabilidadeDTO;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import br.com.fiap.ecocompliance.repository.AgendamentoReducaoCarbonoRepository;
import br.com.fiap.ecocompliance.repository.AuditoriaAmbientalRepository;
import br.com.fiap.ecocompliance.repository.CompensacaoAmbientalRepository;
import br.com.fiap.ecocompliance.repository.EmissaoCarbonoRepository;
import br.com.fiap.ecocompliance.repository.EmpresaRepository;
import br.com.fiap.ecocompliance.repository.LicencaAmbientalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IndicadoresService {

    private final EmpresaRepository empresaRepository;
    private final EmissaoCarbonoRepository emissaoRepository;
    private final CompensacaoAmbientalRepository compensacaoRepository;
    private final LicencaAmbientalRepository licencaRepository;
    private final AuditoriaAmbientalRepository auditoriaRepository;
    private final AgendamentoReducaoCarbonoRepository agendamentoRepository;

    @Transactional(readOnly = true)
    public IndicadoresSustentabilidadeDTO calcular() {
        long totalEmpresas = empresaRepository.count();

        BigDecimal totalEmissoes = emissaoRepository.somaTodasEmissoes();
        BigDecimal totalCompensado = compensacaoRepository.somaTodasCompensacoes();
        BigDecimal saldoLiquido = totalCompensado.subtract(totalEmissoes);

        long licencasAtivas = licencaRepository.countByStatus(StatusLicenca.ATIVA);
        long licencasVencidas = licencaRepository.countByStatus(StatusLicenca.VENCIDA);

        long totalAuditorias = auditoriaRepository.count();
        long auditoriasAltoRisco = auditoriaRepository.countByNivelRisco(NivelRisco.ALTO);

        long agendamentosEmAndamento = agendamentoRepository.countByStatus(StatusAgendamento.EM_ANDAMENTO);
        long agendamentosPendentes = agendamentoRepository.countByStatus(StatusAgendamento.PENDENTE);

        return new IndicadoresSustentabilidadeDTO(
                totalEmpresas,
                totalEmissoes,
                totalCompensado,
                saldoLiquido,
                licencasAtivas,
                licencasVencidas,
                totalAuditorias,
                auditoriasAltoRisco,
                agendamentosEmAndamento,
                agendamentosPendentes
        );
    }
}
