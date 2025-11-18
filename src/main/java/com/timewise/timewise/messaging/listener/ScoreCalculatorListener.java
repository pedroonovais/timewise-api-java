package com.timewise.timewise.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timewise.timewise.config.RabbitMQConfig;
import com.timewise.timewise.messaging.event.AtividadeEvent;
import com.timewise.timewise.service.ScoreDiarioService;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener que processa eventos de atividades de forma assíncrona
 * Calcula o score diário quando uma atividade é criada, atualizada ou deletada
 */
@Component
@Slf4j
public class ScoreCalculatorListener {

    @Autowired
    private ScoreDiarioService scoreDiarioService;

    /**
     * Processa eventos de atividades para calcular score diário
     * Escuta a fila "score.calcular" e processa de forma assíncrona
     * @param evento - Evento de atividade (criada, atualizada ou deletada)
     */
    @RabbitListener(queues = RabbitMQConfig.SCORE_CALCULAR_QUEUE)
    public void calcularScore(AtividadeEvent evento) {
        log.info("Processando cálculo de score assíncrono - Usuário: {}, Data: {}, Tipo: {}", 
            evento.getUsuarioId(), evento.getDataTrabalho(), evento.getTipoEvento());
        
        try {
            // Valida dados do evento
            if (evento.getUsuarioId() == null || evento.getDataTrabalho() == null) {
                log.warn("Evento inválido recebido - usuarioId ou dataTrabalho é null");
                return;
            }
            
            // Calcula score para a data da atividade
            scoreDiarioService.calcularESalvarScore(
                evento.getUsuarioId(), 
                evento.getDataTrabalho()
            );
            log.info("Score calculado com sucesso para usuário ID: {} na data: {}", 
                evento.getUsuarioId(), evento.getDataTrabalho());
            
            // Se a data mudou (atualização), recalcula também a data antiga
            if (evento.getDataAntiga() != null && 
                !evento.getDataAntiga().equals(evento.getDataTrabalho())) {
                log.info("Recalculando score para data antiga: {}", evento.getDataAntiga());
                scoreDiarioService.calcularESalvarScore(
                    evento.getUsuarioId(), 
                    evento.getDataAntiga()
                );
                log.info("Score recalculado com sucesso para data antiga: {}", evento.getDataAntiga());
            }
            
        } catch (Exception e) {
            log.error("Erro ao calcular score assíncrono para usuário ID: {} na data: {} - {}", 
                evento.getUsuarioId(), evento.getDataTrabalho(), e.getMessage(), e);
            // Re-lança a exceção para que o RabbitMQ possa fazer retry
            // O RabbitMQ tentará reprocessar a mensagem automaticamente
            throw new RuntimeException("Erro ao processar cálculo de score", e);
        }
    }
}

