package com.timewise.timewise.auth;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por gerar, validar e extrair informações de tokens JWT
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Extrai o email (username) do token JWT
     * @param token - Token JWT
     * @return Email do usuário
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token
     * @param token - Token JWT
     * @return Data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai uma claim específica do token
     * @param token - Token JWT
     * @param claimsResolver - Função para extrair a claim desejada
     * @return Valor da claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrai todas as claims do token
     * @param token - Token JWT
     * @return Claims do token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gera um token JWT para o usuário
     * @param userDetails - Detalhes do usuário
     * @return Token JWT
     */
    public String generateToken(UserDetails userDetails) {
        return createToken(null, userDetails.getUsername());
    }

    /**
     * Cria um token JWT com claims e subject
     * @param claims - Claims adicionais (não usado, mas mantido para compatibilidade)
     * @param subject - Subject (email do usuário)
     * @return Token JWT
     */
    @SuppressWarnings("unused")
    private String createToken(java.util.Map<String, Object> claims, String subject) {
        log.debug("Gerando token JWT para usuário: {}", subject);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida se o token é válido para o usuário
     * @param token - Token JWT
     * @param userDetails - Detalhes do usuário
     * @return true se o token é válido
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        log.debug("Validação do token para usuário {}: {}", username, isValid ? "válido" : "inválido");
        return isValid;
    }

    /**
     * Verifica se o token está expirado
     * @param token - Token JWT
     * @return true se o token está expirado
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Obtém a chave de assinatura a partir da secret key
     * @return Chave de assinatura
     */
    private SecretKey getSigningKey() {
        // Converte a string da secret key em bytes
        // Para HS256, precisa de pelo menos 256 bits (32 bytes)
        byte[] keyBytes = secretKey.getBytes();
        // Garante que a chave tenha pelo menos 32 bytes
        if (keyBytes.length < 32) {
            // Se for menor, repete a chave até ter 32 bytes
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        } else if (keyBytes.length > 32) {
            // Se for maior, trunca para 32 bytes
            byte[] truncatedKey = new byte[32];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
            keyBytes = truncatedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

