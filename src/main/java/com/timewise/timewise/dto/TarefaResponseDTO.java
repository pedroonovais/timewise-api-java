package com.timewise.timewise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respostas de tarefas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TarefaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Long usuarioId;
}

