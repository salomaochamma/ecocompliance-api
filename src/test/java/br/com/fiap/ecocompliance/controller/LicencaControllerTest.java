package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.LicencaRequestDTO;
import br.com.fiap.ecocompliance.dto.LicencaResponseDTO;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.service.LicencaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LicencaController.class)
@Import(br.com.fiap.ecocompliance.config.SecurityConfig.class)
class LicencaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean LicencaService licencaService;
    @MockBean br.com.fiap.ecocompliance.security.JwtService jwtService;
    @MockBean br.com.fiap.ecocompliance.repository.UsuarioRepository usuarioRepository;

    private static final LocalDate EMISSAO = LocalDate.now().minusYears(1);
    private static final LocalDate VALIDADE = LocalDate.now().plusYears(2);

    private LicencaResponseDTO responseValido() {
        return new LicencaResponseDTO(
                1L, 1L, "Logistica Sustentavel Brasil",
                "Licença de Operação", "LO-2023-001",
                EMISSAO, VALIDADE, StatusLicenca.ATIVA, "IBAMA");
    }

    private LicencaRequestDTO requestValido() {
        return new LicencaRequestDTO(
                1L, "Licença de Operação", "LO-2023-001",
                EMISSAO, VALIDADE, StatusLicenca.ATIVA, "IBAMA");
    }

    @Test
    @DisplayName("listar com usuário autenticado deve retornar 200 com página")
    @WithMockUser(roles = "USER")
    void listar_comUserAutenticado_retorna200() throws Exception {
        when(licencaService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseValido())));

        mockMvc.perform(get("/licencas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].numeroLicenca").value("LO-2023-001"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVA"));
    }

    @Test
    @DisplayName("listar sem autenticação deve retornar 4xx")
    void listar_semAutenticacao_retorna4xx() throws Exception {
        mockMvc.perform(get("/licencas"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("licencasVencendo com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void licencasVencendo_comUserAutenticado_retorna200() throws Exception {
        LicencaResponseDTO vencendo = new LicencaResponseDTO(
                2L, 1L, "Logistica Sustentavel Brasil",
                "Licença Prévia", "LP-2024-002",
                LocalDate.now().minusMonths(6), LocalDate.now().plusDays(15),
                StatusLicenca.ATIVA, "CETESB");
        when(licencaService.licencasVencendo()).thenReturn(List.of(vencendo));

        mockMvc.perform(get("/licencas/vencendo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].numeroLicenca").value("LP-2024-002"));
    }

    @Test
    @DisplayName("buscar por ID com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void buscarPorId_comUserAutenticado_retorna200() throws Exception {
        when(licencaService.buscarPorId(1L)).thenReturn(responseValido());

        mockMvc.perform(get("/licencas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orgaoEmissor").value("IBAMA"));
    }

    @Test
    @DisplayName("buscar por ID quando não existe deve retornar 404")
    @WithMockUser(roles = "USER")
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(licencaService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Licença não encontrada com ID: 99"));

        mockMvc.perform(get("/licencas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("criar com admin autenticado deve retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdminAutenticado_retorna201() throws Exception {
        when(licencaService.criar(any())).thenReturn(responseValido());

        mockMvc.perform(post("/licencas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("escrita com usuário sem permissão deve retornar 403")
    @WithMockUser(roles = "USER")
    void criar_comUserSemPermissao_retorna403() throws Exception {
        mockMvc.perform(delete("/licencas/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("criar com datas inválidas deve retornar 400")
    @WithMockUser(roles = "ADMIN")
    void criar_quandoDatasInvalidas_retorna400() throws Exception {
        when(licencaService.criar(any()))
                .thenThrow(new BusinessException("Data de validade não pode ser anterior à data de emissão"));

        LicencaRequestDTO invalido = new LicencaRequestDTO(
                1L, "Licença de Operação", "LO-2023-003",
                LocalDate.now(), LocalDate.now().minusDays(1),
                StatusLicenca.ATIVA, "IBAMA");

        mockMvc.perform(post("/licencas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("excluir com admin autenticado deve retornar 204")
    @WithMockUser(roles = "ADMIN")
    void excluir_comAdminAutenticado_retorna204() throws Exception {
        doNothing().when(licencaService).excluir(1L);

        mockMvc.perform(delete("/licencas/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
