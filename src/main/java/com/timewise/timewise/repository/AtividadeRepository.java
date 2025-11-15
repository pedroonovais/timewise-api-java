package com.timewise.timewise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timewise.timewise.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
}
