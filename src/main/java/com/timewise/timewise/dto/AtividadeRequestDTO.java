package com.timewise.timewise.dto;

import java.time.LocalDateTime;

import com.timewise.timewise.enums.AtividadeTipo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisições de criação e atualização de atividades
 * Não inclui o ID, pois é gerado automaticamente
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtividadeRequestDTO {

    @NotBlank(message = "{atividade.request.nome.notblank}")
    private String nome;

    @NotNull(message = "{atividade.request.usuarioId.notnull}")
    private Long usuarioId;

    @NotNull(message = "{atividade.request.tempoInicio.notnull}")
    private LocalDateTime tempoInicio;

    @NotNull(message = "{atividade.request.tempoFim.notnull}")
    private LocalDateTime tempoFim;

    @NotNull(message = "{atividade.request.tipo.notnull}")
    private AtividadeTipo tipo;
}

