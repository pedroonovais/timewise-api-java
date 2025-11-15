package com.timewise.timewise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "score_diario")
public class ScoreDiario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{scoreDiario.usuario.notnull}")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull(message = "{scoreDiario.dataTrabalho.notnull}")
    @Column(name = "data_trabalho")
    private java.time.LocalDate dataTrabalho;

    @NotNull(message = "{scoreDiario.valor.notnull}")
    @Column(name = "score")
    private Integer valor;
}
