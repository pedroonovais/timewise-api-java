package com.timewise.timewise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timewise.timewise.dto.TarefaRequestDTO;
import com.timewise.timewise.dto.TarefaResponseDTO;
import com.timewise.timewise.mapper.TarefaMapper;
import com.timewise.timewise.model.Tarefa;
import com.timewise.timewise.repository.TarefaRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pela lógica de negócio relacionada a Tarefas
 */
@Service
@Slf4j
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private TarefaMapper tarefaMapper;

    /**
     * Lista todas as tarefas cadastradas com paginação
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de tarefas
     */
    public Page<TarefaResponseDTO> listarTodos(Pageable pageable) {
        log.info("Listando tarefas - página: {}, tamanho: {}, ordenação: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return tarefaRepository.findAll(pageable)
            .map(tarefaMapper::toResponseDTO);
    }

    /**
     * Busca uma tarefa pelo ID
     * @param id - ID da tarefa a ser buscada
     * @return Tarefa encontrada
     * @throws RuntimeException se a tarefa não for encontrada ou se o ID for nulo
     */
    public TarefaResponseDTO buscarPorId(Long id) {
        if (id == null) {
            log.warn("Tentativa de buscar tarefa com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Buscando tarefa por ID: {}", id);
        Tarefa tarefa = tarefaRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Tarefa não encontrada com ID: {}", id);
                return new RuntimeException("Tarefa não encontrada com ID: " + id);
            });
        return tarefaMapper.toResponseDTO(tarefa);
    }

    /**
     * Cria uma nova tarefa
     * @param tarefaDTO - Dados da tarefa a ser criada
     * @return Tarefa criada
     * @throws RuntimeException se o usuário não for encontrado ou se houver erro de validação
     */
    @Transactional
    @CacheEvict(value = {"tarefas", "tarefasPorUsuario"}, allEntries = true)
    public TarefaResponseDTO criar(TarefaRequestDTO tarefaDTO) {
        log.info("Criando nova tarefa para usuário ID: {}", tarefaDTO.getUsuarioId());
        
        try {
            // Converte DTO para entidade
            Tarefa tarefa = tarefaMapper.toEntity(tarefaDTO);
            if (tarefa == null) {
                log.error("Erro ao converter DTO para entidade");
                throw new RuntimeException("Erro ao processar dados da tarefa");
            }
            
            Tarefa tarefaSalva = tarefaRepository.save(tarefa);
            log.info("Tarefa criada com sucesso. ID: {}", tarefaSalva.getId());
            
            return tarefaMapper.toResponseDTO(tarefaSalva);
        } catch (RuntimeException e) {
            log.error("Erro ao criar tarefa: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao criar tarefa", e);
            throw new RuntimeException("Erro ao processar dados da tarefa", e);
        }
    }

    /**
     * Atualiza uma tarefa existente
     * @param id - ID da tarefa a ser atualizada
     * @param tarefaDTO - Dados atualizados da tarefa
     * @return Tarefa atualizada
     * @throws RuntimeException se a tarefa não for encontrada ou se o usuário não for encontrado
     */
    @Transactional
    @CacheEvict(value = {"tarefas", "tarefasPorUsuario"}, allEntries = true)
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO tarefaDTO) {
        if (id == null) {
            log.warn("Tentativa de atualizar tarefa com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Atualizando tarefa com ID: {}", id);
        
        // Busca a tarefa existente
        Tarefa tarefaExistente = tarefaRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Tarefa não encontrada com ID: {}", id);
                return new RuntimeException("Tarefa não encontrada com ID: " + id);
            });
        
        try {
            // Atualiza os campos usando o mapper
            tarefaMapper.updateEntityFromDTO(tarefaExistente, tarefaDTO);
            
            Tarefa tarefaSalva = tarefaRepository.save(tarefaExistente);
            log.info("Tarefa atualizada com sucesso. ID: {}", tarefaSalva.getId());
            return tarefaMapper.toResponseDTO(tarefaSalva);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar tarefa: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar tarefa", e);
            throw new RuntimeException("Erro ao atualizar tarefa", e);
        }
    }

    /**
     * Deleta uma tarefa pelo ID
     * @param id - ID da tarefa a ser deletada
     * @throws RuntimeException se a tarefa não for encontrada ou se o ID for nulo
     */
    @Transactional
    @CacheEvict(value = {"tarefas", "tarefasPorUsuario"}, allEntries = true)
    public void deletar(Long id) {
        if (id == null) {
            log.warn("Tentativa de deletar tarefa com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Deletando tarefa com ID: {}", id);
        
        // Verifica se a tarefa existe antes de deletar
        if (!tarefaRepository.existsById(id)) {
            log.warn("Tarefa não encontrada com ID: {}", id);
            throw new RuntimeException("Tarefa não encontrada com ID: " + id);
        }
        
        tarefaRepository.deleteById(id);
        log.info("Tarefa deletada com sucesso. ID: {}", id);
    }

    /**
     * Busca todas as tarefas de um usuário com paginação
     * @param usuarioId - ID do usuário
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de tarefas encontradas
     */
    public Page<TarefaResponseDTO> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        if (usuarioId == null) {
            log.warn("Tentativa de buscar tarefas com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Buscando tarefas para usuário ID: {} - página: {}, tamanho: {}", 
            usuarioId, pageable.getPageNumber(), pageable.getPageSize());
        return tarefaRepository.findByUsuarioId(usuarioId, pageable)
            .map(tarefaMapper::toResponseDTO);
    }
}

