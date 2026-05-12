package br.com.fiap.ecocompliance.repository;

import br.com.fiap.ecocompliance.entity.EmissaoCarbono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EmissaoCarbonoRepository extends JpaRepository<EmissaoCarbono, Long> {

    List<EmissaoCarbono> findByEmpresaId(Long empresaId);

    @Query("SELECT COALESCE(SUM(e.quantidadeCo2), 0) FROM EmissaoCarbono e WHERE e.empresa.id = :empresaId")
    BigDecimal somaEmissoesPorEmpresa(@Param("empresaId") Long empresaId);
}
