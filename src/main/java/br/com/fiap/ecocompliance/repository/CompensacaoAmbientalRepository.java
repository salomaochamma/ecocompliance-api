package br.com.fiap.ecocompliance.repository;

import br.com.fiap.ecocompliance.entity.CompensacaoAmbiental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CompensacaoAmbientalRepository extends JpaRepository<CompensacaoAmbiental, Long> {

    List<CompensacaoAmbiental> findByEmpresaId(Long empresaId);

    @Query("SELECT COALESCE(SUM(c.quantidadeCompensada), 0) FROM CompensacaoAmbiental c WHERE c.empresa.id = :empresaId")
    BigDecimal somaCompensacoesPorEmpresa(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.quantidadeCompensada), 0) FROM CompensacaoAmbiental c")
    BigDecimal somaTodasCompensacoes();
}
