package org.group.jet2holiday.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Component
public class YahooFinanceClient {

    private static final int MAX_RETRY = 3;
    private static final long BASE_BACKOFF_MS = 800L;
    private static final long PER_SYMBOL_DELAY_MS = 350L;
    private static final String YAHOO_QUOTE_FALLBACK_URL = "https://query2.finance.yahoo.com/v7/finance/quote";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YahooFinanceClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, BigDecimal> fetchLatestPrices(Set<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Map.of();
        }

        String[] symbolArray = symbols.toArray(new String[0]);
        Map<String, BigDecimal> prices = new HashMap<>();

        Map<String, Stock> quoteMap = fetchBatchWithRetry(symbolArray);

        if (quoteMap != null && !quoteMap.isEmpty()) {
            for (String symbol : symbols) {
                Stock stock = quoteMap.get(symbol);
                if (stock == null || stock.getQuote() == null || stock.getQuote().getPrice() == null) {
                    continue;
                }
                prices.put(symbol, stock.getQuote().getPrice());
            }
        }

        for (String symbol : symbols) {
            if (prices.containsKey(symbol)) {
                continue;
            }
            BigDecimal singlePrice = fetchSingleWithRetry(symbol);
            if (singlePrice != null) {
                prices.put(symbol, singlePrice);
            }
            sleepQuietly(PER_SYMBOL_DELAY_MS);
        }

        return prices;
    }

    private Map<String, Stock> fetchBatchWithRetry(String[] symbolArray) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return YahooFinance.get(symbolArray);
            } catch (Exception ex) {
                if (!isRateLimited(ex) || attempt == MAX_RETRY) {
                    return Map.of();
                }
                sleepQuietly(backoffMs(attempt));
            }
        }
        return Map.of();
    }

    private BigDecimal fetchSingleWithRetry(String symbol) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                Stock stock = YahooFinance.get(symbol);
                if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                    return stock.getQuote().getPrice();
                }

                BigDecimal fallbackPrice = fetchFromYahooQuoteEndpoint(symbol);
                if (fallbackPrice != null) {
                    return fallbackPrice;
                }
                return null;
            } catch (Exception ex) {
                if (!isRateLimited(ex) || attempt == MAX_RETRY) {
                    BigDecimal fallbackPrice = fetchFromYahooQuoteEndpoint(symbol);
                    if (fallbackPrice != null) {
                        return fallbackPrice;
                    }
                    return null;
                }
                sleepQuietly(backoffMs(attempt));
            }
        }
        return fetchFromYahooQuoteEndpoint(symbol);
    }

    private BigDecimal fetchFromYahooQuoteEndpoint(String symbol) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(YAHOO_QUOTE_FALLBACK_URL)
                    .queryParam("symbols", symbol)
                    .build(true)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(MediaType.parseMediaTypes("application/json"));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Referer", "https://finance.yahoo.com/");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode resultNode = root.path("quoteResponse").path("result");
            if (!resultNode.isArray() || resultNode.isEmpty()) {
                return null;
            }

            JsonNode first = resultNode.get(0);
            JsonNode priceNode = first.path("regularMarketPrice");
            if (priceNode.isMissingNode() || priceNode.isNull()) {
                return null;
            }
            return priceNode.decimalValue();
        } catch (RestClientException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isRateLimited(Exception ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("429")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private long backoffMs(int attempt) {
        return BASE_BACKOFF_MS * attempt;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}