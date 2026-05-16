package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.EmissaoRequestDTO;
import br.com.fiap.ecocompliance.dto.EmissaoResponseDTO;
import br.com.fiap.ecocompliance.entity.EmissaoCarbono;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.EmissaoCarbonoRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmissaoServiceTest {

    @Mock
    private EmissaoCarbonoRepository emissaoRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private EmissaoService emissaoService;

    private Empresa empresa;
    private EmissaoCarbono emissao;
    private EmissaoRequestDTO requestDTO;

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

        emissao = EmissaoCarbono.builder()
                .id(1L)
                .empresa(empresa)
                .quantidadeCo2(new BigDecimal("1250.50"))
                .fonteEmissao("Caldeiras industriais")
                .dataRegistro(LocalDate.now())
                .unidadeMedida("tCO2e")
                .build();

        requestDTO = new EmissaoRequestDTO(
                1L,
                new BigDecimal("1250.50"),
                "Caldeiras industriais",
                LocalDate.now(),
                "tCO2e"
        );
    }

    @Test
    @DisplayName("listar deve retornar lista de DTOs mapeados")
    void listar_deveRetornarListaDTOs() {
        when(emissaoRepository.findAll()).thenReturn(List.of(emissao));

        List<EmissaoResponseDTO> resultado = emissaoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).fonteEmissao()).isEqualTo("Caldeiras industriais");
        verify(emissaoRepository).findAll();
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando emissão existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(emissaoRepository.findById(1L)).thenReturn(Optional.of(emissao));

        EmissaoResponseDTO resultado = emissaoService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.empresaId()).isEqualTo(1L);
        assertThat(resultado.unidadeMedida()).isEqualTo("tCO2e");
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando emissão não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(emissaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emissaoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("buscarPorEmpresa deve validar empresa e retornar lista filtrada")
    void buscarPorEmpresa_deveRetornarEmissoesDaEmpresa() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.findByEmpresaId(1L)).thenReturn(List.of(emissao));

        List<EmissaoResponseDTO> resultado = emissaoService.buscarPorEmpresa(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).empresaNome()).isEqualTo("Verde Industria S.A.");
        verify(empresaService).buscarEntidade(1L);
    }

    @Test
    @DisplayName("criar deve salvar e retornar DTO quando dados válidos")
    void criar_quandoValido_deveRetornarDTO() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.save(any(EmissaoCarbono.class))).thenReturn(emissao);

        EmissaoResponseDTO resultado = emissaoService.criar(requestDTO);

        assertThat(resultado.quantidadeCo2()).isEqualByComparingTo("1250.50");
        verify(emissaoRepository).save(any(EmissaoCarbono.class));
    }

    @Test
    @DisplayName("criar deve lançar exceção quando empresa não encontrada")
    void criar_quandoEmpresaNaoEncontrada_deveLancarExcecao() {
        when(empresaService.buscarEntidade(1L))
                .thenThrow(new ResourceNotFoundException("Empresa não encontrada com ID: 1"));

        assertThatThrownBy(() -> emissaoService.criar(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emissaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve modificar campos e retornar DTO atualizado")
    void atualizar_quandoValido_deveRetornarDTO() {
        EmissaoRequestDTO dtoAtualizado = new EmissaoRequestDTO(
                1L, new BigDecimal("2000.00"), "Frota de veículos", LocalDate.now(), "tCO2e");

        EmissaoCarbono emissaoAtualizada = EmissaoCarbono.builder()
                .id(1L).empresa(empresa)
                .quantidadeCo2(new BigDecimal("2000.00"))
                .fonteEmissao("Frota de veículos")
                .dataRegistro(LocalDate.now())
                .unidadeMedida("tCO2e")
                .build();

        when(emissaoRepository.findById(1L)).thenReturn(Optional.of(emissao));
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(emissaoRepository.save(any(EmissaoCarbono.class))).thenReturn(emissaoAtualizada);

        EmissaoResponseDTO resultado = emissaoService.atualizar(1L, dtoAtualizado);

        assertThat(resultado.fonteEmissao()).isEqualTo("Frota de veículos");
        assertThat(resultado.quantidadeCo2()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("excluir deve deletar emissão quando encontrada")
    void excluir_quandoEncontrado_deveExcluir() {
        when(emissaoRepository.findById(1L)).thenReturn(Optional.of(emissao));

        emissaoService.excluir(1L);

        verify(emissaoRepository).delete(emissao);
    }
}
