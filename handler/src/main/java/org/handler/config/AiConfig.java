package org.handler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
