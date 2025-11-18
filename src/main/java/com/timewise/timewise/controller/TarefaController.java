package com.timewise.timewise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.timewise.timewise.dto.TarefaRequestDTO;
import com.timewise.timewise.dto.TarefaResponseDTO;
import com.timewise.timewise.service.TarefaService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tarefas")
@Slf4j
public class TarefaController {
    
    @Autowired
    private TarefaService tarefaService;

    @GetMapping
    @Operation(summary = "Lista todas as tarefas", 
               description = "Retorna uma página de tarefas cadastradas. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: id,desc)")
    public ResponseEntity<Page<TarefaResponseDTO>> listarTodos(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        // Valida tamanho máximo
        if (pageable.getPageSize() > 100) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                100, 
                pageable.getSort()
            );
        }
        Page<TarefaResponseDTO> tarefas = tarefaService.listarTodos(pageable);
        log.info("Retornando página {} de tarefas (total: {})", 
            tarefas.getNumber(), tarefas.getTotalElements());
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa pelo ID", description = "Retorna uma tarefa cadastrada")
    public ResponseEntity<TarefaResponseDTO> getTarefaById(@PathVariable Long id) {
        TarefaResponseDTO tarefa = tarefaService.buscarPorId(id);
        log.info("Tarefa encontrada com ID: {}", id);
        return ResponseEntity.ok(tarefa);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Busca todas as tarefas de um usuário", 
               description = "Retorna uma página de tarefas de um usuário específico. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: id,desc)")
    public ResponseEntity<Page<TarefaResponseDTO>> getTarefasByUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(page = 0, size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        // Valida tamanho máximo
        if (pageable.getPageSize() > 100) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                100, 
                pageable.getSort()
            );
        }
        Page<TarefaResponseDTO> tarefas = tarefaService.buscarPorUsuario(usuarioId, pageable);
        log.info("Retornando página {} de tarefas para usuário ID: {} (total: {})", 
            tarefas.getNumber(), usuarioId, tarefas.getTotalElements());
        return ResponseEntity.ok(tarefas);
    }

    @PostMapping
    @Operation(summary = "Cria uma nova tarefa", description = "Cria uma nova tarefa com os dados fornecidos")
    public ResponseEntity<TarefaResponseDTO> criarTarefa(@Valid @RequestBody TarefaRequestDTO tarefaDTO) {
        TarefaResponseDTO tarefaCriada = tarefaService.criar(tarefaDTO);
        log.info("Tarefa criada com sucesso. ID: {}", tarefaCriada.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaCriada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma tarefa existente", description = "Atualiza uma tarefa com os dados fornecidos")
    public ResponseEntity<TarefaResponseDTO> atualizarTarefa(
            @PathVariable Long id, 
            @Valid @RequestBody TarefaRequestDTO tarefaDTO) {
        TarefaResponseDTO tarefaAtualizada = tarefaService.atualizar(id, tarefaDTO);
        log.info("Tarefa atualizada com sucesso. ID: {}", id);
        return ResponseEntity.ok(tarefaAtualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta uma tarefa existente", description = "Deleta uma tarefa com o ID fornecido")
    public ResponseEntity<Void> deletarTarefa(@PathVariable Long id) {
        tarefaService.deletar(id);
        log.info("Tarefa deletada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}

