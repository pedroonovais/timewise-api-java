package com.timewise.timewise.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuração do RabbitMQ para mensageria assíncrona
 * Define filas, exchanges e bindings para processamento de eventos
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    // Nomes das filas
    public static final String SCORE_CALCULAR_QUEUE = "score.calcular";
    public static final String NOTIFICACOES_QUEUE = "notificacoes.enviar";
    public static final String RELATORIOS_QUEUE = "relatorios.gerar";

    // Nome do exchange
    public static final String TIMEWISE_EXCHANGE = "timewise.exchange";

    // Routing keys
    public static final String ATIVIDADE_CRIADA_ROUTING_KEY = "atividade.criada";
    public static final String ATIVIDADE_ATUALIZADA_ROUTING_KEY = "atividade.atualizada";
    public static final String ATIVIDADE_DELETADA_ROUTING_KEY = "atividade.deletada";

    /**
     * Configura o converter de mensagens para JSON
     * @return Jackson2JsonMessageConverter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura o RabbitTemplate com o converter JSON
     * @param connectionFactory - ConnectionFactory do RabbitMQ
     * @return RabbitTemplate configurado
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            log.error("ConnectionFactory é null");
            throw new IllegalStateException("ConnectionFactory não pode ser null");
        }
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        MessageConverter converter = jsonMessageConverter();
        if (converter != null) {
            template.setMessageConverter(converter);
        }
        log.info("RabbitTemplate configurado com Jackson2JsonMessageConverter");
        return template;
    }

    /**
     * Configura o SimpleRabbitListenerContainerFactory com o converter JSON
     * @param connectionFactory - ConnectionFactory do RabbitMQ
     * @return SimpleRabbitListenerContainerFactory configurado
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        log.info("SimpleRabbitListenerContainerFactory configurado com Jackson2JsonMessageConverter");
        return factory;
    }

    /**
     * Fila para cálculo de score diário
     * @return Queue configurada como durable
     */
    @Bean
    public Queue scoreCalcularQueue() {
        log.info("Criando fila: {}", SCORE_CALCULAR_QUEUE);
        return QueueBuilder.durable(SCORE_CALCULAR_QUEUE).build();
    }

    /**
     * Fila para envio de notificações
     * @return Queue configurada como durable
     */
    @Bean
    public Queue notificacoesQueue() {
        log.info("Criando fila: {}", NOTIFICACOES_QUEUE);
        return QueueBuilder.durable(NOTIFICACOES_QUEUE).build();
    }

    /**
     * Fila para geração de relatórios
     * @return Queue configurada como durable
     */
    @Bean
    public Queue relatoriosQueue() {
        log.info("Criando fila: {}", RELATORIOS_QUEUE);
        return QueueBuilder.durable(RELATORIOS_QUEUE).build();
    }

    /**
     * Exchange do tipo Topic para roteamento de mensagens
     * @return TopicExchange
     */
    @Bean
    public TopicExchange timewiseExchange() {
        log.info("Criando exchange: {}", TIMEWISE_EXCHANGE);
        return new TopicExchange(TIMEWISE_EXCHANGE);
    }

    /**
     * Binding da fila de score com o exchange
     * Escuta eventos de atividades (criada, atualizada, deletada)
     * @return Binding configurado
     */
    @Bean
    public Binding scoreCalcularBinding() {
        log.info("Criando binding: {} -> {} com routing key: atividade.*", 
            SCORE_CALCULAR_QUEUE, TIMEWISE_EXCHANGE);
        return BindingBuilder
            .bind(scoreCalcularQueue())
            .to(timewiseExchange())
            .with("atividade.*");
    }
}

