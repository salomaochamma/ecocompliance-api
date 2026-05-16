package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.AgendamentoRequestDTO;
import br.com.fiap.ecocompliance.dto.AgendamentoResponseDTO;
import br.com.fiap.ecocompliance.entity.AgendamentoReducaoCarbono;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.AgendamentoReducaoCarbonoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoReducaoCarbonoRepository agendamentoRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listar() {
        return agendamentoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> buscarPorEmpresa(Long empresaId) {
        empresaService.buscarEntidade(empresaId);
        return agendamentoRepository.findByEmpresaId(empresaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> buscarPorStatus(StatusAgendamento status) {
        return agendamentoRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {
        validarDatas(dto);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        AgendamentoReducaoCarbono agendamento = AgendamentoReducaoCarbono.builder()
                .empresa(empresa)
                .titulo(dto.titulo())
                .descricao(dto.descricao())
                .tipoIniciativa(dto.tipoIniciativa())
                .metaCo2(dto.metaCo2())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .status(dto.status() != null ? dto.status() : StatusAgendamento.PENDENTE)
                .build();

        return toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponseDTO atualizar(Long id, AgendamentoRequestDTO dto) {
        AgendamentoReducaoCarbono agendamento = buscarEntidade(id);

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO
                || agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Não é possível editar um agendamento com status " + agendamento.getStatus());
        }

        validarDatas(dto);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        agendamento.setEmpresa(empresa);
        agendamento.setTitulo(dto.titulo());
        agendamento.setDescricao(dto.descricao());
        agendamento.setTipoIniciativa(dto.tipoIniciativa());
        agendamento.setMetaCo2(dto.metaCo2());
        agendamento.setDataInicio(dto.dataInicio());
        agendamento.setDataFim(dto.dataFim());
        if (dto.status() != null) agendamento.setStatus(dto.status());

        return toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public void excluir(Long id) {
        AgendamentoReducaoCarbono agendamento = buscarEntidade(id);
        agendamentoRepository.delete(agendamento);
    }

    AgendamentoReducaoCarbono buscarEntidade(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
    }

    private void validarDatas(AgendamentoRequestDTO dto) {
        if (dto.dataFim() != null && dto.dataInicio() != null
                && !dto.dataFim().isAfter(dto.dataInicio())) {
            throw new BusinessException("A data de fim deve ser posterior à data de início");
        }
    }

    private AgendamentoResponseDTO toResponse(AgendamentoReducaoCarbono a) {
        return new AgendamentoResponseDTO(
                a.getId(),
                a.getEmpresa().getId(),
                a.getEmpresa().getNome(),
                a.getTitulo(),
                a.getDescricao(),
                a.getTipoIniciativa(),
                a.getMetaCo2(),
                a.getDataInicio(),
                a.getDataFim(),
                a.getStatus(),
                a.getDataCriacao()
        );
    }
}
