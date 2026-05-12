package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.EmissaoRequestDTO;
import br.com.fiap.ecocompliance.dto.EmissaoResponseDTO;
import br.com.fiap.ecocompliance.service.EmissaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/emissoes")
@RequiredArgsConstructor
public class EmissaoController {

    private final EmissaoService emissaoService;

    @GetMapping
    public ResponseEntity<List<EmissaoResponseDTO>> listar() {
        return ResponseEntity.ok(emissaoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmissaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(emissaoService.buscarPorId(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<EmissaoResponseDTO>> buscarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(emissaoService.buscarPorEmpresa(empresaId));
    }

    @PostMapping
    public ResponseEntity<EmissaoResponseDTO> criar(@RequestBody @Valid EmissaoRequestDTO dto) {
        EmissaoResponseDTO criada = emissaoService.criar(dto);
        return ResponseEntity.created(URI.create("/emissoes/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmissaoResponseDTO> atualizar(@PathVariable Long id,
                                                         @RequestBody @Valid EmissaoRequestDTO dto) {
        return ResponseEntity.ok(emissaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        emissaoService.excluir(id);
    }
}
