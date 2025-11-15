package com.timewise.timewise.mapper;

import org.springframework.stereotype.Component;

import com.timewise.timewise.dto.ScoreDiarioResponseDTO;
import com.timewise.timewise.model.ScoreDiario;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper responsável por converter entre DTOs e entidades ScoreDiario
 */
@Component
@Slf4j
public class ScoreDiarioMapper {

    /**
     * Converte entidade ScoreDiario para ScoreDiarioResponseDTO
     * @param scoreDiario - Entidade ScoreDiario
     * @return DTO de resposta
     */
    public ScoreDiarioResponseDTO toResponseDTO(ScoreDiario scoreDiario) {
        log.debug("Convertendo entidade ScoreDiario para ScoreDiarioResponseDTO");
        if (scoreDiario == null) {
            return null;
        }
        
        return ScoreDiarioResponseDTO.builder()
            .id(scoreDiario.getId())
            .usuarioId(scoreDiario.getUsuario() != null ? scoreDiario.getUsuario().getId() : null)
            .dataTrabalho(scoreDiario.getDataTrabalho())
            .valor(scoreDiario.getValor())
            .build();
    }
}

