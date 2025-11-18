package com.timewise.timewise.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timewise.timewise.config.RabbitMQConfig;
import com.timewise.timewise.messaging.event.AtividadeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Publisher responsável por publicar eventos relacionados a atividades
 * Publica eventos na fila RabbitMQ para processamento assíncrono
 */
@Component
@Slf4j
public class AtividadeEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Publica evento de atividade criada
     * @param atividadeId - ID da atividade criada
     * @param usuarioId - ID do usuário
     * @param dataTrabalho - Data da atividade
     */
    public void publicarAtividadeCriada(Long atividadeId, Long usuarioId, java.time.LocalDate dataTrabalho) {
        AtividadeEvent evento = AtividadeEvent.builder()
            .atividadeId(atividadeId)
            .usuarioId(usuarioId)
            .dataTrabalho(dataTrabalho)
            .tipoEvento("CRIADA")
            .build();
        
        publicarEvento(evento, RabbitMQConfig.ATIVIDADE_CRIADA_ROUTING_KEY);
    }

    /**
     * Publica evento de atividade atualizada
     * @param atividadeId - ID da atividade atualizada
     * @param usuarioId - ID do usuário
     * @param dataTrabalho - Nova data da atividade
     * @param dataAntiga - Data antiga (se mudou)
     */
    public void publicarAtividadeAtualizada(Long atividadeId, Long usuarioId, 
                                           java.time.LocalDate dataTrabalho, 
                                           java.time.LocalDate dataAntiga) {
        AtividadeEvent evento = AtividadeEvent.builder()
            .atividadeId(atividadeId)
            .usuarioId(usuarioId)
            .dataTrabalho(dataTrabalho)
            .dataAntiga(dataAntiga)
            .tipoEvento("ATUALIZADA")
            .build();
        
        publicarEvento(evento, RabbitMQConfig.ATIVIDADE_ATUALIZADA_ROUTING_KEY);
    }

    /**
     * Publica evento de atividade deletada
     * @param atividadeId - ID da atividade deletada (pode ser null)
     * @param usuarioId - ID do usuário
     * @param dataTrabalho - Data da atividade deletada
     */
    public void publicarAtividadeDeletada(Long atividadeId, Long usuarioId, java.time.LocalDate dataTrabalho) {
        AtividadeEvent evento = AtividadeEvent.builder()
            .atividadeId(atividadeId)
            .usuarioId(usuarioId)
            .dataTrabalho(dataTrabalho)
            .tipoEvento("DELETADA")
            .build();
        
        publicarEvento(evento, RabbitMQConfig.ATIVIDADE_DELETADA_ROUTING_KEY);
    }

    /**
     * Publica evento na fila RabbitMQ
     * @param evento - Evento a ser publicado
     * @param routingKey - Chave de roteamento
     */
    private void publicarEvento(AtividadeEvent evento, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.TIMEWISE_EXCHANGE,
                routingKey,
                evento
            );
            log.info("Evento publicado - Tipo: {}, Usuário: {}, Data: {}, Routing Key: {}", 
                evento.getTipoEvento(), evento.getUsuarioId(), evento.getDataTrabalho(), routingKey);
        } catch (Exception e) {
            log.error("Erro ao publicar evento na fila RabbitMQ: {}", e.getMessage(), e);
            // Não lança exceção para não interromper o fluxo principal
            // O evento pode ser reprocessado manualmente se necessário
        }
    }
}

