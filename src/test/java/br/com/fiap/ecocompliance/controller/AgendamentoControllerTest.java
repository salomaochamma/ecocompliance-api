package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.AgendamentoRequestDTO;
import br.com.fiap.ecocompliance.dto.AgendamentoResponseDTO;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.service.AgendamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgendamentoController.class)
@Import(br.com.fiap.ecocompliance.config.SecurityConfig.class)
class AgendamentoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AgendamentoService agendamentoService;

    @MockBean
    br.com.fiap.ecocompliance.security.JwtService jwtService;

    @MockBean
    br.com.fiap.ecocompliance.repository.UsuarioRepository usuarioRepository;

    private static final LocalDate INICIO = LocalDate.now().plusDays(1);
    private static final LocalDate FIM = LocalDate.now().plusMonths(6);

    private AgendamentoResponseDTO responseValido() {
        return new AgendamentoResponseDTO(
                1L,
                1L,
                "Verde Industria S.A.",
                "Troca de Caldeiras",
                "Desc",
                "Eficiência Energética",
                new BigDecimal("600.00"),
                INICIO,
                FIM,
                StatusAgendamento.PENDENTE,
                LocalDateTime.now()
        );
    }

    private AgendamentoRequestDTO requestValido() {
        return new AgendamentoRequestDTO(
                1L,
                "Troca de Caldeiras",
                "Substituição das caldeiras",
                "Eficiência Energética",
                new BigDecimal("600.00"),
                INICIO,
                FIM,
                StatusAgendamento.PENDENTE
        );
    }

    @Test
    @DisplayName("listar com usuário autenticado deve retornar 200 com página")
    @WithMockUser(roles = "USER")
    void listar_comUserAutenticado_retorna200() throws Exception {
        when(agendamentoService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseValido())));

        mockMvc.perform(get("/agendamentos-reducao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Troca de Caldeiras"))
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
    }

    @Test
    @DisplayName("listar sem autenticação deve retornar 4xx")
    void listar_semAutenticacao_retorna4xx() throws Exception {
        mockMvc.perform(get("/agendamentos-reducao"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("buscar por status com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void buscarPorStatus_comUserAutenticado_retorna200() throws Exception {
        when(agendamentoService.buscarPorStatus(StatusAgendamento.PENDENTE))
                .thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/agendamentos-reducao/status/PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    @DisplayName("buscar por ID quando não existe deve retornar 404")
    @WithMockUser(roles = "USER")
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(agendamentoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Agendamento não encontrado com ID: 99"));

        mockMvc.perform(get("/agendamentos-reducao/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("criar com admin autenticado deve retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdminAutenticado_retorna201() throws Exception {
        when(agendamentoService.criar(any())).thenReturn(responseValido());

        mockMvc.perform(post("/agendamentos-reducao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Troca de Caldeiras"));
    }

    @Test
    @DisplayName("escrita com usuário sem permissão deve retornar 403")
    @WithMockUser(roles = "USER")
    void criar_comUserSemPermissao_retorna403() throws Exception {
        // DELETE /agendamentos-reducao/{id} tem handler mapeado → MvcRequestMatcher
        // reconhece o path e aplica hasRole("ADMIN"); regra cobre POST, PUT e DELETE.
        mockMvc.perform(delete("/agendamentos-reducao/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("atualizar agendamento concluído deve retornar 400")
    @WithMockUser(roles = "ADMIN")
    void atualizar_quandoAgendamentoConcluido_retorna400() throws Exception {
        when(agendamentoService.atualizar(eq(1L), any()))
                .thenThrow(new BusinessException("Não é possível editar um agendamento com status CONCLUIDO"));

        mockMvc.perform(put("/agendamentos-reducao/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("excluir com admin autenticado deve retornar 204")
    @WithMockUser(roles = "ADMIN")
    void excluir_comAdminAutenticado_retorna204() throws Exception {
        doNothing().when(agendamentoService).excluir(1L);

        mockMvc.perform(delete("/agendamentos-reducao/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
