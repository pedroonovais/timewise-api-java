package com.timewise.timewise.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timewise.timewise.dto.ScoreDiarioResponseDTO;
import com.timewise.timewise.enums.AtividadeTipo;
import com.timewise.timewise.mapper.ScoreDiarioMapper;
import com.timewise.timewise.model.Atividade;
import com.timewise.timewise.model.ScoreDiario;
import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.AtividadeRepository;
import com.timewise.timewise.repository.ScoreDiarioRepository;
import com.timewise.timewise.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pela lógica de negócio relacionada a ScoreDiario
 */
@Service
@Slf4j
public class ScoreDiarioService {

    @Autowired
    private ScoreDiarioRepository scoreDiarioRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ScoreDiarioMapper scoreDiarioMapper;

    /**
     * Calcula e salva/atualiza o score diário de um usuário para uma data específica
     * @param usuarioId - ID do usuário
     * @param dataTrabalho - Data do trabalho
     * @return ScoreDiario calculado e salvo
     */
    @Transactional
    @CacheEvict(value = "scoresPorUsuario", key = "#usuarioId")
    public ScoreDiario calcularESalvarScore(Long usuarioId, LocalDate dataTrabalho) {
        if (usuarioId == null) {
            log.warn("Tentativa de calcular score com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Calculando score diário para usuário ID: {} na data: {}", usuarioId, dataTrabalho);

        // Busca o usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });

        // Calcula o score
        Integer score = calcularScore(usuario, dataTrabalho);

        // Busca ou cria o score diário
        Optional<ScoreDiario> scoreExistente = scoreDiarioRepository.findByUsuarioAndDataTrabalho(usuario, dataTrabalho);
        
        final ScoreDiario scoreDiario;
        if (scoreExistente.isPresent()) {
            scoreDiario = scoreExistente.get();
            scoreDiario.setValor(score);
            log.info("Atualizando score diário existente. ID: {}", scoreDiario.getId());
        } else {
            scoreDiario = ScoreDiario.builder()
                .usuario(usuario)
                .dataTrabalho(dataTrabalho)
                .valor(score)
                .build();
            log.info("Criando novo score diário");
        }

        // Garante que scoreDiario não é null antes de salvar
        if (scoreDiario == null) {
            log.error("Erro: scoreDiario é nulo após criação/atualização");
            throw new RuntimeException("Erro ao processar score diário");
        }
        
        ScoreDiario scoreSalvo = scoreDiarioRepository.save(scoreDiario);
        log.info("Score diário salvo com sucesso. ID: {}, Score: {}", scoreSalvo.getId(), scoreSalvo.getValor());
        
        return scoreSalvo;
    }

    /**
     * Calcula o score diário baseado nas atividades do usuário na data
     * Score baseado em meta de horas de trabalho (6-8h = score alto)
     * Com bônus se tiver pausas adequadas (10-20% do tempo)
     * @param usuario - Usuário para calcular o score
     * @param dataTrabalho - Data do trabalho
     * @return Score calculado (0-100)
     */
    private Integer calcularScore(Usuario usuario, LocalDate dataTrabalho) {
        log.debug("Calculando score para usuário ID: {} na data: {}", usuario.getId(), dataTrabalho);

        // Define o início e fim do dia
        LocalDateTime inicioDia = dataTrabalho.atStartOfDay();
        LocalDateTime fimDia = dataTrabalho.atTime(23, 59, 59, 999999999);

        // Busca todas as atividades do usuário no dia
        List<Atividade> atividades = atividadeRepository.findByUsuarioAndTempoInicioBetween(
            usuario, inicioDia, fimDia
        );

        double totalTrabalhoHoras = 0.0;
        double totalPausaHoras = 0.0;

        // Soma os tempos de trabalho e pausa
        for (Atividade atividade : atividades) {
            if (atividade.getTempoInicio() == null || atividade.getTempoFim() == null) {
                log.warn("Atividade com tempo nulo ignorada. ID: {}", atividade.getId());
                continue;
            }

            // Calcula a duração em horas
            Duration duracao = Duration.between(atividade.getTempoInicio(), atividade.getTempoFim());
            double horas = duracao.toMinutes() / 60.0; // Converte minutos para horas

            if (atividade.getTipo() == AtividadeTipo.TRABALHO) {
                totalTrabalhoHoras += horas;
            } else if (atividade.getTipo() == AtividadeTipo.PAUSA) {
                totalPausaHoras += horas;
            }
        }

        log.debug("Total trabalho: {} horas, Total pausa: {} horas", totalTrabalhoHoras, totalPausaHoras);

        // Calcula o score baseado em meta de horas de trabalho
        double totalHoras = totalTrabalhoHoras + totalPausaHoras;
        Integer score;
        
        if (totalHoras == 0) {
            score = 0;
        } else {
            // Meta de horas de trabalho ideal: 6-8 horas
            double metaMinima = 6.0;
            double metaMaxima = 8.0;
            double metaIdeal = 7.0; // Ponto ideal no meio do range
            
            // Calcula o score baseado nas horas trabalhadas
            double scoreBase;
            if (totalTrabalhoHoras >= metaMinima && totalTrabalhoHoras <= metaMaxima) {
                // Dentro da meta ideal: score alto (70-90 pontos)
                // Quanto mais próximo de 7h, maior o score
                double distanciaDoIdeal = Math.abs(totalTrabalhoHoras - metaIdeal);
                double scoreMaximo = 90.0;
                scoreBase = scoreMaximo - (distanciaDoIdeal * 10.0); // Reduz 10 pontos por hora de distância
            } else if (totalTrabalhoHoras < metaMinima) {
                // Abaixo da meta: score proporcional (0-70 pontos)
                scoreBase = (totalTrabalhoHoras / metaMinima) * 70.0;
            } else {
                // Acima da meta: score reduzido (acima de 8h começa a reduzir)
                // Máximo de 90 pontos, reduz 5 pontos por hora acima de 8h
                double horasAcima = totalTrabalhoHoras - metaMaxima;
                scoreBase = 90.0 - (horasAcima * 5.0);
                if (scoreBase < 0) scoreBase = 0;
            }
            
            // Calcula a proporção de pausa
            double proporcaoPausa = (totalPausaHoras / totalHoras) * 100;
            
            // Bônus para pausas adequadas (entre 10% e 20% do tempo)
            // Ideal: 15% de pausa
            double bonus = 0.0;
            if (proporcaoPausa >= 10.0 && proporcaoPausa <= 20.0) {
                // Bônus máximo de 10 pontos se estiver no range ideal (15%)
                double distanciaDoIdeal = Math.abs(proporcaoPausa - 15.0);
                bonus = 10.0 - (distanciaDoIdeal * 1.0); // Bônus decresce conforme distancia do ideal
                if (bonus < 0) bonus = 0;
            }
            
            // Score final = score base + bônus (limitado a 100)
            double scoreFinal = scoreBase + bonus;
            score = (int) Math.round(Math.min(Math.max(scoreFinal, 0.0), 100.0));
        }

        double proporcaoPausaPercentual = totalHoras > 0 ? (totalPausaHoras / totalHoras) * 100 : 0.0;
        log.info("Score calculado: {}% (Trabalho: {}h, Pausa: {}h, Proporção pausa: {}%)", 
            score, totalTrabalhoHoras, totalPausaHoras, String.format("%.2f", proporcaoPausaPercentual));
        return score;
    }

    /**
     * Lista todos os scores diários de um usuário com paginação
     * @param usuarioId - ID do usuário
     * @param pageable - Parâmetros de paginação (page, size, sort)
     * @return Página de scores diários
     */
    public Page<ScoreDiarioResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        if (usuarioId == null) {
            log.warn("Tentativa de listar scores com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Listando scores diários do usuário ID: {} - página: {}, tamanho: {}", 
            usuarioId, pageable.getPageNumber(), pageable.getPageSize());
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });

