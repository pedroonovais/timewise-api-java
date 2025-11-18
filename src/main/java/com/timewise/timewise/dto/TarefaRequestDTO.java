package com.timewise.timewise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisições de criação e atualização de tarefas
 * Não inclui o ID, pois é gerado automaticamente
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TarefaRequestDTO {

    @NotBlank(message = "{tarefa.request.nome.notblank}")
    private String nome;

    @NotNull(message = "{tarefa.request.descricao.notnull}")
    private String descricao;

    @NotNull(message = "{tarefa.request.usuarioId.notnull}")
    private Long usuarioId;
}

