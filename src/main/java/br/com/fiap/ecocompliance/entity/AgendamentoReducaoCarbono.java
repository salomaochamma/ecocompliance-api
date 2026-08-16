package br.com.fiap.ecocompliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_AGENDAMENTO_REDUCAO_CARBONO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoReducaoCarbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AGENDAMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "DS_TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "DS_DESCRICAO", nullable = false, length = 500)
    private String descricao;

    @Column(name = "DS_TIPO_INICIATIVA", nullable = false, length = 50)
    private String tipoIniciativa;

    @Column(name = "VL_META_CO2", nullable = false, precision = 15, scale = 4)
    private BigDecimal metaCo2;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "DT_FIM", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_STATUS", nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    private void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (status == null) status = StatusAgendamento.PENDENTE;
    }
}
