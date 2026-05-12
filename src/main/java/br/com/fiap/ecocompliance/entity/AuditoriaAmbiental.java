package br.com.fiap.ecocompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "TB_AUDITORIA_AMBIENTAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaAmbiental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AUDITORIA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "DT_AUDITORIA", nullable = false)
    private LocalDate dataAuditoria;

    @Column(name = "DS_RESULTADO", nullable = false, length = 200)
    private String resultado;

    @Column(name = "DS_OBSERVACOES", length = 1000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_NIVEL_RISCO", nullable = false, length = 10)
    private NivelRisco nivelRisco;
}
