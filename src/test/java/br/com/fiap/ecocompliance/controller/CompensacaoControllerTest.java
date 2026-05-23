package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.CompensacaoRequestDTO;
import br.com.fiap.ecocompliance.dto.CompensacaoResponseDTO;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.service.CompensacaoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompensacaoController.class)
@Import(br.com.fiap.ecocompliance.config.SecurityConfig.class)
class CompensacaoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CompensacaoService compensacaoService;
    @MockBean br.com.fiap.ecocompliance.security.JwtService jwtService;
    @MockBean br.com.fiap.ecocompliance.repository.UsuarioRepository usuarioRepository;

    private CompensacaoResponseDTO responseValido() {
        return new CompensacaoResponseDTO(
                1L, 1L, "EcoEnergia Renovaveis Ltda",
                "Reflorestamento", "Plantio de 5000 árvores",
                new BigDecimal("320.50"), LocalDate.now());
    }

    private CompensacaoRequestDTO requestValido() {
        return new CompensacaoRequestDTO(
                1L, "Reflorestamento", "Plantio de 5000 árvores",
                new BigDecimal("320.50"), LocalDate.now());
    }

    @Test
    @DisplayName("listar com usuário autenticado deve retornar 200 com página")
    @WithMockUser(roles = "USER")
    void listar_comUserAutenticado_retorna200() throws Exception {
        when(compensacaoService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseValido())));

        mockMvc.perform(get("/compensacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].tipoCompensacao").value("Reflorestamento"));
    }

    @Test
    @DisplayName("listar sem autenticação deve retornar 4xx")
    void listar_semAutenticacao_retorna4xx() throws Exception {
        mockMvc.perform(get("/compensacoes"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("buscar por ID com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void buscarPorId_comUserAutenticado_retorna200() throws Exception {
        when(compensacaoService.buscarPorId(1L)).thenReturn(responseValido());

        mockMvc.perform(get("/compensacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoCompensacao").value("Reflorestamento"));
    }

    @Test
    @DisplayName("buscar por ID quando não existe deve retornar 404")
    @WithMockUser(roles = "USER")
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(compensacaoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Compensação não encontrada com ID: 99"));

        mockMvc.perform(get("/compensacoes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("criar com admin autenticado deve retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdminAutenticado_retorna201() throws Exception {
        when(compensacaoService.criar(any())).thenReturn(responseValido());

        mockMvc.perform(post("/compensacoes")
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
        mockMvc.perform(delete("/compensacoes/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("atualizar com admin autenticado deve retornar 200")
    @WithMockUser(roles = "ADMIN")
    void atualizar_comAdminAutenticado_retorna200() throws Exception {
        when(compensacaoService.atualizar(eq(1L), any())).thenReturn(responseValido());

        mockMvc.perform(put("/compensacoes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("excluir com admin autenticado deve retornar 204")
    @WithMockUser(roles = "ADMIN")
    void excluir_comAdminAutenticado_retorna204() throws Exception {
        doNothing().when(compensacaoService).excluir(1L);

        mockMvc.perform(delete("/compensacoes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
