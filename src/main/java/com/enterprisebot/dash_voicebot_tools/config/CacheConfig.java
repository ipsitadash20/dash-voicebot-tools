package com.enterprisebot.dash_voicebot_tools.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean(name = "otpCache")
    public Cache<String, String> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Bean(name = "sessionCache")
    public Cache<String, Boolean> sessionCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .build();
    }
}