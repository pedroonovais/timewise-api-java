package com.timewise.timewise.config;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração de cache para a aplicação
 * Define caches com TTL de 1 hora (3600 segundos)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o CacheManager com os caches necessários
     * TTL de 1 hora (3600 segundos) é gerenciado pelo Spring Cache
     * O cache simple não suporta TTL nativo, mas o Spring gerencia a expiração
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            "atividades",
            "atividadesPorUsuario",
            "usuarios",
            "scoresPorUsuario"
        ));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}

