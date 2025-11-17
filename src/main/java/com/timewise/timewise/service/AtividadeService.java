package com.timewise.timewise.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private ScoreDiarioService scoreDiarioService;

    /**
     * Lista todas as atividades cadastradas com paginação
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de atividades
     */
    public Page<AtividadeResponseDTO> listarTodos(Pageable pageable) {
        log.info("Listando atividades - página: {}, tamanho: {}, ordenação: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return atividadeRepository.findAll(pageable)
            .map(atividadeMapper::toResponseDTO);
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
    @CacheEvict(value = {"atividades", "atividadesPorUsuario", "scoresPorUsuario"}, allEntries = true)
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
        
        // Calcula e atualiza o score diário do usuário para a data da atividade
        LocalDate dataAtividade = atividadeSalva.getTempoInicio().toLocalDate();
        try {
            scoreDiarioService.calcularESalvarScore(atividadeSalva.getUsuario().getId(), dataAtividade);
            log.info("Score diário atualizado para usuário ID: {} na data: {}", 
                atividadeSalva.getUsuario().getId(), dataAtividade);
        } catch (Exception e) {
            log.error("Erro ao calcular score diário após criar atividade: {}", e.getMessage());
            // Não interrompe o fluxo, apenas loga o erro
        }
        
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
    @CacheEvict(value = {"atividades", "atividadesPorUsuario", "scoresPorUsuario"}, allEntries = true)
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
        
        // Guarda a data antiga para recalcular o score se necessário
        LocalDate dataAntiga = atividadeExistente.getTempoInicio() != null ? 
            atividadeExistente.getTempoInicio().toLocalDate() : null;
        
        // Valida se tempoFim é posterior a tempoInicio
        if (atividadeDTO.getTempoFim().isBefore(atividadeDTO.getTempoInicio()) || 
            atividadeDTO.getTempoFim().isEqual(atividadeDTO.getTempoInicio())) {
            log.warn("Tentativa de atualizar atividade com tempoFim anterior ou igual a tempoInicio");
            throw new RuntimeException("O tempo de fim deve ser posterior ao tempo de início");
        }
        
        // Atualiza os campos usando o mapper
        atividadeMapper.updateEntityFromDTO(atividadeExistente, atividadeDTO);
        
        Atividade atividadeSalva = atividadeRepository.save(atividadeExistente);
        log.info("Atividade atualizada com sucesso. ID: {}", atividadeSalva.getId());
        
        // Calcula e atualiza o score diário para a data nova
        LocalDate dataNova = atividadeSalva.getTempoInicio().toLocalDate();
        try {
            scoreDiarioService.calcularESalvarScore(atividadeSalva.getUsuario().getId(), dataNova);
            log.info("Score diário atualizado para usuário ID: {} na data: {}", 
                atividadeSalva.getUsuario().getId(), dataNova);
            
            // Se a data mudou, recalcula também a data antiga
            if (dataAntiga != null && !dataAntiga.equals(dataNova)) {
                scoreDiarioService.calcularESalvarScore(atividadeSalva.getUsuario().getId(), dataAntiga);
                log.info("Score diário atualizado para data antiga: {}", dataAntiga);
            }
        } catch (Exception e) {
            log.error("Erro ao calcular score diário após atualizar atividade: {}", e.getMessage());
            // Não interrompe o fluxo, apenas loga o erro
        }
        
        return atividadeMapper.toResponseDTO(atividadeSalva);
    }

    /**
     * Deleta uma atividade pelo ID
     * @param id - ID da atividade a ser deletada
     * @throws RuntimeException se a atividade não for encontrada ou se o ID for nulo
     */
    @Transactional
    @CacheEvict(value = {"atividades", "atividadesPorUsuario", "scoresPorUsuario"}, allEntries = true)
    public void deletar(Long id) {
        if (id == null) {
            log.warn("Tentativa de deletar atividade com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Deletando atividade com ID: {}", id);
        
        // Busca a atividade para obter a data antes de deletar
        Atividade atividade = atividadeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Atividade não encontrada com ID: {}", id);
                return new RuntimeException("Atividade não encontrada com ID: " + id);
            });
        
        // Guarda informações para recalcular o score
        Long usuarioId = atividade.getUsuario() != null ? atividade.getUsuario().getId() : null;
        LocalDate dataAtividade = atividade.getTempoInicio() != null ? 
            atividade.getTempoInicio().toLocalDate() : null;
        
        atividadeRepository.deleteById(id);
        log.info("Atividade deletada com sucesso. ID: {}", id);
        
        // Recalcula o score diário após deletar a atividade
        if (usuarioId != null && dataAtividade != null) {
            try {
                scoreDiarioService.calcularESalvarScore(usuarioId, dataAtividade);
                log.info("Score diário atualizado após deletar atividade. Usuário ID: {}, Data: {}", 
                    usuarioId, dataAtividade);
            } catch (Exception e) {
                log.error("Erro ao calcular score diário após deletar atividade: {}", e.getMessage());
                // Não interrompe o fluxo, apenas loga o erro
            }
        }
    }

    /**
     * Busca todas as atividades de um usuário com paginação
     * @param usuarioId - ID do usuário
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de atividades encontradas
     */
    public Page<AtividadeResponseDTO> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        if (usuarioId == null) {
            log.warn("Tentativa de buscar atividades com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Buscando atividades para usuário ID: {} - página: {}, tamanho: {}", 
            usuarioId, pageable.getPageNumber(), pageable.getPageSize());
        return atividadeRepository.findByUsuarioId(usuarioId, pageable)
            .map(atividadeMapper::toResponseDTO);
    }
}

