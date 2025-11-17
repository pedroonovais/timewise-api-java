package com.timewise.timewise.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timewise.timewise.dto.UsuarioRequestDTO;
import com.timewise.timewise.dto.UsuarioResponseDTO;
import com.timewise.timewise.mapper.UsuarioMapper;
import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pela lógica de negócio relacionada a Usuários
 */
@Service
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    /**
     * Lista todos os usuários cadastrados com paginação
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de usuários (sem senha)
     */
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        log.info("Listando usuários - página: {}, tamanho: {}, ordenação: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return usuarioRepository.findAll(pageable)
            .map(usuarioMapper::toResponseDTO);
    }

    /**
     * Busca um usuário pelo ID
     * @param id - ID do usuário a ser buscado
     * @return Usuário encontrado (sem senha)
     * @throws RuntimeException se o usuário não for encontrado ou se o ID for nulo
     */
    public UsuarioResponseDTO buscarPorId(Long id) {
        if (id == null) {
            log.warn("Tentativa de buscar usuário com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Buscando usuário por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", id);
                return new RuntimeException("Usuário não encontrado com ID: " + id);
            });
        return usuarioMapper.toResponseDTO(usuario);
    }

    /**
     * Busca um usuário pelo email
     * @param email - Email do usuário a ser buscado
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        log.info("Buscando usuário por email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Cria um novo usuário
     * @param usuarioDTO - Dados do usuário a ser criado
     * @return Usuário criado (sem senha)
     * @throws RuntimeException se o email já estiver cadastrado
     */
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO criar(UsuarioRequestDTO usuarioDTO) {
        log.info("Criando novo usuário com email: {}", usuarioDTO.getEmail());
        
        // Valida se o email já existe
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuarioDTO.getEmail());
        if (usuarioExistente.isPresent()) {
            log.warn("Tentativa de criar usuário com email já cadastrado: {}", usuarioDTO.getEmail());
            throw new RuntimeException("Email já cadastrado: " + usuarioDTO.getEmail());
        }
        
        // Converte DTO para entidade
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        if (usuario == null) {
            log.error("Erro ao converter DTO para entidade");
            throw new RuntimeException("Erro ao processar dados do usuário");
        }
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso. ID: {}", usuarioSalvo.getId());
        
        return usuarioMapper.toResponseDTO(usuarioSalvo);
    }

    /**
     * Atualiza um usuário existente
     * @param id - ID do usuário a ser atualizado
     * @param usuarioDTO - Dados atualizados do usuário
     * @return Usuário atualizado (sem senha)
     * @throws RuntimeException se o usuário não for encontrado ou se o email já estiver em uso por outro usuário
     */
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioDTO) {
        if (id == null) {
            log.warn("Tentativa de atualizar usuário com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Atualizando usuário com ID: {}", id);
        
        // Busca o usuário existente
        Usuario usuarioExistente = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", id);
                return new RuntimeException("Usuário não encontrado com ID: " + id);
            });
        
        // Valida se o email está sendo alterado e se já existe outro usuário com esse email
        if (!usuarioExistente.getEmail().equals(usuarioDTO.getEmail())) {
            Optional<Usuario> usuarioComEmail = usuarioRepository.findByEmail(usuarioDTO.getEmail());
            if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(id)) {
                log.warn("Tentativa de atualizar usuário com email já cadastrado: {}", usuarioDTO.getEmail());
                throw new RuntimeException("Email já cadastrado: " + usuarioDTO.getEmail());
            }
        }
        
        // Atualiza os campos usando o mapper
        usuarioMapper.updateEntityFromDTO(usuarioExistente, usuarioDTO);
        
        Usuario usuarioSalvo = usuarioRepository.save(usuarioExistente);
        log.info("Usuário atualizado com sucesso. ID: {}", usuarioSalvo.getId());
        return usuarioMapper.toResponseDTO(usuarioSalvo);
    }

    /**
     * Deleta um usuário pelo ID
     * @param id - ID do usuário a ser deletado
     * @throws RuntimeException se o usuário não for encontrado ou se o ID for nulo
     */
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void deletar(Long id) {
        if (id == null) {
            log.warn("Tentativa de deletar usuário com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Deletando usuário com ID: {}", id);
        
        // Verifica se o usuário existe antes de deletar
        if (!usuarioRepository.existsById(id)) {
            log.warn("Usuário não encontrado com ID: {}", id);
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        
        usuarioRepository.deleteById(id);
        log.info("Usuário deletado com sucesso. ID: {}", id);
    }
}

