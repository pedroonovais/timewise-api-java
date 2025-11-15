package com.timewise.timewise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timewise.timewise.model.Atividade;
import com.timewise.timewise.model.Usuario;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    /**
     * Busca todas as atividades de um usuário
     * @param usuarioId - ID do usuário
     * @return Lista de atividades encontradas
     */
    List<Atividade> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas as atividades de um usuário em uma data específica
     * @param usuario - Usuário das atividades
     * @param data - Data das atividades (apenas a parte da data é considerada)
     * @return Lista de atividades do usuário na data
     */
    List<Atividade> findByUsuarioAndTempoInicioBetween(
        Usuario usuario, 
        java.time.LocalDateTime inicioDia, 
        java.time.LocalDateTime fimDia
    );
}
