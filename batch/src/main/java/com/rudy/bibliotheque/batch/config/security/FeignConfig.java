package com.rudy.bibliotheque.batch.config.security;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new AuthInterceptor();
    }
}
