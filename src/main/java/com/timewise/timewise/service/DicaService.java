package com.timewise.timewise.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.timewise.timewise.dto.ScoreDiarioResponseDTO;
import com.timewise.timewise.model.ScoreDiario;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service responsável pela geração de dicas de bem-estar usando IA
 */
@Service
@Slf4j
public class DicaService {

    @Autowired
    private ScoreDiarioService scoreDiarioService;

    private final WebClient webClient;
    private final String groqModel;

    /**
     * Construtor que inicializa o WebClient com as configurações da API Groq
     * @param apiKey - Chave da API Groq
     * @param groqModel - Modelo de IA a ser utilizado
     */
    public DicaService(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String groqModel) {
        this.groqModel = groqModel;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        log.info("DicaService inicializado com modelo: {}", groqModel);
    }

    /**
     * Gera uma dica personalizada de bem-estar para o usuário baseada no seu score diário
     * @param usuarioId - ID do usuário
     * @return Dica gerada pela IA
     */
    @SuppressWarnings("null")
    public String gerarDica(Long usuarioId) {
        log.info("=== GERANDO DICA DE BEM-ESTAR ===");
        log.info("Usuário ID: {}", usuarioId);

        if (usuarioId == null) {
            log.warn("Tentativa de gerar dica com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }

        LocalDate hoje = LocalDate.now();
        log.info("Data atual: {}", hoje);

        // Busca ou calcula o score diário do usuário
        ScoreDiarioResponseDTO scoreResponse;
        try {
            scoreResponse = scoreDiarioService.buscarPorUsuarioEData(usuarioId, hoje);
            log.info("Score encontrado para hoje: {}", scoreResponse.getValor());
        } catch (RuntimeException e) {
            log.info("Score não encontrado para hoje, calculando novo score...");
            ScoreDiario scoreCalculado = scoreDiarioService.calcularESalvarScore(usuarioId, hoje);
            scoreResponse = ScoreDiarioResponseDTO.builder()
                    .id(scoreCalculado.getId())
                    .usuarioId(scoreCalculado.getUsuario().getId())
                    .dataTrabalho(scoreCalculado.getDataTrabalho())
                    .valor(scoreCalculado.getValor())
                    .build();
            log.info("Novo score calculado: {}", scoreResponse.getValor());
        }

        // Obtém as horas de trabalho e pausa
        double[] horas = scoreDiarioService.calcularHorasTrabalhoEPausa(usuarioId, hoje);
        double horasTrabalho = horas[0];
        double horasPausa = horas[1];

        log.info("Horas de trabalho: {}h, Horas de pausa: {}h", horasTrabalho, horasPausa);

        // Constrói o prompt para a IA
        String prompt = construirPrompt(scoreResponse.getValor(), horasTrabalho, horasPausa);
        log.debug("Prompt construído: {}", prompt);

        // Cria a requisição para a API Groq
        ChatCompletionRequest request = new ChatCompletionRequest(
                groqModel,
                List.of(
                        new Message("system", """
                                Você é um especialista em bem-estar e produtividade saudável.
                                Sua função é fornecer dicas personalizadas e motivacionais para ajudar as pessoas
                                a manterem um equilíbrio saudável entre trabalho e descanso.
                                Seja empático, encorajador e forneça conselhos práticos e acionáveis.
                                Mantenha suas respostas concisas (máximo 3-4 frases) e em português do Brasil.
                                """.trim()),
                        new Message("user", prompt)
                ),
                0.7,  // Temperatura para respostas criativas mas coerentes
                200,  // Limite de tokens para resposta concisa
                false // Sem streaming
        );

        log.info("Enviando requisição para Groq API...");

        try {
            // Envia a requisição para a API Groq
            GroqResponse response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Erro na API Groq - Status: {}, Body: {}", 
                                                clientResponse.statusCode(), body);
                                        return Mono.error(new RuntimeException(
                                                "Erro na API Groq: " + clientResponse.statusCode() + " - " + body));
                                    }))
                    .bodyToMono(GroqResponse.class)
                    .block();

            log.info("Resposta recebida da API Groq");

            // Extrai a mensagem da resposta
            if (response != null && response.choices != null && !response.choices.isEmpty()) {
                Choice choice = response.choices.get(0);
                if (choice.message != null && choice.message.content != null) {
                    String dica = choice.message.content.trim();
                    log.info("Dica gerada com sucesso: {}", dica);
                    return dica;
                }
            }

            log.warn("Resposta da API Groq vazia ou sem conteúdo");
            return "Desculpe, não foi possível gerar uma dica no momento. Tente novamente mais tarde.";

        } catch (WebClientResponseException e) {
            log.error("Erro HTTP na chamada à API Groq - Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return "Erro ao se comunicar com o serviço de dicas. Tente novamente mais tarde.";
        } catch (Exception e) {
            log.error("Erro geral ao gerar dica: {}", e.getMessage(), e);
            return "Erro ao gerar dica de bem-estar. Tente novamente mais tarde.";
        }
    }

    /**
     * Constrói o prompt para a IA baseado nos dados do usuário
     * @param score - Score diário (0-100)
     * @param horasTrabalho - Horas de trabalho
     * @param horasPausa - Horas de pausa
     * @return Prompt formatado
     */
    private String construirPrompt(Integer score, double horasTrabalho, double horasPausa) {
        return String.format(
                """
                Hoje o usuário teve os seguintes indicadores de produtividade:
                - Score de bem-estar: %d/100
                - Horas de trabalho: %.1fh
                - Horas de pausa: %.1fh
                
                Com base nesses dados, forneça uma dica personalizada e motivacional para ajudar
                a pessoa a manter ou melhorar seu equilíbrio entre trabalho e descanso.
                """,
                score, horasTrabalho, horasPausa
        ).trim();
    }

    // ===== DTOs para comunicação com a API Groq =====

    /**
     * Requisição para completar chat com a API Groq
     */
    public record ChatCompletionRequest(
            String model,
            List<Message> messages,
            Double temperature,
            Integer max_tokens,
            Boolean stream
    ) {}

    /**
     * Resposta da API Groq
     */
    public static class GroqResponse {
        public List<Choice> choices;
    }

    /**
     * Opção de resposta da API Groq
     */
    public static class Choice {
        public Message message;
    }

    /**
     * Mensagem de chat
     */
    public static class Message {
        public String role;
        public String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

