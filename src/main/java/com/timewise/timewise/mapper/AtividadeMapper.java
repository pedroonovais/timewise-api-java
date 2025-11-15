package com.timewise.timewise.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timewise.timewise.dto.AtividadeRequestDTO;
import com.timewise.timewise.dto.AtividadeResponseDTO;
import com.timewise.timewise.model.Atividade;
import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper responsável por converter entre DTOs e entidades Atividade
 */
@Component
@Slf4j
public class AtividadeMapper {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Converte AtividadeRequestDTO para entidade Atividade
     * @param dto - DTO de requisição
     * @return Entidade Atividade
     * @throws RuntimeException se o usuário não for encontrado
     */
    public Atividade toEntity(AtividadeRequestDTO dto) {
        log.debug("Convertendo AtividadeRequestDTO para entidade Atividade");
        if (dto == null) {
            return null;
        }
        
        Long usuarioId = dto.getUsuarioId();
        if (usuarioId == null) {
            log.warn("Tentativa de criar atividade com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        
        // Busca o usuário pelo ID
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });
        
        return Atividade.builder()
            .nome(dto.getNome())
            .usuario(usuario)
            .tempoInicio(dto.getTempoInicio())
            .tempoFim(dto.getTempoFim())
            .tipo(dto.getTipo())
            .build();
    }

    /**
     * Converte entidade Atividade para AtividadeResponseDTO
     * @param atividade - Entidade Atividade
     * @return DTO de resposta
     */
    public AtividadeResponseDTO toResponseDTO(Atividade atividade) {
        log.debug("Convertendo entidade Atividade para AtividadeResponseDTO");
        if (atividade == null) {
            return null;
        }
        
        return AtividadeResponseDTO.builder()
            .id(atividade.getId())
            .nome(atividade.getNome())
            .usuarioId(atividade.getUsuario() != null ? atividade.getUsuario().getId() : null)
            .tempoInicio(atividade.getTempoInicio())
            .tempoFim(atividade.getTempoFim())
            .tipo(atividade.getTipo())
            .build();
    }

    /**
     * Atualiza uma entidade Atividade com dados do DTO
     * @param atividade - Entidade existente a ser atualizada
     * @param dto - DTO com novos dados
     */
    public void updateEntityFromDTO(Atividade atividade, AtividadeRequestDTO dto) {
        log.debug("Atualizando entidade Atividade com dados do DTO");
        if (atividade == null || dto == null) {
            return;
        }
        
        // Busca o usuário pelo ID se foi alterado
        if (dto.getUsuarioId() != null && 
            (atividade.getUsuario() == null || !atividade.getUsuario().getId().equals(dto.getUsuarioId()))) {
            Long usuarioId = dto.getUsuarioId();
            if (usuarioId == null) {
                log.warn("Tentativa de atualizar atividade com usuarioId nulo");
                throw new RuntimeException("ID do usuário não pode ser nulo");
            }
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com ID: {}", usuarioId);
                    return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
                });
            atividade.setUsuario(usuario);
        }
        
        atividade.setNome(dto.getNome());
        atividade.setTempoInicio(dto.getTempoInicio());
        atividade.setTempoFim(dto.getTempoFim());
        atividade.setTipo(dto.getTipo());
    }
}

