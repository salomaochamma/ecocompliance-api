package br.com.fiap.ecocompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "TB_LICENCA_AMBIENTAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicencaAmbiental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LICENCA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "DS_TIPO_LICENCA", nullable = false, length = 100)
    private String tipoLicenca;

    @Column(name = "NR_LICENCA", nullable = false, length = 50, unique = true)
    private String numeroLicenca;

    @Column(name = "DT_EMISSAO", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "DT_VALIDADE", nullable = false)
    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_STATUS", nullable = false, length = 20)
    private StatusLicenca status;

    @Column(name = "DS_ORGAO_EMISSOR", nullable = false, length = 100)
    private String orgaoEmissor;
}
