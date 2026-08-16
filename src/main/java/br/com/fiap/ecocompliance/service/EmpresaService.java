package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.EmpresaRequestDTO;
import br.com.fiap.ecocompliance.dto.EmpresaResponseDTO;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public Page<EmpresaResponseDTO> listar(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
        return toResponse(empresa);
    }

    @Transactional(readOnly = true)
    public Empresa buscarEntidade(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    @Transactional
    public EmpresaResponseDTO criar(EmpresaRequestDTO dto) {
        if (empresaRepository.existsByCnpj(dto.cnpj())) {
            throw new BusinessException("Já existe uma empresa cadastrada com o CNPJ: " + dto.cnpj());
        }

        Empresa empresa = Empresa.builder()
                .nome(dto.nome())
                .cnpj(dto.cnpj())
                .setor(dto.setor())
                .emailResponsavel(dto.emailResponsavel())
                .build();

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponseDTO atualizar(Long id, EmpresaRequestDTO dto) {
        Empresa empresa = buscarEntidade(id);

        if (!empresa.getCnpj().equals(dto.cnpj()) && empresaRepository.existsByCnpj(dto.cnpj())) {
            throw new BusinessException("Já existe outra empresa com o CNPJ: " + dto.cnpj());
        }

        empresa.setNome(dto.nome());
        empresa.setCnpj(dto.cnpj());
        empresa.setSetor(dto.setor());
        empresa.setEmailResponsavel(dto.emailResponsavel());

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void excluir(Long id) {
        Empresa empresa = buscarEntidade(id);
        empresaRepository.delete(empresa);
    }

    private EmpresaResponseDTO toResponse(Empresa e) {
        return new EmpresaResponseDTO(
                e.getId(),
                e.getNome(),
                e.getCnpj(),
                e.getSetor(),
                e.getEmailResponsavel(),
                e.getCreatedAt()
        );
    }
}
