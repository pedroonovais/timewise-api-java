package com.timewise.timewise.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timewise.timewise.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
}
