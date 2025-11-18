package com.timewise.timewise.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timewise.timewise.dto.TarefaRequestDTO;
import com.timewise.timewise.dto.TarefaResponseDTO;
import com.timewise.timewise.model.Tarefa;
import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper responsável por converter entre DTOs e entidades Tarefa
 */
@Component
@Slf4j
public class TarefaMapper {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Converte TarefaRequestDTO para entidade Tarefa
     * @param dto - DTO de requisição
     * @return Entidade Tarefa
     * @throws RuntimeException se o usuário não for encontrado
     */
    public Tarefa toEntity(TarefaRequestDTO dto) {
        log.debug("Convertendo TarefaRequestDTO para entidade Tarefa");
        if (dto == null) {
            return null;
        }
        
        Long usuarioId = dto.getUsuarioId();
        if (usuarioId == null) {
            log.warn("Tentativa de criar tarefa com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        
        // Busca o usuário pelo ID
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });
        
        return Tarefa.builder()
            .nome(dto.getNome())
            .descricao(dto.getDescricao())
            .usuario(usuario)
            .build();
    }

    /**
     * Converte entidade Tarefa para TarefaResponseDTO
     * @param tarefa - Entidade Tarefa
     * @return DTO de resposta
     */
    public TarefaResponseDTO toResponseDTO(Tarefa tarefa) {
        log.debug("Convertendo entidade Tarefa para TarefaResponseDTO");
        if (tarefa == null) {
            return null;
        }
        
        Long usuarioId = tarefa.getUsuario() != null ? tarefa.getUsuario().getId() : null;
        return TarefaResponseDTO.builder()
            .id(tarefa.getId())
            .nome(tarefa.getNome())
            .descricao(tarefa.getDescricao())
            .usuarioId(usuarioId)
            .build();
    }

    /**
     * Atualiza uma entidade Tarefa com dados do DTO
     * @param tarefa - Entidade existente a ser atualizada
     * @param dto - DTO com novos dados
     */
    public void updateEntityFromDTO(Tarefa tarefa, TarefaRequestDTO dto) {
        log.debug("Atualizando entidade Tarefa com dados do DTO");
        if (tarefa == null || dto == null) {
            return;
        }
        
        tarefa.setNome(dto.getNome());
        tarefa.setDescricao(dto.getDescricao());
        
        // Atualiza o usuário se fornecido
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com ID: {}", dto.getUsuarioId());
                    return new RuntimeException("Usuário não encontrado com ID: " + dto.getUsuarioId());
                });
            tarefa.setUsuario(usuario);
        }
    }
}

