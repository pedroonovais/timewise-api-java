package com.timewise.timewise.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timewise.timewise.dto.AtividadeRequestDTO;
import com.timewise.timewise.dto.AtividadeResponseDTO;
import com.timewise.timewise.mapper.AtividadeMapper;
import com.timewise.timewise.model.Atividade;
import com.timewise.timewise.repository.AtividadeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pela lógica de negócio relacionada a Atividades
 */
@Service
@Slf4j
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private AtividadeMapper atividadeMapper;

    /**
     * Lista todas as atividades cadastradas
     * @return Lista de todas as atividades
     */
    public List<AtividadeResponseDTO> listarTodos() {
        log.info("Listando todas as atividades");
        return atividadeRepository.findAll().stream()
            .map(atividadeMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Busca uma atividade pelo ID
     * @param id - ID da atividade a ser buscada
     * @return Atividade encontrada
     * @throws RuntimeException se a atividade não for encontrada ou se o ID for nulo
     */
    public AtividadeResponseDTO buscarPorId(Long id) {
        if (id == null) {
            log.warn("Tentativa de buscar atividade com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Buscando atividade por ID: {}", id);
        Atividade atividade = atividadeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Atividade não encontrada com ID: {}", id);
                return new RuntimeException("Atividade não encontrada com ID: " + id);
            });
        return atividadeMapper.toResponseDTO(atividade);
    }

    /**
     * Cria uma nova atividade
     * @param atividadeDTO - Dados da atividade a ser criada
     * @return Atividade criada
     * @throws RuntimeException se o usuário não for encontrado ou se houver erro de validação
     */
    @Transactional
    public AtividadeResponseDTO criar(AtividadeRequestDTO atividadeDTO) {
        log.info("Criando nova atividade para usuário ID: {}", atividadeDTO.getUsuarioId());
        
        // Valida se tempoFim é posterior a tempoInicio
        if (atividadeDTO.getTempoFim().isBefore(atividadeDTO.getTempoInicio()) || 
            atividadeDTO.getTempoFim().isEqual(atividadeDTO.getTempoInicio())) {
            log.warn("Tentativa de criar atividade com tempoFim anterior ou igual a tempoInicio");
            throw new RuntimeException("O tempo de fim deve ser posterior ao tempo de início");
        }
        
        // Converte DTO para entidade
        Atividade atividade = atividadeMapper.toEntity(atividadeDTO);
        if (atividade == null) {
            log.error("Erro ao converter DTO para entidade");
            throw new RuntimeException("Erro ao processar dados da atividade");
        }
        
        Atividade atividadeSalva = atividadeRepository.save(atividade);
        log.info("Atividade criada com sucesso. ID: {}", atividadeSalva.getId());
        
        return atividadeMapper.toResponseDTO(atividadeSalva);
    }

    /**
     * Atualiza uma atividade existente
     * @param id - ID da atividade a ser atualizada
     * @param atividadeDTO - Dados atualizados da atividade
     * @return Atividade atualizada
     * @throws RuntimeException se a atividade não for encontrada ou se houver erro de validação
     */
    @Transactional
    public AtividadeResponseDTO atualizar(Long id, AtividadeRequestDTO atividadeDTO) {
        if (id == null) {
            log.warn("Tentativa de atualizar atividade com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Atualizando atividade com ID: {}", id);
        
        // Busca a atividade existente
        Atividade atividadeExistente = atividadeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Atividade não encontrada com ID: {}", id);
                return new RuntimeException("Atividade não encontrada com ID: " + id);
            });
        
        // Valida se tempoFim é posterior a tempoInicio
        if (atividadeDTO.getTempoFim().isBefore(atividadeDTO.getTempoInicio()) || 
            atividadeDTO.getTempoFim().isEqual(atividadeDTO.getTempoInicio())) {
            log.warn("Tentativa de atualizar atividade com tempoFim anterior ou igual a tempoInicio");
            throw new RuntimeException("O tempo de fim deve ser posterior ao tempo de início");
        }
        
        // Atualiza os campos usando o mapper
        atividadeMapper.updateEntityFromDTO(atividadeExistente, atividadeDTO);
        
        if (atividadeExistente == null) {
            log.error("Erro: atividade existente é nula após atualização");
            throw new RuntimeException("Erro ao atualizar atividade");
        }
        
        Atividade atividadeSalva = atividadeRepository.save(atividadeExistente);
        log.info("Atividade atualizada com sucesso. ID: {}", atividadeSalva.getId());
        return atividadeMapper.toResponseDTO(atividadeSalva);
    }

    /**
     * Deleta uma atividade pelo ID
     * @param id - ID da atividade a ser deletada
     * @throws RuntimeException se a atividade não for encontrada ou se o ID for nulo
     */
    @Transactional
    public void deletar(Long id) {
        if (id == null) {
            log.warn("Tentativa de deletar atividade com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Deletando atividade com ID: {}", id);
        
        // Verifica se a atividade existe antes de deletar
        if (!atividadeRepository.existsById(id)) {
            log.warn("Atividade não encontrada com ID: {}", id);
            throw new RuntimeException("Atividade não encontrada com ID: " + id);
        }
        
        atividadeRepository.deleteById(id);
        log.info("Atividade deletada com sucesso. ID: {}", id);
    }
}

