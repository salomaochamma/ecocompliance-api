package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.AgendamentoRequestDTO;
import br.com.fiap.ecocompliance.dto.AgendamentoResponseDTO;
import br.com.fiap.ecocompliance.entity.AgendamentoReducaoCarbono;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.AgendamentoReducaoCarbonoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoReducaoCarbonoRepository agendamentoRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Empresa empresa;
    private AgendamentoReducaoCarbono agendamento;
    private AgendamentoRequestDTO requestDTO;

    private static final LocalDate INICIO = LocalDate.now().plusDays(1);
    private static final LocalDate FIM = LocalDate.now().plusMonths(6);

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

        agendamento = AgendamentoReducaoCarbono.builder()
                .id(1L)
                .empresa(empresa)
                .titulo("Troca de Caldeiras por Biomassa")
                .descricao("Substituição gradual das caldeiras a gás por biomassa")
                .tipoIniciativa("Eficiência Energética")
                .metaCo2(new BigDecimal("600.00"))
                .dataInicio(INICIO)
                .dataFim(FIM)
                .status(StatusAgendamento.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .build();

        requestDTO = new AgendamentoRequestDTO(
                1L,
                "Troca de Caldeiras por Biomassa",
                "Substituição gradual das caldeiras a gás por biomassa",
                "Eficiência Energética",
                new BigDecimal("600.00"),
                INICIO,
                FIM,
                StatusAgendamento.PENDENTE
        );
    }

    @Test
    @DisplayName("listar deve retornar página de DTOs mapeados")
    void listar_deveRetornarPaginaDTOs() {
        when(agendamentoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(agendamento)));

        Page<AgendamentoResponseDTO> resultado = agendamentoService.listar(Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Troca de Caldeiras por Biomassa");
        verify(agendamentoRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando agendamento existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        AgendamentoResponseDTO resultado = agendamentoService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.status()).isEqualTo(StatusAgendamento.PENDENTE);
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("buscarPorEmpresa deve retornar agendamentos da empresa")
    void buscarPorEmpresa_deveRetornarAgendamentosDaEmpresa() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(agendamentoRepository.findByEmpresaId(1L)).thenReturn(List.of(agendamento));

        List<AgendamentoResponseDTO> resultado = agendamentoService.buscarPorEmpresa(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).empresaNome()).isEqualTo("Verde Industria S.A.");
    }

    @Test
    @DisplayName("buscarPorStatus deve retornar agendamentos com o status informado")
    void buscarPorStatus_deveRetornarFiltrado() {
        when(agendamentoRepository.findByStatus(StatusAgendamento.PENDENTE)).thenReturn(List.of(agendamento));

        List<AgendamentoResponseDTO> resultado = agendamentoService.buscarPorStatus(StatusAgendamento.PENDENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).status()).isEqualTo(StatusAgendamento.PENDENTE);
    }

    @Test
    @DisplayName("criar deve salvar e retornar DTO quando datas válidas")
    void criar_quandoValido_deveRetornarDTO() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(agendamentoRepository.save(any(AgendamentoReducaoCarbono.class))).thenReturn(agendamento);

        AgendamentoResponseDTO resultado = agendamentoService.criar(requestDTO);

        assertThat(resultado.titulo()).isEqualTo("Troca de Caldeiras por Biomassa");
        assertThat(resultado.metaCo2()).isEqualByComparingTo("600.00");
        verify(agendamentoRepository).save(any(AgendamentoReducaoCarbono.class));
    }

    @Test
    @DisplayName("criar deve usar status PENDENTE como padrão quando status não informado")
    void criar_quandoStatusNulo_deveUsarPendente() {
        AgendamentoRequestDTO semStatus = new AgendamentoRequestDTO(
                1L, "Titulo", "Descricao", "Tipo",
                new BigDecimal("100.00"), INICIO, FIM, null);

        AgendamentoReducaoCarbono salvo = AgendamentoReducaoCarbono.builder()
                .id(2L).empresa(empresa).titulo("Titulo").descricao("Descricao")
                .tipoIniciativa("Tipo").metaCo2(new BigDecimal("100.00"))
                .dataInicio(INICIO).dataFim(FIM)
                .status(StatusAgendamento.PENDENTE).dataCriacao(LocalDateTime.now())
                .build();

        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(agendamentoRepository.save(any())).thenReturn(salvo);

        AgendamentoResponseDTO resultado = agendamentoService.criar(semStatus);

        assertThat(resultado.status()).isEqualTo(StatusAgendamento.PENDENTE);
    }

    @Test
    @DisplayName("criar deve lançar BusinessException quando dataFim anterior a dataInicio")
    void criar_quandoDataFimAnteriorAInicio_deveLancarBusinessException() {
        AgendamentoRequestDTO dtoInvalido = new AgendamentoRequestDTO(
                1L, "Titulo", "Descricao", "Tipo",
                new BigDecimal("100.00"),
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusDays(1),
                null
        );

        assertThatThrownBy(() -> agendamentoService.criar(dtoInvalido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("data de fim");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar deve lançar BusinessException quando dataFim igual a dataInicio")
    void criar_quandoDataFimIgualInicio_deveLancarBusinessException() {
        LocalDate mesmaData = LocalDate.now().plusMonths(3);
        AgendamentoRequestDTO dtoInvalido = new AgendamentoRequestDTO(
                1L, "Titulo", "Descricao", "Tipo",
                new BigDecimal("100.00"), mesmaData, mesmaData, null);

        assertThatThrownBy(() -> agendamentoService.criar(dtoInvalido))
                .isInstanceOf(BusinessException.class);

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve modificar campos e retornar DTO atualizado")
    void atualizar_quandoValido_deveRetornarDTO() {
        AgendamentoReducaoCarbono emAndamento = AgendamentoReducaoCarbono.builder()
                .id(1L).empresa(empresa).titulo("Titulo").descricao("Desc")
                .tipoIniciativa("Tipo").metaCo2(new BigDecimal("100.00"))
                .dataInicio(INICIO).dataFim(FIM)
                .status(StatusAgendamento.EM_ANDAMENTO).dataCriacao(LocalDateTime.now())
                .build();

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(emAndamento));
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponseDTO resultado = agendamentoService.atualizar(1L, requestDTO);

        assertThat(resultado).isNotNull();
        verify(agendamentoRepository).save(any());
    }

    @Test
    @DisplayName("atualizar deve lançar BusinessException quando agendamento está CONCLUIDO")
    void atualizar_quandoConcluido_deveLancarBusinessException() {
        AgendamentoReducaoCarbono concluido = AgendamentoReducaoCarbono.builder()
                .id(1L).empresa(empresa).titulo("T").descricao("D")
                .tipoIniciativa("Tp").metaCo2(new BigDecimal("100.00"))
                .dataInicio(INICIO).dataFim(FIM)
                .status(StatusAgendamento.CONCLUIDO).dataCriacao(LocalDateTime.now())
                .build();

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(concluido));

        assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CONCLUIDO");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve lançar BusinessException quando agendamento está CANCELADO")
    void atualizar_quandoCancelado_deveLancarBusinessException() {
        AgendamentoReducaoCarbono cancelado = AgendamentoReducaoCarbono.builder()
                .id(1L).empresa(empresa).titulo("T").descricao("D")
                .tipoIniciativa("Tp").metaCo2(new BigDecimal("100.00"))
                .dataInicio(INICIO).dataFim(FIM)
                .status(StatusAgendamento.CANCELADO).dataCriacao(LocalDateTime.now())
                .build();

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CANCELADO");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("excluir deve deletar agendamento quando encontrado")
    void excluir_quandoEncontrado_deveExcluir() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        agendamentoService.excluir(1L);

        verify(agendamentoRepository).delete(agendamento);
    }

    @Test
    @DisplayName("excluir deve lançar ResourceNotFoundException quando agendamento não existe")
    void excluir_quandoNaoEncontrado_deveLancarExcecao() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(agendamentoRepository, never()).delete(any());
    }
}
