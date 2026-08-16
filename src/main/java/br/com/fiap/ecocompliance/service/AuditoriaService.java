package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.AuditoriaRequestDTO;
import br.com.fiap.ecocompliance.dto.AuditoriaResponseDTO;
import br.com.fiap.ecocompliance.entity.AuditoriaAmbiental;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.AuditoriaAmbientalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaAmbientalRepository auditoriaRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDTO> listar(Pageable pageable) {
        return auditoriaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditoriaResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponseDTO> buscarPorRisco(NivelRisco nivel) {
        return auditoriaRepository.findByNivelRisco(nivel).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AuditoriaResponseDTO criar(AuditoriaRequestDTO dto) {
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        AuditoriaAmbiental aud = AuditoriaAmbiental.builder()
                .empresa(empresa)
                .dataAuditoria(dto.dataAuditoria())
                .resultado(dto.resultado())
                .observacoes(dto.observacoes())
                .nivelRisco(dto.nivelRisco())
                .build();

        return toResponse(auditoriaRepository.save(aud));
    }

    @Transactional
    public AuditoriaResponseDTO atualizar(Long id, AuditoriaRequestDTO dto) {
        AuditoriaAmbiental aud = buscarEntidade(id);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        aud.setEmpresa(empresa);
        aud.setDataAuditoria(dto.dataAuditoria());
        aud.setResultado(dto.resultado());
        aud.setObservacoes(dto.observacoes());
        aud.setNivelRisco(dto.nivelRisco());

        return toResponse(auditoriaRepository.save(aud));
    }

    @Transactional
    public void excluir(Long id) {
        AuditoriaAmbiental aud = buscarEntidade(id);
        auditoriaRepository.delete(aud);
    }

    private AuditoriaAmbiental buscarEntidade(Long id) {
        return auditoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auditoria não encontrada com ID: " + id));
    }

    private AuditoriaResponseDTO toResponse(AuditoriaAmbiental a) {
        return new AuditoriaResponseDTO(
                a.getId(),
                a.getEmpresa().getId(),
                a.getEmpresa().getNome(),
                a.getDataAuditoria(),
                a.getResultado(),
                a.getObservacoes(),
                a.getNivelRisco()
        );
    }
}
