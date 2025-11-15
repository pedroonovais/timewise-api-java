package com.timewise.timewise.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.timewise.timewise.enums.AtividadeTipo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "atividades")
public class Atividade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{atividade.nome.notblank}")
    private String nome;

    @NotNull(message = "{atividade.usuario.notnull}")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull(message = "{atividade.tempoInicio.notnull}")
    private java.time.LocalDateTime tempoInicio;

    @NotNull(message = "{atividade.tempoFim.notnull}")
    private java.time.LocalDateTime tempoFim;

    @NotNull(message = "{atividade.tipo.notnull}")
    @Enumerated(EnumType.STRING)
    private AtividadeTipo tipo;
}
