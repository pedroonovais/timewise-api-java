package com.timewise.timewise.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respostas de score diário
 * Score não pode ser editado, apenas consultado
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreDiarioResponseDTO {

    private Long id;
    private Long usuarioId;
    private LocalDate dataTrabalho;
    private Integer valor;
}

