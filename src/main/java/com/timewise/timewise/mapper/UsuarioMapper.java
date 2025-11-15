package com.timewise.timewise.mapper;

import org.springframework.stereotype.Component;

import com.timewise.timewise.dto.UsuarioRequestDTO;
import com.timewise.timewise.dto.UsuarioResponseDTO;
import com.timewise.timewise.model.Usuario;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper responsável por converter entre DTOs e entidades Usuario
 */
@Component
@Slf4j
public class UsuarioMapper {

    /**
     * Converte UsuarioRequestDTO para entidade Usuario
     * @param dto - DTO de requisição
     * @return Entidade Usuario
     */
    public Usuario toEntity(UsuarioRequestDTO dto) {
        log.debug("Convertendo UsuarioRequestDTO para entidade Usuario");
        if (dto == null) {
            return null;
        }
        
        return Usuario.builder()
            .nome(dto.getNome())
            .email(dto.getEmail())
            .senha(dto.getSenha())
            .build();
    }

    /**
     * Converte entidade Usuario para UsuarioResponseDTO
     * @param usuario - Entidade Usuario
     * @return DTO de resposta (sem senha)
     */
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        log.debug("Convertendo entidade Usuario para UsuarioResponseDTO");
        if (usuario == null) {
            return null;
        }
        
        return UsuarioResponseDTO.builder()
            .id(usuario.getId())
            .nome(usuario.getNome())
            .email(usuario.getEmail())
            .build();
    }

    /**
     * Atualiza uma entidade Usuario com dados do DTO
     * @param usuario - Entidade existente a ser atualizada
     * @param dto - DTO com novos dados
     */
    public void updateEntityFromDTO(Usuario usuario, UsuarioRequestDTO dto) {
        log.debug("Atualizando entidade Usuario com dados do DTO");
        if (usuario == null || dto == null) {
            return;
        }
        
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
    }
}

