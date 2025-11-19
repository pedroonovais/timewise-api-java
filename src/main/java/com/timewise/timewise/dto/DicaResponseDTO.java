package com.timewise.timewise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de dicas de bem-estar geradas pela IA
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DicaResponseDTO {

    private String message;
}

