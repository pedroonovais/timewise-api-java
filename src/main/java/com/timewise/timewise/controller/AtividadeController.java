package com.timewise.timewise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timewise.timewise.dto.AtividadeRequestDTO;
import com.timewise.timewise.dto.AtividadeResponseDTO;
import com.timewise.timewise.service.AtividadeService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/atividades")
@Slf4j
public class AtividadeController {
    
    @Autowired
    private AtividadeService atividadeService;

    @GetMapping
    @Operation(summary = "Lista todas as atividades", description = "Retorna uma lista de todas as atividades cadastradas")
    public ResponseEntity<List<AtividadeResponseDTO>> listarTodos() {
        List<AtividadeResponseDTO> atividades = atividadeService.listarTodos();
        log.info("Retornando {} atividades", atividades.size());
        return ResponseEntity.ok(atividades);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma atividade pelo ID", description = "Retorna uma atividade cadastrada")
    public ResponseEntity<AtividadeResponseDTO> getAtividadeById(@PathVariable Long id) {
        AtividadeResponseDTO atividade = atividadeService.buscarPorId(id);
        log.info("Atividade encontrada com ID: {}", id);
        return ResponseEntity.ok(atividade);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Busca todas as atividades de um usuário", description = "Retorna uma lista de todas as atividades de um usuário específico")
    public ResponseEntity<List<AtividadeResponseDTO>> getAtividadesByUsuario(@PathVariable Long usuarioId) {
        List<AtividadeResponseDTO> atividades = atividadeService.buscarPorUsuario(usuarioId);
        log.info("Atividades encontradas para usuário ID: {}", usuarioId);
        return ResponseEntity.ok(atividades);
    }

    @PostMapping
    @Operation(summary = "Cria uma nova atividade", description = "Cria uma nova atividade com os dados fornecidos")
    public ResponseEntity<AtividadeResponseDTO> criarAtividade(@Valid @RequestBody AtividadeRequestDTO atividadeDTO) {
        AtividadeResponseDTO atividadeCriada = atividadeService.criar(atividadeDTO);
        log.info("Atividade criada com sucesso. ID: {}", atividadeCriada.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(atividadeCriada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma atividade existente", description = "Atualiza uma atividade com os dados fornecidos")
    public ResponseEntity<AtividadeResponseDTO> atualizarAtividade(
            @PathVariable Long id, 
            @Valid @RequestBody AtividadeRequestDTO atividadeDTO) {
        AtividadeResponseDTO atividadeAtualizada = atividadeService.atualizar(id, atividadeDTO);
        log.info("Atividade atualizada com sucesso. ID: {}", id);
        return ResponseEntity.ok(atividadeAtualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta uma atividade existente", description = "Deleta uma atividade com o ID fornecido")
    public ResponseEntity<Void> deletarAtividade(@PathVariable Long id) {
        atividadeService.deletar(id);
        log.info("Atividade deletada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}

