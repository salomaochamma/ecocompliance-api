package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.LicencaRequestDTO;
import br.com.fiap.ecocompliance.dto.LicencaResponseDTO;
import br.com.fiap.ecocompliance.service.LicencaService;
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
@RequestMapping("/licencas")
@RequiredArgsConstructor
public class LicencaController {

    private final LicencaService licencaService;

    @GetMapping
    public ResponseEntity<Page<LicencaResponseDTO>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(licencaService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicencaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(licencaService.buscarPorId(id));
    }

    @GetMapping("/vencendo")
    public ResponseEntity<List<LicencaResponseDTO>> licencasVencendo() {
        return ResponseEntity.ok(licencaService.licencasVencendo());
    }

    @PostMapping
    public ResponseEntity<LicencaResponseDTO> criar(@RequestBody @Valid LicencaRequestDTO dto) {
        LicencaResponseDTO criada = licencaService.criar(dto);
        return ResponseEntity.created(URI.create("/licencas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LicencaResponseDTO> atualizar(@PathVariable Long id,
                                                         @RequestBody @Valid LicencaRequestDTO dto) {
        return ResponseEntity.ok(licencaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        licencaService.excluir(id);
    }
}