        return scoreDiarioRepository.findByUsuarioOrderByDataTrabalhoDesc(usuario, pageable)
            .map(scoreDiarioMapper::toResponseDTO);
    }

    /**
     * Busca um score diário por ID
     * @param id - ID do score diário
     * @return Score diário encontrado
     */
    public ScoreDiarioResponseDTO buscarPorId(Long id) {
        if (id == null) {
            log.warn("Tentativa de buscar score diário com ID nulo");
            throw new RuntimeException("ID não pode ser nulo");
        }
        log.info("Buscando score diário por ID: {}", id);
        
        ScoreDiario scoreDiario = scoreDiarioRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Score diário não encontrado com ID: {}", id);
                return new RuntimeException("Score diário não encontrado com ID: " + id);
            });
        
        return scoreDiarioMapper.toResponseDTO(scoreDiario);
    }

    /**
     * Busca um score diário por usuário e data
     * @param usuarioId - ID do usuário
     * @param dataTrabalho - Data do trabalho
     * @return Score diário encontrado
     */
    public ScoreDiarioResponseDTO buscarPorUsuarioEData(Long usuarioId, LocalDate dataTrabalho) {
        if (usuarioId == null) {
            log.warn("Tentativa de buscar score com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Buscando score diário do usuário ID: {} na data: {}", usuarioId, dataTrabalho);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });

        ScoreDiario scoreDiario = scoreDiarioRepository.findByUsuarioAndDataTrabalho(usuario, dataTrabalho)
            .orElseThrow(() -> {
                log.warn("Score diário não encontrado para usuário ID: {} na data: {}", usuarioId, dataTrabalho);
                return new RuntimeException("Score diário não encontrado para a data: " + dataTrabalho);
            });

        return scoreDiarioMapper.toResponseDTO(scoreDiario);
    }
}

