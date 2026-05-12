package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.RelatorioEmpresaDTO;
import br.com.fiap.ecocompliance.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<RelatorioEmpresaDTO> relatorioPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioEmpresa(empresaId));
    }
}
