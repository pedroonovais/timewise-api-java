package com.timewise.timewise.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Handler global para tratamento de exceções da aplicação
 * Mapeia exceções para status codes HTTP apropriados e mensagens amigáveis
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    /**
     * Trata exceções de validação do Spring (@Valid)
     * Retorna status 400 (Bad Request) com lista de erros de validação
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, Locale locale) {
        log.warn("Erro de validação: {}", ex.getMessage());
        
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });

        String message = getMessage("error.validation", locale);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Trata RuntimeException genéricas
     * Analisa a mensagem para determinar o status code apropriado
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, Locale locale) {
        String message = ex.getMessage();
        log.warn("RuntimeException capturada: {}", message);

        // Determina o status code baseado na mensagem
        HttpStatus status = determineHttpStatus(message);
        
        // Tenta buscar mensagem amigável do MessageSource
        String friendlyMessage = getFriendlyMessage(message, locale);
        
        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            friendlyMessage,
            null
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Trata exceções genéricas não previstas
     * Retorna status 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, Locale locale) {
        log.error("Exceção não tratada: {}", ex.getMessage(), ex);
        
        String message = getMessage("error.internal", locale);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message,
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Determina o status HTTP baseado na mensagem de erro
     */
    private HttpStatus determineHttpStatus(String message) {
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String lowerMessage = message.toLowerCase();

        // 404 - Não encontrado
        if (lowerMessage.contains("não encontrado") || 
            lowerMessage.contains("nao encontrado") ||
            lowerMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        // 409 - Conflito (recurso já existe)
        if (lowerMessage.contains("já cadastrado") || 
            lowerMessage.contains("ja cadastrado") ||
            lowerMessage.contains("already registered") ||
            lowerMessage.contains("already exists")) {
            return HttpStatus.CONFLICT;
        }

        // 400 - Bad Request (validações de negócio)
        if (lowerMessage.contains("não pode ser nulo") ||
            lowerMessage.contains("nao pode ser nulo") ||
            lowerMessage.contains("cannot be null") ||
            lowerMessage.contains("deve ser posterior") ||
            lowerMessage.contains("must be after") ||
            lowerMessage.contains("obrigatório") ||
            lowerMessage.contains("required")) {
            return HttpStatus.BAD_REQUEST;
        }

        // Default: 500 para erros não mapeados
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Busca mensagem amigável do MessageSource baseada na mensagem de erro
     */
    private String getFriendlyMessage(String message, Locale locale) {
        if (message == null) {
            return getMessage("error.generic", locale);
        }

        String lowerMessage = message.toLowerCase();

        // Mapeia mensagens específicas para chaves do MessageSource
        if (lowerMessage.contains("não encontrado") || lowerMessage.contains("nao encontrado")) {
            if (lowerMessage.contains("score diário") || lowerMessage.contains("score diario")) {
                return getMessage("error.scoreDiario.notfound", locale);
            } else if (lowerMessage.contains("atividade")) {
                return getMessage("error.atividade.notfound", locale);
            } else if (lowerMessage.contains("usuário") || lowerMessage.contains("usuario")) {
                return getMessage("error.usuario.notfound", locale);
            }
            return getMessage("error.notfound", locale);
        }

        if (lowerMessage.contains("já cadastrado") || lowerMessage.contains("ja cadastrado")) {
            if (lowerMessage.contains("email")) {
                return getMessage("error.email.duplicado", locale);
            }
            return getMessage("error.duplicado", locale);
        }

        if (lowerMessage.contains("não pode ser nulo") || lowerMessage.contains("nao pode ser nulo")) {
            return getMessage("error.null", locale);
        }

        if (lowerMessage.contains("deve ser posterior")) {
            return getMessage("error.tempo.invalido", locale);
        }

        // Mapeia erros de processamento
        if (lowerMessage.contains("erro ao processar dados da atividade")) {
            return getMessage("error.atividade.processar", locale);
        }

        if (lowerMessage.contains("erro ao atualizar atividade")) {
            return getMessage("error.atividade.atualizar", locale);
        }

        if (lowerMessage.contains("erro ao processar dados do usuário") || 
            lowerMessage.contains("erro ao processar dados do usuario")) {
            return getMessage("error.usuario.processar", locale);
        }

        // Se não encontrar mapeamento, retorna a mensagem original
        return message;
    }

    /**
     * Busca mensagem do MessageSource
     */
    private String getMessage(String key, Locale locale) {
        try {
            Locale targetLocale = locale != null ? locale : 
                (LocaleContextHolder.getLocale() != null ? LocaleContextHolder.getLocale() : Locale.getDefault());
            String message = messageSource.getMessage(key, null, targetLocale);
            return message != null ? message : key;
        } catch (Exception e) {
            log.warn("Chave de mensagem não encontrada: {}", key);
            return key;
        }
    }

    /**
     * Classe interna para representar resposta de erro padronizada
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private List<String> errors;

        public ErrorResponse(int status, String message, List<String> errors) {
            this.status = status;
            this.message = message;
            this.errors = errors;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}

