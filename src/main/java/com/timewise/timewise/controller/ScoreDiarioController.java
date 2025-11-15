package com.timewise.timewise.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timewise.timewise.dto.ScoreDiarioResponseDTO;
import com.timewise.timewise.service.ScoreDiarioService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints de ScoreDiario
 * Score não pode ser editado, apenas consultado
 */
@RestController
@RequestMapping("/score-diario")
@Slf4j
public class ScoreDiarioController {
    
    @Autowired
    private ScoreDiarioService scoreDiarioService;

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Lista todos os scores diários de um usuário", 
               description = "Retorna uma lista de todos os scores diários de um usuário específico")
    public ResponseEntity<List<ScoreDiarioResponseDTO>> listarPorUsuario(
            @PathVariable Long usuarioId) {
        List<ScoreDiarioResponseDTO> scores = scoreDiarioService.listarPorUsuario(usuarioId);
        log.info("Retornando {} scores diários para usuário ID: {}", scores.size(), usuarioId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um score diário pelo ID", 
               description = "Retorna um score diário cadastrado")
    public ResponseEntity<ScoreDiarioResponseDTO> getScoreById(@PathVariable Long id) {
        ScoreDiarioResponseDTO score = scoreDiarioService.buscarPorId(id);
        log.info("Score diário encontrado com ID: {}", id);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/usuario/{usuarioId}/data")
    @Operation(summary = "Busca um score diário por usuário e data", 
               description = "Retorna o score diário de um usuário para uma data específica")
    public ResponseEntity<ScoreDiarioResponseDTO> getScorePorUsuarioEData(
            @PathVariable Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataTrabalho) {
        ScoreDiarioResponseDTO score = scoreDiarioService.buscarPorUsuarioEData(usuarioId, dataTrabalho);
        log.info("Score diário encontrado para usuário ID: {} na data: {}", usuarioId, dataTrabalho);
        return ResponseEntity.ok(score);
    }
}

