package com.timewise.timewise.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuração para suporte a mensagens de validação em múltiplos idiomas
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configura o MessageSource para carregar mensagens de validação
     * Suporta português (pt_BR) e inglês (en)
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache por 1 hora
        return messageSource;
    }

    /**
     * Configura o validador para usar o MessageSource configurado
     * Isso permite que as mensagens de validação sejam internacionalizadas
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        MessageSource source = messageSource();
        if (source != null) {
            bean.setValidationMessageSource(source);
        }
        return bean;
    }
}

