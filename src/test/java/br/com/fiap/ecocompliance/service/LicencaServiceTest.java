package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.LicencaRequestDTO;
import br.com.fiap.ecocompliance.dto.LicencaResponseDTO;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.LicencaAmbiental;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.LicencaAmbientalRepository;
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
class LicencaServiceTest {

    @Mock
    private LicencaAmbientalRepository licencaRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private LicencaService licencaService;

    private static final LocalDate EMISSAO = LocalDate.now().minusYears(1);
    private static final LocalDate VALIDADE = LocalDate.now().plusYears(2);

    private Empresa empresa;
    private LicencaAmbiental licenca;
    private LicencaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("Logistica Sustentavel Brasil")
                .cnpj("34.567.890/0001-11")
                .setor("Logística")
                .emailResponsavel("esg@logistica.com.br")
                .createdAt(LocalDateTime.now())
                .build();

        licenca = LicencaAmbiental.builder()
                .id(1L)
                .empresa(empresa)
                .tipoLicenca("Licença de Operação")
                .numeroLicenca("LO-2023-001")
                .dataEmissao(EMISSAO)
                .dataValidade(VALIDADE)
                .status(StatusLicenca.ATIVA)
                .orgaoEmissor("IBAMA")
                .build();

        requestDTO = new LicencaRequestDTO(
                1L,
                "Licença de Operação",
                "LO-2023-001",
                EMISSAO,
                VALIDADE,
                StatusLicenca.ATIVA,
                "IBAMA"
        );
    }

    @Test
    @DisplayName("listar deve retornar página de DTOs mapeados")
    void listar_deveRetornarPaginaDTOs() {
        when(licencaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(licenca)));

        Page<LicencaResponseDTO> resultado = licencaService.listar(Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).numeroLicenca()).isEqualTo("LO-2023-001");
        verify(licencaRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarPorId deve retornar DTO quando licença existe")
    void buscarPorId_quandoEncontrado_deveRetornarDTO() {
        when(licencaRepository.findById(1L)).thenReturn(Optional.of(licenca));

        LicencaResponseDTO resultado = licencaService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.status()).isEqualTo(StatusLicenca.ATIVA);
        assertThat(resultado.orgaoEmissor()).isEqualTo("IBAMA");
    }

    @Test
    @DisplayName("buscarPorId deve lançar ResourceNotFoundException quando licença não existe")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(licencaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licencaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("licencasVencendo deve retornar licenças com vencimento nos próximos 30 dias")
    void licencasVencendo_deveRetornarListaFiltrada() {
        LicencaAmbiental vencendo = LicencaAmbiental.builder()
                .id(2L).empresa(empresa)
                .tipoLicenca("Licença Prévia").numeroLicenca("LP-2024-002")
                .dataEmissao(LocalDate.now().minusMonths(6))
                .dataValidade(LocalDate.now().plusDays(15))
                .status(StatusLicenca.ATIVA).orgaoEmissor("CETESB")
                .build();

        when(licencaRepository.findVencendo(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(vencendo));

        List<LicencaResponseDTO> resultado = licencaService.licencasVencendo();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).numeroLicenca()).isEqualTo("LP-2024-002");
        verify(licencaRepository).findVencendo(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("criar deve salvar e retornar DTO quando datas válidas")
    void criar_quandoValido_deveRetornarDTO() {
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(licencaRepository.save(any(LicencaAmbiental.class))).thenReturn(licenca);

        LicencaResponseDTO resultado = licencaService.criar(requestDTO);

        assertThat(resultado.tipoLicenca()).isEqualTo("Licença de Operação");
        assertThat(resultado.status()).isEqualTo(StatusLicenca.ATIVA);
        verify(licencaRepository).save(any(LicencaAmbiental.class));
    }

    @Test
    @DisplayName("criar deve lançar BusinessException quando dataValidade é anterior à dataEmissao")
    void criar_quandoDataValidadeAnteriorAEmissao_deveLancarBusinessException() {
        LicencaRequestDTO dtoInvalido = new LicencaRequestDTO(
                1L, "Licença de Operação", "LO-2023-002",
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                StatusLicenca.ATIVA, "IBAMA"
        );

        assertThatThrownBy(() -> licencaService.criar(dtoInvalido))
                .isInstanceOf(BusinessException.class);

        verify(licencaRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve modificar campos e retornar DTO atualizado")
    void atualizar_quandoValido_deveRetornarDTO() {
        LicencaRequestDTO dtoAtualizado = new LicencaRequestDTO(
                1L, "Licença de Instalação", "LI-2024-001",
                EMISSAO, VALIDADE, StatusLicenca.SUSPENSA, "IBAMA");

        LicencaAmbiental atualizada = LicencaAmbiental.builder()
                .id(1L).empresa(empresa)
                .tipoLicenca("Licença de Instalação").numeroLicenca("LI-2024-001")
                .dataEmissao(EMISSAO).dataValidade(VALIDADE)
                .status(StatusLicenca.SUSPENSA).orgaoEmissor("IBAMA")
                .build();

        when(licencaRepository.findById(1L)).thenReturn(Optional.of(licenca));
        when(empresaService.buscarEntidade(1L)).thenReturn(empresa);
        when(licencaRepository.save(any(LicencaAmbiental.class))).thenReturn(atualizada);

        LicencaResponseDTO resultado = licencaService.atualizar(1L, dtoAtualizado);

        assertThat(resultado.tipoLicenca()).isEqualTo("Licença de Instalação");
        assertThat(resultado.status()).isEqualTo(StatusLicenca.SUSPENSA);
        verify(licencaRepository).save(any(LicencaAmbiental.class));
    }

    @Test
    @DisplayName("excluir deve deletar licença quando encontrada")
    void excluir_quandoEncontrado_deveExcluir() {
        when(licencaRepository.findById(1L)).thenReturn(Optional.of(licenca));

        licencaService.excluir(1L);

        verify(licencaRepository).delete(licenca);
    }

    @Test
    @DisplayName("excluir deve lançar ResourceNotFoundException quando licença não existe")
    void excluir_quandoNaoEncontrado_deveLancarExcecao() {
        when(licencaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licencaService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(licencaRepository, never()).delete(any());
    }
}
