package com.timewise.timewise.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
               description = "Retorna uma página de scores diários de um usuário específico. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: dataTrabalho,desc)")
    public ResponseEntity<Page<ScoreDiarioResponseDTO>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(page = 0, size = 20, sort = "dataTrabalho", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        // Valida tamanho máximo
        if (pageable.getPageSize() > 100) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                100, 
                pageable.getSort()
            );
        }
        Page<ScoreDiarioResponseDTO> scores = scoreDiarioService.listarPorUsuario(usuarioId, pageable);
        log.info("Retornando página {} de scores diários para usuário ID: {} (total: {})", 
            scores.getNumber(), usuarioId, scores.getTotalElements());
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

