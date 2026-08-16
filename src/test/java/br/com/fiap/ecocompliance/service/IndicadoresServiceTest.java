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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicadoresServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private EmissaoCarbonoRepository emissaoRepository;
    @Mock private CompensacaoAmbientalRepository compensacaoRepository;
    @Mock private LicencaAmbientalRepository licencaRepository;
    @Mock private AuditoriaAmbientalRepository auditoriaRepository;
    @Mock private AgendamentoReducaoCarbonoRepository agendamentoRepository;

    @InjectMocks
    private IndicadoresService indicadoresService;

    @Test
    @DisplayName("calcular deve agregar todos os indicadores corretamente")
    void calcular_deveRetornarIndicadoresCorretos() {
        when(empresaRepository.count()).thenReturn(3L);
        when(emissaoRepository.somaTodasEmissoes()).thenReturn(new BigDecimal("2600.00"));
        when(compensacaoRepository.somaTodasCompensacoes()).thenReturn(new BigDecimal("1800.00"));
        when(licencaRepository.countByStatus(StatusLicenca.ATIVA)).thenReturn(4L);
        when(licencaRepository.countByStatus(StatusLicenca.VENCIDA)).thenReturn(1L);
        when(auditoriaRepository.count()).thenReturn(5L);
        when(auditoriaRepository.countByNivelRisco(NivelRisco.ALTO)).thenReturn(2L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.EM_ANDAMENTO)).thenReturn(1L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.PENDENTE)).thenReturn(2L);

        IndicadoresSustentabilidadeDTO resultado = indicadoresService.calcular();

        assertThat(resultado.totalEmpresas()).isEqualTo(3L);
        assertThat(resultado.totalEmissoesCo2()).isEqualByComparingTo("2600.00");
        assertThat(resultado.totalCarbonoCompensado()).isEqualByComparingTo("1800.00");
        assertThat(resultado.saldoLiquidoCarbono()).isEqualByComparingTo("-800.00");
        assertThat(resultado.licencasAtivas()).isEqualTo(4L);
        assertThat(resultado.licencasVencidas()).isEqualTo(1L);
        assertThat(resultado.totalAuditorias()).isEqualTo(5L);
        assertThat(resultado.auditoriasAltoRisco()).isEqualTo(2L);
        assertThat(resultado.agendamentosEmAndamento()).isEqualTo(1L);
        assertThat(resultado.agendamentosPendentes()).isEqualTo(2L);
    }

    @Test
    @DisplayName("calcular deve retornar saldo líquido positivo quando compensação supera emissões")
    void calcular_quandoCompensacaoSuperaEmissoes_saldoDeveSerPositivo() {
        when(empresaRepository.count()).thenReturn(1L);
        when(emissaoRepository.somaTodasEmissoes()).thenReturn(new BigDecimal("500.00"));
        when(compensacaoRepository.somaTodasCompensacoes()).thenReturn(new BigDecimal("800.00"));
        when(licencaRepository.countByStatus(StatusLicenca.ATIVA)).thenReturn(0L);
        when(licencaRepository.countByStatus(StatusLicenca.VENCIDA)).thenReturn(0L);
        when(auditoriaRepository.count()).thenReturn(0L);
        when(auditoriaRepository.countByNivelRisco(NivelRisco.ALTO)).thenReturn(0L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.EM_ANDAMENTO)).thenReturn(0L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.PENDENTE)).thenReturn(0L);

        IndicadoresSustentabilidadeDTO resultado = indicadoresService.calcular();

        assertThat(resultado.saldoLiquidoCarbono()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("calcular deve retornar zero para saldo quando emissões e compensações são iguais")
    void calcular_quandoEmissoesIguaisACompensacoes_saldoDeveSerZero() {
        when(empresaRepository.count()).thenReturn(0L);
        when(emissaoRepository.somaTodasEmissoes()).thenReturn(BigDecimal.ZERO);
        when(compensacaoRepository.somaTodasCompensacoes()).thenReturn(BigDecimal.ZERO);
        when(licencaRepository.countByStatus(StatusLicenca.ATIVA)).thenReturn(0L);
        when(licencaRepository.countByStatus(StatusLicenca.VENCIDA)).thenReturn(0L);
        when(auditoriaRepository.count()).thenReturn(0L);
        when(auditoriaRepository.countByNivelRisco(NivelRisco.ALTO)).thenReturn(0L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.EM_ANDAMENTO)).thenReturn(0L);
        when(agendamentoRepository.countByStatus(StatusAgendamento.PENDENTE)).thenReturn(0L);

        IndicadoresSustentabilidadeDTO resultado = indicadoresService.calcular();

        assertThat(resultado.saldoLiquidoCarbono()).isEqualByComparingTo("0");
        assertThat(resultado.totalEmpresas()).isZero();
    }
}
