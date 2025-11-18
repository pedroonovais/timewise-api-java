package com.timewise.timewise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.timewise.timewise.auth.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuração de segurança do Spring Security
 * Define endpoints públicos, privados e configura autenticação JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura o PasswordEncoder para criptografar senhas com BCrypt
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configurando BCryptPasswordEncoder para criptografia de senhas");
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura o AuthenticationProvider usando UserDetailsService e PasswordEncoder
     * @return DaoAuthenticationProvider
     */
    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        log.info("Configurando AuthenticationProvider");
        return authProvider;
    }

    /**
     * Configura o AuthenticationManager
     * @param config - AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("Configurando AuthenticationManager");
        return config.getAuthenticationManager();
    }

    /**
     * Configura a cadeia de filtros de segurança
     * Define quais endpoints são públicos e quais requerem autenticação
     * @param http - HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurando SecurityFilterChain");
        
        http
            // Desabilita CSRF (não necessário para API stateless com JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configura autenticação stateless (sem sessão)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configura autorização de endpoints
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/usuarios").permitAll() // POST para registro
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            
            // Configura o AuthenticationProvider
            .authenticationProvider(authenticationProvider())
            
            // Adiciona o filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.info("SecurityFilterChain configurado com sucesso");
        return http.build();
    }
}

