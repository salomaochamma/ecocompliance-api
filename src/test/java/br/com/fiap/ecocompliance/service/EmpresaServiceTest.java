package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.EmpresaRequestDTO;
import br.com.fiap.ecocompliance.dto.EmpresaResponseDTO;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa empresa;
    private EmpresaRequestDTO requestDTO;

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

        requestDTO = new EmpresaRequestDTO(
                "Verde Industria S.A.",
                "12.345.678/0001-90",
                "Manufatura",
                "esg@verde.com.br"
        );
    }

    @Test
    @DisplayName("listar deve retornar lista de DTOs")
    void listar_deveRetornarListaDTOs() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<EmpresaResponseDTO> resultado = empresaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Verde Industria S.A.");
        verify(empresaRepository).findAll();
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando empresa existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO resultado = empresaService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.cnpj()).isEqualTo("12.345.678/0001-90");
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando empresa não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("criar deve retornar DTO quando CNPJ não está cadastrado")
    void criar_quandoCnpjNovoCadastrado_deveRetornarDTO() {
        when(empresaRepository.existsByCnpj(requestDTO.cnpj())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.criar(requestDTO);

        assertThat(resultado.nome()).isEqualTo("Verde Industria S.A.");
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    @DisplayName("criar deve lançar BusinessException quando CNPJ já cadastrado")
    void criar_quandoCnpjDuplicado_deveLancarBusinessException() {
        when(empresaRepository.existsByCnpj(requestDTO.cnpj())).thenReturn(true);

        assertThatThrownBy(() -> empresaService.criar(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(requestDTO.cnpj());

        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve retornar DTO quando dados válidos")
    void atualizar_quandoValido_deveRetornarDTO() {
        // CNPJ do request igual ao da entidade → existsByCnpj não é chamado
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.atualizar(1L, requestDTO);

        assertThat(resultado).isNotNull();
        verify(empresaRepository).save(any(Empresa.class));
        verify(empresaRepository, never()).existsByCnpj(any());
    }

    @Test
    @DisplayName("atualizar deve lançar BusinessException quando CNPJ pertence a outra empresa")
    void atualizar_quandoCnpjJaExisteEmOutraEmpresa_deveLancarBusinessException() {
        Empresa outroCnpj = Empresa.builder()
                .id(1L).cnpj("11.111.111/0001-11")
                .nome("x").setor("x").emailResponsavel("x@x.com")
                .createdAt(LocalDateTime.now()).build();

        EmpresaRequestDTO dtoNovoCnpj = new EmpresaRequestDTO(
                "Verde Industria S.A.", "99.999.999/0001-99", "Manufatura", "esg@verde.com.br");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(outroCnpj));
        when(empresaRepository.existsByCnpj("99.999.999/0001-99")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.atualizar(1L, dtoNovoCnpj))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("excluir deve deletar empresa quando encontrada")
    void excluir_quandoEncontrado_deveExcluir() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        empresaService.excluir(1L);

        verify(empresaRepository).delete(empresa);
    }

    @Test
    @DisplayName("excluir deve lançar ResourceNotFoundException quando empresa não existe")
    void excluir_quandoNaoEncontrado_deveLancarExcecao() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(empresaRepository, never()).delete(any());
    }
}
