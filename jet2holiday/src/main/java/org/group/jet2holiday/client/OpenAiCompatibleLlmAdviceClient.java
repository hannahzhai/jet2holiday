package org.group.jet2holiday.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.group.jet2holiday.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleLlmAdviceClient implements LlmAdviceClient {

    private static final String SYSTEM_PROMPT = "You are a portfolio analysis assistant. Return strict JSON with fields: summary (string), riskLevel (LOW|MEDIUM|HIGH), suggestions (array of short strings), riskWarnings (array of short strings). Do not include markdown code fences.";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final int timeoutMs;

    public OpenAiCompatibleLlmAdviceClient(
            ObjectMapper objectMapper,
            @Value("${llm.enabled:false}") boolean enabled,
            @Value("${llm.api.endpoint:https://api.openai.com/v1/chat/completions}") String endpoint,
            @Value("${llm.api.key:}") String apiKey,
            @Value("${llm.api.model:gpt-4o-mini}") String model,
            @Value("${llm.api.timeout-ms:12000}") int timeoutMs
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public LlmAdviceResult generateAdvice(String prompt) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException("LLM advice API is not enabled or missing key");
        }

        try {
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "model", model,
                            "temperature", 0.3,
                            "response_format", java.util.Map.of("type", "json_object"),
                            "messages", List.of(
                                    java.util.Map.of("role", "system", "content", SYSTEM_PROMPT),
                                    java.util.Map.of("role", "user", "content", prompt)
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(timeoutMs))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ExternalApiException("LLM API request failed, status=" + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new ExternalApiException("LLM API returned empty content");
            }

            JsonNode adviceNode = parseJsonContent(contentNode.asText());
            String summary = adviceNode.path("summary").asText("No summary returned.");
            String riskLevel = adviceNode.path("riskLevel").asText("MEDIUM");
            List<String> suggestions = readStringArray(adviceNode.path("suggestions"));
            List<String> riskWarnings = readStringArray(adviceNode.path("riskWarnings"));

            return new LlmAdviceResult(summary, riskLevel, suggestions, riskWarnings);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalApiException("LLM API call failed", ex);
        }
    }

    @Override
    public String modelName() {
        return model;
    }

    private JsonNode parseJsonContent(String content) throws IOException {
        String cleaned = content.trim();
        if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-zA-Z]*", "");
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        return objectMapper.readTree(cleaned);
    }

    private List<String> readStringArray(JsonNode arrayNode) {
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(arrayNode.spliterator(), false)
                .map(JsonNode::asText)
                .filter(text -> text != null && !text.isBlank())
                .toList();
    }
}

