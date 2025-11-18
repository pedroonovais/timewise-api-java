package com.timewise.timewise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timewise.timewise.auth.JwtService;
import com.timewise.timewise.dto.LoginRequestDTO;
import com.timewise.timewise.dto.LoginResponseDTO;
import com.timewise.timewise.dto.UsuarioResponseDTO;
import com.timewise.timewise.service.UsuarioService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints de autenticação
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint de login
     * Autentica o usuário e retorna um token JWT
     * @param loginRequest - Dados de login (email e senha)
     * @return Token JWT e informações do usuário
     */
    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário", 
               description = "Valida as credenciais e retorna um token JWT para autenticação")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Tentativa de login para email: {}", loginRequest.getEmail());
        
        // Autentica o usuário usando AuthenticationManager
        // Isso valida as credenciais usando o UserDetailsService e PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getSenha()
            )
        );
        
        // Se chegou aqui, a autenticação foi bem-sucedida
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Gera o token JWT
        String token = jwtService.generateToken(userDetails);
        
        // Busca informações completas do usuário usando o email
        // Como a autenticação foi bem-sucedida, o usuário existe
        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(loginRequest.getEmail())
            .map(u -> {
                try {
                    return usuarioService.buscarPorId(u.getId());
                } catch (Exception e) {
                    log.error("Erro ao buscar usuário por ID: {}", u.getId(), e);
                    throw new RuntimeException("Erro ao buscar informações do usuário");
                }
            })
            .orElseThrow(() -> {
                log.error("Usuário não encontrado após autenticação bem-sucedida: {}", loginRequest.getEmail());
                return new RuntimeException("Erro ao buscar informações do usuário");
            });
        
        log.info("Login bem-sucedido para usuário: {} (ID: {})", loginRequest.getEmail(), usuario.getId());
        
        LoginResponseDTO response = LoginResponseDTO.builder()
            .token(token)
            .usuario(usuario)
            .build();
        
        return ResponseEntity.ok(response);
    }
}

