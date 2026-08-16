package br.com.fiap.ecocompliance.repository;

import br.com.fiap.ecocompliance.entity.AgendamentoReducaoCarbono;
import br.com.fiap.ecocompliance.entity.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendamentoReducaoCarbonoRepository extends JpaRepository<AgendamentoReducaoCarbono, Long> {

    List<AgendamentoReducaoCarbono> findByEmpresaId(Long empresaId);

    List<AgendamentoReducaoCarbono> findByStatus(StatusAgendamento status);

    long countByStatus(StatusAgendamento status);
}
