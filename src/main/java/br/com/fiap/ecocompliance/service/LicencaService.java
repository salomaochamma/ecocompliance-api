package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.LicencaRequestDTO;
import br.com.fiap.ecocompliance.dto.LicencaResponseDTO;
import br.com.fiap.ecocompliance.entity.Empresa;
import br.com.fiap.ecocompliance.entity.LicencaAmbiental;
import br.com.fiap.ecocompliance.exception.BusinessException;
import br.com.fiap.ecocompliance.exception.ResourceNotFoundException;
import br.com.fiap.ecocompliance.repository.LicencaAmbientalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicencaService {

    private final LicencaAmbientalRepository licencaRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public Page<LicencaResponseDTO> listar(Pageable pageable) {
        return licencaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LicencaResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<LicencaResponseDTO> licencasVencendo() {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(30);
        return licencaRepository.findVencendo(hoje, limite).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LicencaResponseDTO criar(LicencaRequestDTO dto) {
        validarDatas(dto.dataEmissao(), dto.dataValidade());

        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        LicencaAmbiental licenca = LicencaAmbiental.builder()
                .empresa(empresa)
                .tipoLicenca(dto.tipoLicenca())
                .numeroLicenca(dto.numeroLicenca())
                .dataEmissao(dto.dataEmissao())
                .dataValidade(dto.dataValidade())
                .status(dto.status())
                .orgaoEmissor(dto.orgaoEmissor())
                .build();

        return toResponse(licencaRepository.save(licenca));
    }

    @Transactional
    public LicencaResponseDTO atualizar(Long id, LicencaRequestDTO dto) {
        validarDatas(dto.dataEmissao(), dto.dataValidade());

        LicencaAmbiental licenca = buscarEntidade(id);
        Empresa empresa = empresaService.buscarEntidade(dto.empresaId());

        licenca.setEmpresa(empresa);
        licenca.setTipoLicenca(dto.tipoLicenca());
        licenca.setNumeroLicenca(dto.numeroLicenca());
        licenca.setDataEmissao(dto.dataEmissao());
        licenca.setDataValidade(dto.dataValidade());
        licenca.setStatus(dto.status());
        licenca.setOrgaoEmissor(dto.orgaoEmissor());

        return toResponse(licencaRepository.save(licenca));
    }

    @Transactional
    public void excluir(Long id) {
        LicencaAmbiental licenca = buscarEntidade(id);
        licencaRepository.delete(licenca);
    }

    private LicencaAmbiental buscarEntidade(Long id) {
        return licencaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Licena não encontrada com ID: " + id));
    }

    private void validarDatas(LocalDate emissao, LocalDate validade) {
        if (validade.isBefore(emissao)) {
            throw new BusinessException("Data de validade não pode ser anterior  data de emissão");
        }
    }

    private LicencaResponseDTO toResponse(LicencaAmbiental l) {
        return new LicencaResponseDTO(
                l.getId(),
                l.getEmpresa().getId(),
                l.getEmpresa().getNome(),
                l.getTipoLicenca(),
                l.getNumeroLicenca(),
                l.getDataEmissao(),
                l.getDataValidade(),
                l.getStatus(),
                l.getOrgaoEmissor()
        );
    }
}
