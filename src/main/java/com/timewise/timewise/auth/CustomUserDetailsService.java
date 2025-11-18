package com.timewise.timewise.auth;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço customizado para carregar detalhes do usuário para autenticação
 * Implementa UserDetailsService do Spring Security
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carrega usuário pelo email (username)
     * @param email - Email do usuário
     * @return UserDetails com informações do usuário
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando usuário pelo email: {}", email);
        
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        
        if (usuarioOptional.isEmpty()) {
            log.warn("Usuário não encontrado com email: {}", email);
            throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
        }
        
        Usuario usuario = usuarioOptional.get();
        log.debug("Usuário encontrado: {} (ID: {})", usuario.getEmail(), usuario.getId());
        
        // Retorna UserDetails com email como username, senha criptografada e authorities vazias
        // (não estamos usando roles/permissões, apenas autenticação básica)
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(new ArrayList<>()) // Sem roles/permissões
                .build();
    }
}

