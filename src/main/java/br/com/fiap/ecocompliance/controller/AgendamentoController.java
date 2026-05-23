package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.AgendamentoRequestDTO;
import br.com.fiap.ecocompliance.dto.AgendamentoResponseDTO;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import br.com.fiap.ecocompliance.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/agendamentos-reducao")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @GetMapping
    public ResponseEntity<Page<AgendamentoResponseDTO>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(agendamentoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(agendamentoService.buscarPorEmpresa(empresaId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarPorStatus(@PathVariable StatusAgendamento status) {
        return ResponseEntity.ok(agendamentoService.buscarPorStatus(status));
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criar(@RequestBody @Valid AgendamentoRequestDTO dto) {
        AgendamentoResponseDTO criado = agendamentoService.criar(dto);
        return ResponseEntity.created(URI.create("/agendamentos-reducao/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody @Valid AgendamentoRequestDTO dto) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        agendamentoService.excluir(id);
    }
}
