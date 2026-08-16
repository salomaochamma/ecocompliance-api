package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.CompensacaoRequestDTO;
import br.com.fiap.ecocompliance.dto.CompensacaoResponseDTO;
import br.com.fiap.ecocompliance.entity.CompensacaoAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.CompensacaoAmbientalRepository;
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
class CompensacaoServiceTest {

    @Mock
    private CompensacaoAmbientalRepository compensacaoRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private CompensacaoService compensacaoService;

    private Empresa empresa;
    private CompensacaoAmbiental compensacao;
    private CompensacaoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("EcoEnergia Renovaveis Ltda")
                .cnpj("23.456.789/0001-00")
                .setor("Energia")
                .emailResponsavel("esg@ecoenergia.com.br")
                .createdAt(LocalDateTime.now())
                .build();

        compensacao = CompensacaoAmbiental.builder()
                .id(1L)
                .empresa(empresa)
                .tipoCompensacao("Reflorestamento")
                .descricao("Plantio de 5000 árvores nativas")
                .quantidadeCompensada(new BigDecimal("320.50"))
                .dataAcao(LocalDate.now())
                .build();

        requestDTO = new CompensacaoRequestDTO(
                1L,
                "Reflorestamento",
                "Plantio de 5000 árvores nativas",
                new BigDecimal("320.50"),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("listar deve retornar página de DTOs mapeados")
    void listar_deveRetornarPaginaDTOs() {
        when(compensacaoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(compensacao)));

        Page<CompensacaoResponseDTO> resultado = compensacaoService.listar(Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).tipoCompensacao()).isEqualTo("Reflorestamento");
        verify(compensacaoRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando compensação existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(compensacaoRepository.findById(1L)).thenReturn(Optional.of(compensacao));

        CompensacaoResponseDTO resultado = compensacaoService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.tipoCompensacao()).isEqualTo("Reflorestamento");
        assertThat(resultado.quantidadeCompensada()).isEqualByComparingTo("320.50");
        assertThat(resultado.empresaNome()).isEqualTo("EcoEnergia Renovaveis Ltda");
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando compensação não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(compensacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compensacaoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("criar deve salvar e retornar DTO quando dados válidos")
    void criar_quandoValido_deveRetornarDTO() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(compensacaoRepository.save(any(CompensacaoAmbiental.class))).thenReturn(compensacao);

        CompensacaoResponseDTO resultado = compensacaoService.criar(requestDTO);

        assertThat(resultado.tipoCompensacao()).isEqualTo("Reflorestamento");
        assertThat(resultado.quantidadeCompensada()).isEqualByComparingTo("320.50");
        verify(compensacaoRepository).save(any(CompensacaoAmbiental.class));
    }

    @Test
    @DisplayName("criar deve lançar exceção quando empresa não encontrada")
    void criar_quandoEmpresaNaoEncontrada_deveLancarExcecao() {
        when(empresaService.buscarEntidade(1L))
                .thenThrow(new ResourceNotFoundException("Empresa não encontrada com ID: 1"));

        assertThatThrownBy(() -> compensacaoService.criar(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(compensacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve modificar campos e retornar DTO atualizado")
    void atualizar_quandoValido_deveRetornarDTO() {
        CompensacaoRequestDTO dtoAtualizado = new CompensacaoRequestDTO(
                1L, "Energia Solar", "Instalação de painéis", new BigDecimal("500.00"), LocalDate.now());

        CompensacaoAmbiental atualizada = CompensacaoAmbiental.builder()
                .id(1L).empresa(empresa)
                .tipoCompensacao("Energia Solar")
                .descricao("Instalação de painéis")
                .quantidadeCompensada(new BigDecimal("500.00"))
                .dataAcao(LocalDate.now())
                .build();

        when(compensacaoRepository.findById(1L)).thenReturn(Optional.of(compensacao));
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(compensacaoRepository.save(any(CompensacaoAmbiental.class))).thenReturn(atualizada);

        CompensacaoResponseDTO resultado = compensacaoService.atualizar(1L, dtoAtualizado);

        assertThat(resultado.tipoCompensacao()).isEqualTo("Energia Solar");
        assertThat(resultado.quantidadeCompensada()).isEqualByComparingTo("500.00");
        verify(compensacaoRepository).save(any(CompensacaoAmbiental.class));
    }

    @Test
    @DisplayName("excluir deve deletar compensação quando encontrada")
    void excluir_quandoEncontrado_deveExcluir() {
        when(compensacaoRepository.findById(1L)).thenReturn(Optional.of(compensacao));

        compensacaoService.excluir(1L);

        verify(compensacaoRepository).delete(compensacao);
    }

    @Test
    @DisplayName("excluir deve lançar ResourceNotFoundException quando compensação não existe")
    void excluir_quandoNaoEncontrado_deveLancarExcecao() {
        when(compensacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compensacaoService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(compensacaoRepository, never()).delete(any());
    }
}
