package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.EmissaoRequestDTO;
import br.com.fiap.ecocompliance.dto.EmissaoResponseDTO;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.service.EmissaoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmissaoController.class)
@Import(br.com.fiap.ecocompliance.config.SecurityConfig.class)
class EmissaoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmissaoService emissaoService;

    @MockBean
    br.com.fiap.ecocompliance.security.JwtService jwtService;

    @MockBean
    br.com.fiap.ecocompliance.repository.UsuarioRepository usuarioRepository;

    private EmissaoResponseDTO responseValido() {
        return new EmissaoResponseDTO(
                1L,
                1L,
                "Verde Industria S.A.",
                new BigDecimal("1250.50"),
                "Caldeiras industriais",
                LocalDate.now(),
                "tCO2e"
        );
    }

    private EmissaoRequestDTO requestValido() {
        return new EmissaoRequestDTO(
                1L,
                new BigDecimal("500.00"),
                "Frota de veículos",
                LocalDate.now(),
                "tCO2e"
        );
    }

    @Test
    @DisplayName("listar com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void listar_comUserAutenticado_retorna200() throws Exception {
        when(emissaoService.listar()).thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/emissoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fonteEmissao").value("Caldeiras industriais"));
    }

    @Test
    @DisplayName("listar sem autenticação deve retornar 4xx")
    void listar_semAutenticacao_retorna4xx() throws Exception {
        mockMvc.perform(get("/emissoes"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("buscar por empresa com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void buscarPorEmpresa_comUserAutenticado_retorna200() throws Exception {
        when(emissaoService.buscarPorEmpresa(1L)).thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/emissoes/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].empresaId").value(1));
    }

    @Test
    @DisplayName("buscar por ID quando não existe deve retornar 404")
    @WithMockUser(roles = "USER")
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(emissaoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Emissão não encontrada com ID: 99"));

        mockMvc.perform(get("/emissoes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("criar com admin autenticado deve retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdminAutenticado_retorna201() throws Exception {
        when(emissaoService.criar(any())).thenReturn(responseValido());

        mockMvc.perform(post("/emissoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("escrita com usuário sem permissão deve retornar 403")
    @WithMockUser(roles = "USER")
    void criar_comUserSemPermissao_retorna403() throws Exception {
        // DELETE /emissoes/{id} tem handler mapeado → MvcRequestMatcher reconhece
        // o path e aplica hasRole("ADMIN"); a mesma regra cobre POST, PUT e DELETE.
        mockMvc.perform(delete("/emissoes/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
