package com.timewise.timewise.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
     * Score = (pausa / (trabalho + pausa)) * 100
     * Se trabalho + pausa = 0, score = 0
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

        // Calcula o score
        double totalHoras = totalTrabalhoHoras + totalPausaHoras;
        Integer score;
        
        if (totalHoras == 0) {
            score = 0;
        } else {
            // Score = (pausa / (trabalho + pausa)) * 100
            double scoreDouble = (totalPausaHoras / totalHoras) * 100;
            score = (int) Math.round(scoreDouble);
        }

        log.info("Score calculado: {}% (Trabalho: {}h, Pausa: {}h)", score, totalTrabalhoHoras, totalPausaHoras);
        return score;
    }

    /**
     * Lista todos os scores diários de um usuário
     * @param usuarioId - ID do usuário
     * @return Lista de scores diários
     */
    public List<ScoreDiarioResponseDTO> listarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            log.warn("Tentativa de listar scores com usuarioId nulo");
            throw new RuntimeException("ID do usuário não pode ser nulo");
        }
        log.info("Listando scores diários do usuário ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado com ID: {}", usuarioId);
                return new RuntimeException("Usuário não encontrado com ID: " + usuarioId);
            });

        return scoreDiarioRepository.findByUsuarioOrderByDataTrabalhoDesc(usuario).stream()
            .map(scoreDiarioMapper::toResponseDTO)
            .toList();
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

