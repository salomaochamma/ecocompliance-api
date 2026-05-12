package br.com.fiap.ecocompliance.repository;

import br.com.fiap.ecocompliance.entity.LicencaAmbiental;
import br.com.fiap.ecocompliance.entity.StatusLicenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LicencaAmbientalRepository extends JpaRepository<LicencaAmbiental, Long> {

    List<LicencaAmbiental> findByEmpresaId(Long empresaId);

    @Query("SELECT l FROM LicencaAmbiental l WHERE l.dataValidade BETWEEN :hoje AND :limite " +
            "AND l.status = br.com.fiap.ecocompliance.entity.StatusLicenca.ATIVA ORDER BY l.dataValidade ASC")
    List<LicencaAmbiental> findVencendo(@Param("hoje") LocalDate hoje,
                                        @Param("limite") LocalDate limite);

    long countByEmpresaIdAndStatus(Long empresaId, StatusLicenca status);
}
