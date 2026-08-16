package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.RelatorioEmpresaDTO;
import br.com.fiap.ecocompliance.entity.AuditoriaAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.AuditoriaAmbientalRepository;
import br.com.fiap.ecocompliance.repository.CompensacaoAmbientalRepository;
import br.com.fiap.ecocompliance.repository.EmissaoCarbonoRepository;
import br.com.fiap.ecocompliance.repository.LicencaAmbientalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock private EmpresaService empresaService;
    @Mock private EmissaoCarbonoRepository emissaoRepository;
    @Mock private CompensacaoAmbientalRepository compensacaoRepository;
    @Mock private LicencaAmbientalRepository licencaRepository;
    @Mock private AuditoriaAmbientalRepository auditoriaRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("Verde Industria S.A.")
                .cnpj("12.345.678/0001-90")
                .setor("Manufatura")
                .emailResponsavel("esg@verde.com.br")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AuditoriaAmbiental auditoria(NivelRisco risco) {
        return AuditoriaAmbiental.builder()
                .id(1L).empresa(empresa)
                .dataAuditoria(LocalDate.now())
                .resultado("Resultado").nivelRisco(risco)
                .build();
    }

    @Test
    @DisplayName("gerarRelatorioEmpresa deve retornar relatório completo quando empresa existe")
    void gerarRelatorioEmpresa_quandoEncontrada_deveRetornarRelatorio() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.somaEmissoesPorEmpresa(1L)).thenReturn(new BigDecimal("1250.00"));
        when(compensacaoRepository.somaCompensacoesPorEmpresa(1L)).thenReturn(new BigDecimal("800.00"));
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.ATIVA)).thenReturn(2L);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.VENCIDA)).thenReturn(1L);
        when(auditoriaRepository.countByEmpresaId(1L)).thenReturn(3L);
        when(auditoriaRepository.findByEmpresaId(1L))
                .thenReturn(List.of(auditoria(NivelRisco.BAIXO), auditoria(NivelRisco.ALTO)));

        RelatorioEmpresaDTO resultado = relatorioService.gerarRelatorioEmpresa(1L);

        assertThat(resultado.empresa().nome()).isEqualTo("Verde Industria S.A.");
        assertThat(resultado.totalEmissoesCarbono()).isEqualByComparingTo("1250.00");
        assertThat(resultado.totalCarbonoCompensado()).isEqualByComparingTo("800.00");
        assertThat(resultado.quantidadeLicencasAtivas()).isEqualTo(2L);
        assertThat(resultado.quantidadeLicencasVencidas()).isEqualTo(1L);
        assertThat(resultado.quantidadeAuditorias()).isEqualTo(3L);
        assertThat(resultado.maiorNivelRisco()).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    @DisplayName("gerarRelatorioEmpresa deve lançar ResourceNotFoundException quando empresa não existe")
    void gerarRelatorioEmpresa_quandoNaoEncontrada_deveLancarExcecao() {
        when(empresaService.buscarEntidade(99L))
                .thenThrow(new ResourceNotFoundException("Empresa não encontrada com ID: 99"));

        assertThatThrownBy(() -> relatorioService.gerarRelatorioEmpresa(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("gerarRelatorioEmpresa deve retornar maiorNivelRisco nulo quando não há auditorias")
    void gerarRelatorioEmpresa_quandoSemAuditorias_maiorRiscoDeveSerNulo() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.somaEmissoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(compensacaoRepository.somaCompensacoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.ATIVA)).thenReturn(0L);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.VENCIDA)).thenReturn(0L);
        when(auditoriaRepository.countByEmpresaId(1L)).thenReturn(0L);
        when(auditoriaRepository.findByEmpresaId(1L)).thenReturn(List.of());

        RelatorioEmpresaDTO resultado = relatorioService.gerarRelatorioEmpresa(1L);

        assertThat(resultado.maiorNivelRisco()).isNull();
        assertThat(resultado.quantidadeAuditorias()).isZero();
    }

    @Test
    @DisplayName("gerarRelatorioEmpresa deve identificar ALTO como maior risco entre múltiplas auditorias")
    void gerarRelatorioEmpresa_comMultiplasAuditorias_deveIdentificarMaiorRisco() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.somaEmissoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(compensacaoRepository.somaCompensacoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.ATIVA)).thenReturn(0L);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.VENCIDA)).thenReturn(0L);
        when(auditoriaRepository.countByEmpresaId(1L)).thenReturn(3L);
        when(auditoriaRepository.findByEmpresaId(1L)).thenReturn(
                List.of(auditoria(NivelRisco.BAIXO), auditoria(NivelRisco.MEDIO), auditoria(NivelRisco.ALTO)));

        RelatorioEmpresaDTO resultado = relatorioService.gerarRelatorioEmpresa(1L);

        assertThat(resultado.maiorNivelRisco()).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    @DisplayName("gerarRelatorioEmpresa deve identificar MEDIO quando não há auditoria de alto risco")
    void gerarRelatorioEmpresa_semAuditoriaAltoRisco_deveIdentificarMedio() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.somaEmissoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(compensacaoRepository.somaCompensacoesPorEmpresa(1L)).thenReturn(BigDecimal.ZERO);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.ATIVA)).thenReturn(0L);
        when(licencaRepository.countByEmpresaIdAndStatus(1L, StatusLicenca.VENCIDA)).thenReturn(0L);
        when(auditoriaRepository.countByEmpresaId(1L)).thenReturn(2L);
        when(auditoriaRepository.findByEmpresaId(1L)).thenReturn(
                List.of(auditoria(NivelRisco.BAIXO), auditoria(NivelRisco.MEDIO)));

        RelatorioEmpresaDTO resultado = relatorioService.gerarRelatorioEmpresa(1L);

        assertThat(resultado.maiorNivelRisco()).isEqualTo(NivelRisco.MEDIO);
    }
}
