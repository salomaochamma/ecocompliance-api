package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.AuditoriaRequestDTO;
import br.com.fiap.ecocompliance.dto.AuditoriaResponseDTO;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.service.AuditoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaResponseDTO>> listar() {
        return ResponseEntity.ok(auditoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarPorId(id));
    }

    @GetMapping("/risco/{nivelRisco}")
    public ResponseEntity<List<AuditoriaResponseDTO>> buscarPorRisco(@PathVariable NivelRisco nivelRisco) {
        return ResponseEntity.ok(auditoriaService.buscarPorRisco(nivelRisco));
    }

    @PostMapping
    public ResponseEntity<AuditoriaResponseDTO> criar(@RequestBody @Valid AuditoriaRequestDTO dto) {
        AuditoriaResponseDTO criada = auditoriaService.criar(dto);
        return ResponseEntity.created(URI.create("/auditorias/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditoriaResponseDTO> atualizar(@PathVariable Long id,
                                                           @RequestBody @Valid AuditoriaRequestDTO dto) {
        return ResponseEntity.ok(auditoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        auditoriaService.excluir(id);
    }
}
