package com.timewise.timewise.dto;

import java.time.LocalDateTime;

import com.timewise.timewise.enums.AtividadeTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respostas de atividades
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtividadeResponseDTO {

    private Long id;
    private String nome;
    private Long usuarioId;
    private LocalDateTime tempoInicio;
    private LocalDateTime tempoFim;
    private AtividadeTipo tipo;
}

