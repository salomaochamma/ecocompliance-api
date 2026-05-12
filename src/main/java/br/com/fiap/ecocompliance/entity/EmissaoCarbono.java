package br.com.fiap.ecocompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TB_EMISSAO_CARBONO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmissaoCarbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMISSAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "VL_QUANTIDADE_CO2", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeCo2;

    @Column(name = "DS_FONTE_EMISSAO", nullable = false, length = 150)
    private String fonteEmissao;

    @Column(name = "DT_REGISTRO", nullable = false)
    private LocalDate dataRegistro;

    @Column(name = "DS_UNIDADE_MEDIDA", nullable = false, length = 20)
    private String unidadeMedida;
}
