package com.timewise.timewise.messaging.event;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento publicado quando uma atividade é criada, atualizada ou deletada
 * Usado para processar cálculo de score de forma assíncrona
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtividadeEvent {

    /**
     * ID da atividade (pode ser null se foi deletada)
     */
    private Long atividadeId;

    /**
     * ID do usuário dono da atividade
     */
    private Long usuarioId;

    /**
     * Data da atividade (data do trabalho)
     */
    private LocalDate dataTrabalho;

    /**
     * Tipo do evento: "CRIADA", "ATUALIZADA", "DELETADA"
     */
    private String tipoEvento;

    /**
     * Data antiga (usado apenas em atualizações quando a data muda)
     */
    private LocalDate dataAntiga;
}

