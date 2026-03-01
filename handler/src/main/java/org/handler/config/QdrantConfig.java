package org.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantConfig {
    @Bean
    public WebClient qdrantWebClient(
            @Value("${qdrant.url}") String url
    ) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }
}
