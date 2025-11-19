package com.timewise.timewise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timewise.timewise.dto.DicaResponseDTO;
import com.timewise.timewise.service.DicaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints de dicas de bem-estar geradas por IA
 */
@RestController
@RequestMapping("/dica")
@Slf4j
@Tag(name = "Dicas de Bem-Estar", description = "Endpoints para geração de dicas personalizadas usando IA")
public class DicaController {

    @Autowired
    private DicaService dicaService;

    /**
     * Gera uma dica personalizada de bem-estar para o usuário
     * @param usuarioId - ID do usuário
     * @return DicaResponseDTO com a dica gerada pela IA
     */
    @GetMapping("/{usuarioId}")
    @Operation(
            summary = "Gera uma dica de bem-estar personalizada",
            description = "Gera uma dica personalizada usando IA baseada no score diário, horas de trabalho e pausa do usuário. " +
                          "Se o score de hoje não existir, será calculado automaticamente antes de gerar a dica."
    )
    public ResponseEntity<DicaResponseDTO> gerarDica(@PathVariable Long usuarioId) {
        log.info("Requisição recebida para gerar dica para usuário ID: {}", usuarioId);

        try {
            String mensagem = dicaService.gerarDica(usuarioId);
            DicaResponseDTO response = DicaResponseDTO.builder()
                    .message(mensagem)
                    .build();

            log.info("Dica gerada com sucesso para usuário ID: {}", usuarioId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erro ao gerar dica para usuário ID: {} - {}", usuarioId, e.getMessage());
            
            // Retorna mensagem de erro amigável
            DicaResponseDTO errorResponse = DicaResponseDTO.builder()
                    .message("Não foi possível gerar uma dica no momento: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar dica para usuário ID: {}", usuarioId, e);
            
            DicaResponseDTO errorResponse = DicaResponseDTO.builder()
                    .message("Erro interno ao gerar dica. Tente novamente mais tarde.")
                    .build();
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

