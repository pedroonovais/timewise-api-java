package com.timewise.timewise.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.timewise.timewise.model.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    /**
     * Busca todas as tarefas de um usuário
     * @param usuarioId - ID do usuário
     * @return Lista de tarefas encontradas
     */
    List<Tarefa> findByUsuarioId(Long usuarioId);
    
    /**
     * Busca todas as tarefas de um usuário com paginação
     * @param usuarioId - ID do usuário
     * @param pageable - Parâmetros de paginação
     * @return Página de tarefas encontradas
     */
    Page<Tarefa> findByUsuarioId(Long usuarioId, Pageable pageable);
}

