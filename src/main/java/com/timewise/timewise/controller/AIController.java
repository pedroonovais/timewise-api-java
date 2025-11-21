package com.timewise.timewise.controller;

import com.timewise.timewise.dto.AnaliseProdutividadeDTO;
import com.timewise.timewise.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI", description = "Recursos de Inteligência Artificial Generativa")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "Gerar análise de produtividade", description = "Analisa atividades e scores recentes para gerar insights personalizados.")
    @GetMapping("/analise")
    public ResponseEntity<AnaliseProdutividadeDTO> getAnaliseProdutividade(Authentication authentication) {
        String email = authentication.getName();
        
        String insights = aiService.gerarAnaliseProdutividade(email);
        
        return ResponseEntity.ok(AnaliseProdutividadeDTO.builder()
                .insights(insights)
                .dataAnalise(LocalDateTime.now())
                .build());
    }
}

