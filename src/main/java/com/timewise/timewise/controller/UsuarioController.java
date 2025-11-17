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

import com.timewise.timewise.dto.UsuarioRequestDTO;
import com.timewise.timewise.dto.UsuarioResponseDTO;
import com.timewise.timewise.service.UsuarioService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/usuarios")
@Slf4j
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Lista todos os usuários", 
               description = "Retorna uma página de usuários cadastrados. Parâmetros: page (padrão: 0), size (padrão: 20, máximo: 100), sort (padrão: id,desc)")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @PageableDefault(page = 0, size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        // Valida tamanho máximo
        if (pageable.getPageSize() > 100) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                100, 
                pageable.getSort()
            );
        }
        Page<UsuarioResponseDTO> usuarios = usuarioService.listarTodos(pageable);
        log.info("Retornando página {} de usuários (total: {})", 
            usuarios.getNumber(), usuarios.getTotalElements());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário pelo ID", description = "Retorna um usuário cadastrado")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        log.info("Usuário encontrado com ID: {}", id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @Operation(summary = "Cria um novo usuário", description = "Cria um novo usuário com os dados fornecidos")
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        UsuarioResponseDTO usuarioCriado = usuarioService.criar(usuarioDTO);
        log.info("Usuário criado com sucesso. ID: {}", usuarioCriado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário existente", description = "Atualiza um usuário com os dados fornecidos")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable Long id, 
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizar(id, usuarioDTO);
        log.info("Usuário atualizado com sucesso. ID: {}", id);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um usuário existente", description = "Deleta um usuário com o ID fornecido")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletar(id);
        log.info("Usuário deletado com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
