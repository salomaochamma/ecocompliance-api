package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.CompensacaoRequestDTO;
import br.com.fiap.ecocompliance.dto.CompensacaoResponseDTO;
import br.com.fiap.ecocompliance.entity.CompensacaoAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.CompensacaoAmbientalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompensacaoService {

    private final CompensacaoAmbientalRepository compensacaoRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public Page<CompensacaoResponseDTO> listar(Pageable pageable) {
        return compensacaoRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CompensacaoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public CompensacaoResponseDTO criar(CompensacaoRequestDTO dto) {
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        CompensacaoAmbiental c = CompensacaoAmbiental.builder()
                .empresa(empresa)
                .tipoCompensacao(dto.tipoCompensacao())
                .descricao(dto.descricao())
                .quantidadeCompensada(dto.quantidadeCompensada())
                .dataAcao(dto.dataAcao())
                .build();

        return toResponse(compensacaoRepository.save(c));
    }

    @Transactional
    public CompensacaoResponseDTO atualizar(Long id, CompensacaoRequestDTO dto) {
        CompensacaoAmbiental c = buscarEntidade(id);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        c.setEmpresa(empresa);
        c.setTipoCompensacao(dto.tipoCompensacao());
        c.setDescricao(dto.descricao());
        c.setQuantidadeCompensada(dto.quantidadeCompensada());
        c.setDataAcao(dto.dataAcao());

        return toResponse(compensacaoRepository.save(c));
    }

    @Transactional
    public void excluir(Long id) {
        CompensacaoAmbiental c = buscarEntidade(id);
        compensacaoRepository.delete(c);
    }

    private CompensacaoAmbiental buscarEntidade(Long id) {
        return compensacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compensao não encontrada com ID: " + id));
    }

    private CompensacaoResponseDTO toResponse(CompensacaoAmbiental c) {
        return new CompensacaoResponseDTO(
                c.getId(),
                c.getEmpresa().getId(),
                c.getEmpresa().getNome(),
                c.getTipoCompensacao(),
                c.getDescricao(),
                c.getQuantidadeCompensada(),
                c.getDataAcao()
        );
    }
}
