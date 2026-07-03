package com.ruoyi.crm.ai.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class AiRestTemplateConfig {
    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate(AiConfig aiConfig) {
        return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(aiConfig.getTimeout()))
            .setReadTimeout(Duration.ofMillis(aiConfig.getTimeout()))
            .build();
    }
}
