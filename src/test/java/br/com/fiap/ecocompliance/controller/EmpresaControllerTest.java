package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.EmpresaRequestDTO;
import br.com.fiap.ecocompliance.dto.EmpresaResponseDTO;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.service.EmpresaService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpresaController.class)
@Import(br.com.fiap.ecocompliance.config.SecurityConfig.class)
class EmpresaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmpresaService empresaService;

    @MockBean
    br.com.fiap.ecocompliance.security.JwtService jwtService;

    @MockBean
    br.com.fiap.ecocompliance.repository.UsuarioRepository usuarioRepository;

    private EmpresaResponseDTO responseValido() {
        return new EmpresaResponseDTO(
                1L,
                "Verde Industria S.A.",
                "12.345.678/0001-90",
                "Manufatura",
                "esg@verde.com.br",
                LocalDateTime.now()
        );
    }

    private EmpresaRequestDTO requestValido() {
        return new EmpresaRequestDTO(
                "Nova Empresa",
                "99.888.777/0001-66",
                "Tecnologia",
                "esg@nova.com"
        );
    }

    @Test
    @DisplayName("listar com usuário autenticado deve retornar 200 com página")
    @WithMockUser(roles = "USER")
    void listar_comUserAutenticado_retorna200ComPagina() throws Exception {
        when(empresaService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseValido())));

        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nome").value("Verde Industria S.A."));
    }

    @Test
    @DisplayName("listar sem autenticação deve retornar 4xx")
    void listar_semAutenticacao_retorna403() throws Exception {
        mockMvc.perform(get("/empresas"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("buscar por ID com usuário autenticado deve retornar 200")
    @WithMockUser(roles = "USER")
    void buscarPorId_comUserAutenticado_retorna200() throws Exception {
        when(empresaService.buscarPorId(1L)).thenReturn(responseValido());

        mockMvc.perform(get("/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"));
    }

    @Test
    @DisplayName("buscar por ID quando não existe deve retornar 404")
    @WithMockUser(roles = "USER")
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(empresaService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Empresa não encontrada com ID: 99"));

        mockMvc.perform(get("/empresas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("criar com admin autenticado deve retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar_comAdminAutenticado_retorna201() throws Exception {
        EmpresaResponseDTO criada = new EmpresaResponseDTO(
                2L, "Nova Empresa", "99.888.777/0001-66",
                "Tecnologia", "esg@nova.com", LocalDateTime.now()
        );
        when(empresaService.criar(any())).thenReturn(criada);

        mockMvc.perform(post("/empresas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("escrita com usuário sem permissão deve retornar 403")
    @WithMockUser(roles = "USER")
    void criar_comUserSemPermissao_retorna403() throws Exception {
        // DELETE /empresas/{id} tem handler mapeado → MvcRequestMatcher reconhece
        // o path e aplica hasRole("ADMIN"); a mesma regra cobre POST, PUT e DELETE.
        mockMvc.perform(delete("/empresas/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
