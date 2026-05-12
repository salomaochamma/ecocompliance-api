package br.com.fiap.ecocompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TB_COMPENSACAO_AMBIENTAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompensacaoAmbiental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMPENSACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "DS_TIPO_COMPENSACAO", nullable = false, length = 100)
    private String tipoCompensacao;

    @Column(name = "DS_DESCRICAO", nullable = false, length = 500)
    private String descricao;

    @Column(name = "VL_QUANTIDADE_COMPENSADA", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeCompensada;

    @Column(name = "DT_ACAO", nullable = false)
    private LocalDate dataAcao;
}
