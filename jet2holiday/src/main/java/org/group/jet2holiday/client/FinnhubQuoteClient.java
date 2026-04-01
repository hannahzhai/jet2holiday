package org.group.jet2holiday.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;
import org.group.jet2holiday.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FinnhubQuoteClient {

    private final String baseUrl;
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FinnhubQuoteClient(
            ObjectMapper objectMapper,
            @Value("${marketdata.finnhub.base-url:https://finnhub.io/api/v1}") String baseUrl,
            @Value("${marketdata.finnhub.api-key:}") String apiKey,
            @Value("${marketdata.finnhub.timeout-ms:8000}") int timeoutMs
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public Optional<BigDecimal> fetchQuote(String symbol) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException("Finnhub API key is missing");
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/quote")
                    .queryParam("symbol", symbol)
                    .queryParam("token", apiKey)
                    .build(true)
                    .toUri();

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ExternalApiException("Finnhub quote request failed, status=" + response.getStatusCode().value());
            }

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode currentPriceNode = root.path("c");
            if (currentPriceNode.isMissingNode() || currentPriceNode.isNull()) {
                return Optional.empty();
            }

            BigDecimal currentPrice = currentPriceNode.decimalValue();
            if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return Optional.empty();
            }
            return Optional.of(currentPrice);
        } catch (RestClientException ex) {
            throw new ExternalApiException("Finnhub quote API call failed", ex);
        } catch (Exception ex) {
            throw new ExternalApiException("Failed to parse Finnhub quote response", ex);
        }
    }
}

