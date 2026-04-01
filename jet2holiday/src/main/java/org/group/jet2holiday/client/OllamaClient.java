package org.group.jet2holiday.client;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OllamaClient {

    private final WebClient webClient;
    private final String model;
    private final long timeoutMs;

    public OllamaClient(WebClient.Builder builder,
                        @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
                        @Value("${ollama.model:llama3.2:latest}") String model,
                        @Value("${ollama.timeout-ms:10000}") long timeoutMs) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.model = model;
        this.timeoutMs = timeoutMs;
    }

    public String askModel(String question) {
        Map<String, Object> body = Map.of(
                "model", "llama3.2:latest",  // 直接用你本地的模型
                "prompt", question,
                "stream", false
        );

        Map<?, ?> response = webClient.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.get("response") != null) {
            return response.get("response").toString();
        }
        return "No response";
    }

    public String modelName() {
        return model;
    }
}
