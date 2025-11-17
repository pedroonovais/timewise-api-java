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
    @Operation(summary = "Lista todas as atividades", 
               description = "Retorna uma página de atividades cadastradas. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: id,desc)")
    public ResponseEntity<Page<AtividadeResponseDTO>> listarTodos(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        // Valida tamanho máximo
        if (pageable.getPageSize() > 100) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                100, 
                pageable.getSort()
            );
        }
        Page<AtividadeResponseDTO> atividades = atividadeService.listarTodos(pageable);
        log.info("Retornando página {} de atividades (total: {})", 
            atividades.getNumber(), atividades.getTotalElements());
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
    @Operation(summary = "Busca todas as atividades de um usuário", 
               description = "Retorna uma página de atividades de um usuário específico. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: id,desc)")
    public ResponseEntity<Page<AtividadeResponseDTO>> getAtividadesByUsuario(
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
        Page<AtividadeResponseDTO> atividades = atividadeService.buscarPorUsuario(usuarioId, pageable);
        log.info("Retornando página {} de atividades para usuário ID: {} (total: {})", 
            atividades.getNumber(), usuarioId, atividades.getTotalElements());
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

