package org.group.jet2holiday.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OllamaClient {

    private final WebClient webClient;

    public OllamaClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:11434").build(); // Ollama 默认端口
    }

    public String askModel(String question) {

        Map<String, Object> body = Map.of(
                "model", "llama3.2:latest",  // 直接用你本地的模型
                "prompt", question,
                "stream", false
        );

        Map response = webClient.post()
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
}
