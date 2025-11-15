package com.timewise.timewise.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timewise.timewise.model.ScoreDiario;
import com.timewise.timewise.model.Usuario;

public interface ScoreDiarioRepository extends JpaRepository<ScoreDiario, Long> {
    
    /**
     * Busca um score diário por usuário e data
     * @param usuario - Usuário do score
     * @param dataTrabalho - Data do trabalho
     * @return Optional contendo o score se encontrado
     */
    Optional<ScoreDiario> findByUsuarioAndDataTrabalho(Usuario usuario, LocalDate dataTrabalho);
    
    /**
     * Busca todos os scores diários de um usuário ordenados por data (mais recente primeiro)
     * @param usuario - Usuário dos scores
     * @return Lista de scores diários ordenados por data
     */
    List<ScoreDiario> findByUsuarioOrderByDataTrabalhoDesc(Usuario usuario);
}

