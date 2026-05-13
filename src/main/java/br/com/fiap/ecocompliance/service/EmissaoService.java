package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.EmissaoRequestDTO;
import br.com.fiap.ecocompliance.dto.EmissaoResponseDTO;
import br.com.fiap.ecocompliance.entity.EmissaoCarbono;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.EmissaoCarbonoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmissaoService {

    private final EmissaoCarbonoRepository emissaoRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public List<EmissaoResponseDTO> listar() {
        return emissaoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmissaoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EmissaoResponseDTO> buscarPorEmpresa(Long empresaId) {
        empresaService.buscarEntidade(empresaId);
        return emissaoRepository.findByEmpresaId(empresaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EmissaoResponseDTO criar(EmissaoRequestDTO dto) {
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        EmissaoCarbono e = EmissaoCarbono.builder()
                .empresa(empresa)
                .quantidadeCo2(dto.quantidadeCo2())
                .fonteEmissao(dto.fonteEmissao())
                .dataRegistro(dto.dataRegistro())
                .unidadeMedida(dto.unidadeMedida())
                .build();

        return toResponse(emissaoRepository.save(e));
    }

    @Transactional
    public EmissaoResponseDTO atualizar(Long id, EmissaoRequestDTO dto) {
        EmissaoCarbono e = buscarEntidade(id);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        e.setEmpresa(empresa);
        e.setQuantidadeCo2(dto.quantidadeCo2());
        e.setFonteEmissao(dto.fonteEmissao());
        e.setDataRegistro(dto.dataRegistro());
        e.setUnidadeMedida(dto.unidadeMedida());

        return toResponse(emissaoRepository.save(e));
    }

    @Transactional
    public void excluir(Long id) {
        EmissaoCarbono e = buscarEntidade(id);
        emissaoRepository.delete(e);
    }

    private EmissaoCarbono buscarEntidade(Long id) {
        return emissaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emisso não encontrada com ID: " + id));
    }

    private EmissaoResponseDTO toResponse(EmissaoCarbono e) {
        return new EmissaoResponseDTO(
                e.getId(),
                e.getEmpresa().getId(),
                e.getEmpresa().getNome(),
                e.getQuantidadeCo2(),
                e.getFonteEmissao(),
                e.getDataRegistro(),
                e.getUnidadeMedida()
        );
    }
}
