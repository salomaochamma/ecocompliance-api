package br.com.fiap.ecocompliance.repository;

import br.com.fiap.ecocompliance.entity.AuditoriaAmbiental;
import br.com.fiap.ecocompliance.entity.NivelRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaAmbientalRepository extends JpaRepository<AuditoriaAmbiental, Long> {

    List<AuditoriaAmbiental> findByNivelRisco(NivelRisco nivelRisco);

    List<AuditoriaAmbiental> findByEmpresaId(Long empresaId);

    long countByEmpresaId(Long empresaId);

    long countByNivelRisco(NivelRisco nivelRisco);
}
