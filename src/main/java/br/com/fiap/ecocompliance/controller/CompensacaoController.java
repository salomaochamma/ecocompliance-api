package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.CompensacaoRequestDTO;
import br.com.fiap.ecocompliance.dto.CompensacaoResponseDTO;
import br.com.fiap.ecocompliance.service.CompensacaoService;
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
@RequestMapping("/compensacoes")
@RequiredArgsConstructor
public class CompensacaoController {

    private final CompensacaoService compensacaoService;

    @GetMapping
    public ResponseEntity<Page<CompensacaoResponseDTO>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(compensacaoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompensacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compensacaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompensacaoResponseDTO> criar(@RequestBody @Valid CompensacaoRequestDTO dto) {
        CompensacaoResponseDTO criada = compensacaoService.criar(dto);
        return ResponseEntity.created(URI.create("/compensacoes/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompensacaoResponseDTO> atualizar(@PathVariable Long id,
                                                             @RequestBody @Valid CompensacaoRequestDTO dto) {
        return ResponseEntity.ok(compensacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        compensacaoService.excluir(id);
    }
}
