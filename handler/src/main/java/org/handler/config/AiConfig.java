package org.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Component
@Configuration
@ConfigurationProperties(prefix = "custom.ai")
public class AiConfig {
    private Map<String, String> prompt;

    public void setPrompt(Map<String, String> prompt) {
        this.prompt = prompt;
    }

    public Optional<String> getPrompt(String key) {
        return Optional.ofNullable(prompt.get(key));
    }
}
