package com.timewise.timewise.model;

import jakarta.persistence.Entity;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tarefas")
public class Tarefa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{tarefa.nome.notblank}")
    private String nome;

    @NotNull(message = "{tarefa.descricao.notnull}")
    private String descricao;

    @NotNull(message = "{tarefa.usuario.notnull}")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
