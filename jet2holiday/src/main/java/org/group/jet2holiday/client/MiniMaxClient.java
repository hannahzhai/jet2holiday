package org.group.jet2holiday.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MiniMaxClient {

    private final WebClient webClient;
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final long timeoutMs;

    public MiniMaxClient(WebClient.Builder builder,
                         @Value("${minimax.api.endpoint:https://api.minimax.chat/v1/chat/completions}") String endpoint,
                         @Value("${minimax.api.key:}") String apiKey,
                         @Value("${minimax.api.model:MiniMax-Text-01}") String model,
                         @Value("${minimax.api.timeout-ms:10000}") long timeoutMs) {
        this.webClient = builder.build();
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutMs = timeoutMs;
    }

    public String generateInsight(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException("MiniMax API key is missing.");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a concise portfolio analyst."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<?, ?> response = webClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofMillis(timeoutMs));

            if (response == null) {
                throw new ExternalApiException("MiniMax returned empty response.");
            }

            Object choicesObj = response.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                throw new ExternalApiException("MiniMax response missing choices.");
            }

            Object firstChoiceObj = choices.getFirst();
            if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
                throw new ExternalApiException("MiniMax response has invalid choice format.");
            }

            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map<?, ?> message)) {
                throw new ExternalApiException("MiniMax response missing message.");
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                throw new ExternalApiException("MiniMax response missing content.");
            }
            return contentObj.toString();
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalApiException("MiniMax API call failed", ex);
        }
    }

    public String modelName() {
        return model;
    }
}
