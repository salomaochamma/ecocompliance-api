package br.com.fiap.ecocompliance.controller;

import br.com.fiap.ecocompliance.dto.IndicadoresSustentabilidadeDTO;
import br.com.fiap.ecocompliance.service.IndicadoresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/indicadores-sustentabilidade")
@RequiredArgsConstructor
public class IndicadoresController {

    private final IndicadoresService indicadoresService;

    @GetMapping
    public ResponseEntity<IndicadoresSustentabilidadeDTO> indicadores() {
        return ResponseEntity.ok(indicadoresService.calcular());
    }
}
