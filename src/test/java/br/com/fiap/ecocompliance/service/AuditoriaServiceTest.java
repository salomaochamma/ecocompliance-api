package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.AuditoriaRequestDTO;
import br.com.fiap.ecocompliance.dto.AuditoriaResponseDTO;
import br.com.fiap.ecocompliance.entity.AuditoriaAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.AuditoriaAmbientalRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaAmbientalRepository auditoriaRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Empresa empresa;
    private AuditoriaAmbiental auditoria;
    private AuditoriaRequestDTO requestDTO;

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

        auditoria = AuditoriaAmbiental.builder()
                .id(1L)
                .empresa(empresa)
                .dataAuditoria(LocalDate.now())
                .resultado("Conformidade parcial identificada")
                .observacoes("Necessário ajuste nos filtros")
                .nivelRisco(NivelRisco.MEDIO)
                .build();

        requestDTO = new AuditoriaRequestDTO(
                1L,
                LocalDate.now(),
                "Conformidade parcial identificada",
                "Necessário ajuste nos filtros",
                NivelRisco.MEDIO
        );
    }

    @Test
    @DisplayName("listar deve retornar página de DTOs mapeados")
    void listar_deveRetornarPaginaDTOs() {
        when(auditoriaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(auditoria)));

        Page<AuditoriaResponseDTO> resultado = auditoriaService.listar(Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).resultado()).isEqualTo("Conformidade parcial identificada");
        verify(auditoriaRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando auditoria existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(auditoriaRepository.findById(1L)).thenReturn(Optional.of(auditoria));

        AuditoriaResponseDTO resultado = auditoriaService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nivelRisco()).isEqualTo(NivelRisco.MEDIO);
        assertThat(resultado.empresaNome()).isEqualTo("Verde Industria S.A.");
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando auditoria não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(auditoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("buscarPorRisco deve retornar auditorias filtradas pelo nível de risco")
    void buscarPorRisco_deveRetornarListaFiltrada() {
        when(auditoriaRepository.findByNivelRisco(NivelRisco.MEDIO)).thenReturn(List.of(auditoria));

        List<AuditoriaResponseDTO> resultado = auditoriaService.buscarPorRisco(NivelRisco.MEDIO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nivelRisco()).isEqualTo(NivelRisco.MEDIO);
        verify(auditoriaRepository).findByNivelRisco(NivelRisco.MEDIO);
    }

    @Test
    @DisplayName("criar deve salvar e retornar DTO quando dados válidos")
    void criar_quandoValido_deveRetornarDTO() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(auditoriaRepository.save(any(AuditoriaAmbiental.class))).thenReturn(auditoria);

        AuditoriaResponseDTO resultado = auditoriaService.criar(requestDTO);

        assertThat(resultado.resultado()).isEqualTo("Conformidade parcial identificada");
        assertThat(resultado.nivelRisco()).isEqualTo(NivelRisco.MEDIO);
        verify(auditoriaRepository).save(any(AuditoriaAmbiental.class));
    }

    @Test
    @DisplayName("criar deve lançar exceção quando empresa não encontrada")
    void criar_quandoEmpresaNaoEncontrada_deveLancarExcecao() {
        when(empresaService.buscarEntidade(1L))
                .thenThrow(new ResourceNotFoundException("Empresa não encontrada com ID: 1"));

        assertThatThrownBy(() -> auditoriaService.criar(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(auditoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve modificar campos e retornar DTO atualizado")
    void atualizar_quandoValido_deveRetornarDTO() {
        AuditoriaRequestDTO dtoAtualizado = new AuditoriaRequestDTO(
                1L, LocalDate.now(), "Auditoria revisada", null, NivelRisco.ALTO);

        AuditoriaAmbiental atualizada = AuditoriaAmbiental.builder()
                .id(1L).empresa(empresa)
                .dataAuditoria(LocalDate.now())
                .resultado("Auditoria revisada")
                .nivelRisco(NivelRisco.ALTO)
                .build();

        when(auditoriaRepository.findById(1L)).thenReturn(Optional.of(auditoria));
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(auditoriaRepository.save(any(AuditoriaAmbiental.class))).thenReturn(atualizada);

        AuditoriaResponseDTO resultado = auditoriaService.atualizar(1L, dtoAtualizado);

        assertThat(resultado.resultado()).isEqualTo("Auditoria revisada");
        assertThat(resultado.nivelRisco()).isEqualTo(NivelRisco.ALTO);
        verify(auditoriaRepository).save(any(AuditoriaAmbiental.class));
    }

    @Test
    @DisplayName("atualizar deve lançar ResourceNotFoundException quando auditoria não existe")
    void atualizar_quandoNaoEncontrado_deveLancarExcecao() {
        when(auditoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriaService.atualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(auditoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("excluir deve deletar auditoria quando encontrada")
    void excluir_quandoEncontrado_deveExcluir() {
        when(auditoriaRepository.findById(1L)).thenReturn(Optional.of(auditoria));

        auditoriaService.excluir(1L);

        verify(auditoriaRepository).delete(auditoria);
    }

    @Test
    @DisplayName("excluir deve lançar ResourceNotFoundException quando auditoria não existe")
    void excluir_quandoNaoEncontrado_deveLancarExcecao() {
        when(auditoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriaService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(auditoriaRepository, never()).delete(any());
    }
}
