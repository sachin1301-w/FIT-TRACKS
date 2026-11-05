
package com.fitness.aiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqService {

    private final WebClient webClient;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.key}")
    private String groqApiKey;

    public GroqService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getRecommendations(String details) {

        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-20b",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", details
                        )
                ),
                "max_completion_tokens", 1200,
                "temperature", 0.3
        );

        Map<?, ?> response = webClient.post()
                .uri(groqApiUrl)
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null) {
            throw new RuntimeException("Groq returned an empty response");
        }

        Object choicesObject = response.get("choices");

        if (!(choicesObject instanceof List<?> choices)
                || choices.isEmpty()
                || !(choices.get(0) instanceof Map<?, ?> choice)) {
            log.error("Groq response has no choices: {}", response);
            throw new RuntimeException("Groq returned no choices");
        }

        Object messageObject = choice.get("message");

        if (!(messageObject instanceof Map<?, ?> message)) {
            throw new RuntimeException("Groq response has no message");
        }

        Object contentObject = message.get("content");

        if (!(contentObject instanceof String content)
                || content.isBlank()) {
            throw new RuntimeException("Groq returned empty content");
        }

        Object finishReason = choice.get("finish_reason");

        log.info("Groq finish reason: {}", finishReason);
        log.info("Groq generated {} characters", content.length());

        if ("length".equals(finishReason)) {
            log.warn(
                    "Groq stopped because the output token limit was reached. "
                            + "The generated JSON may be incomplete."
            );
        }

        return content;
    }
}
