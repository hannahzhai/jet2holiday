package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.group.jet2holiday.client.FinnhubQuoteClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketDataRefreshResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class MarketDataService {

    private static final String SOURCE = "FINNHUB_QUOTE";
    private static final String DEFAULT_CURRENCY = "USD";

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final FinnhubQuoteClient finnhubQuoteClient;

    public MarketDataService(
            AccountRepository accountRepository,
            PortfolioItemRepository portfolioItemRepository,
            PriceSnapshotRepository priceSnapshotRepository,
            FinnhubQuoteClient finnhubQuoteClient
    ) {
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.finnhubQuoteClient = finnhubQuoteClient;
    }

    @Transactional
    public MarketDataRefreshResponse refreshAllHoldingsMarketData() {
        Account account = getCurrentAccount();
        List<PortfolioItem> holdings = portfolioItemRepository.findByAccountId(account.getId());
        LocalDate snapshotDate = LocalDate.now();

        if (holdings.isEmpty()) {
            return new MarketDataRefreshResponse(
                    true,
                    snapshotDate,
                    0,
                    List.of(),
                    List.of(),
                    "No holdings found for market data refresh."
            );
        }

        Set<String> symbols = new LinkedHashSet<>();
        for (PortfolioItem holding : holdings) {
            symbols.add(normalizeSymbol(holding.getSymbol()));
        }

        List<String> successSymbols = new ArrayList<>();
        List<String> failedSymbols = new ArrayList<>();

        for (String symbol : symbols) {
            try {
                BigDecimal latestPrice = finnhubQuoteClient.fetchQuote(symbol).orElse(null);
                if (latestPrice == null) {
                    failedSymbols.add(symbol);
                    continue;
                }
                upsertSnapshot(symbol, snapshotDate, latestPrice, DEFAULT_CURRENCY);
                successSymbols.add(symbol);
            } catch (ExternalApiException ex) {
                failedSymbols.add(symbol);
            }
        }

        boolean success = !successSymbols.isEmpty() && failedSymbols.isEmpty();
        String message;
        if (successSymbols.isEmpty()) {
            message = "Market data refresh failed for all symbols.";
        } else if (failedSymbols.isEmpty()) {
            message = "Market data refreshed successfully.";
        } else {
            message = "Market data refreshed partially. Some symbols failed.";
        }

        return new MarketDataRefreshResponse(
                success,
                snapshotDate,
                successSymbols.size(),
                successSymbols,
                failedSymbols,
                message
        );
    }

    @Transactional
    public LatestMarketDataResponse getLatestMarketData(String rawSymbol) {
        String symbol = normalizeSymbol(rawSymbol);
        BigDecimal realtimePrice = fetchRequiredRealtimePrice(symbol);
        PriceSnapshot snapshot = upsertSnapshot(symbol, LocalDate.now(), realtimePrice, DEFAULT_CURRENCY);
        return toResponse(snapshot);
    }

    @Transactional
    public List<LatestMarketDataResponse> getLatestMarketDataBatch(String symbolsCsv) {
        if (symbolsCsv == null || symbolsCsv.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "symbols query param must not be blank");
        }

        List<String> symbols = Arrays.stream(symbolsCsv.split(","))
                .map(this::normalizeSymbol)
                .distinct()
                .toList();

        List<LatestMarketDataResponse> responses = new ArrayList<>();
        LocalDate snapshotDate = LocalDate.now();
        for (String symbol : symbols) {
            BigDecimal realtimePrice = fetchRequiredRealtimePrice(symbol);
            PriceSnapshot snapshot = upsertSnapshot(symbol, snapshotDate, realtimePrice, DEFAULT_CURRENCY);
            responses.add(toResponse(snapshot));
        }

        return responses;
    }

    private BigDecimal fetchRequiredRealtimePrice(String symbol) {
        try {
            return finnhubQuoteClient.fetchQuote(symbol)
                    .orElseThrow(() -> new ResponseStatusException(BAD_GATEWAY, "Missing realtime market data for symbol: " + symbol));
        } catch (ExternalApiException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "Failed to fetch realtime market data from Finnhub.");
        }
    }

    private PriceSnapshot upsertSnapshot(String symbol, LocalDate snapshotDate, BigDecimal currentPrice, String currency) {
        PriceSnapshot snapshot = priceSnapshotRepository.findBySymbolAndSnapshotDate(symbol, snapshotDate)
                .orElseGet(PriceSnapshot::new);

        snapshot.setSymbol(symbol);
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setCurrentPrice(currentPrice);
        snapshot.setCurrency(currency);
        return priceSnapshotRepository.save(snapshot);
    }

    private LatestMarketDataResponse toResponse(PriceSnapshot snapshot) {
        return new LatestMarketDataResponse(
                snapshot.getSymbol(),
                snapshot.getSnapshotDate(),
                snapshot.getCurrentPrice(),
                snapshot.getCurrency(),
                SOURCE
        );
    }

    private Account getCurrentAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> accountRepository.save(new Account()));
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Symbol must not be blank");
        }
        return symbol.trim().toUpperCase();
    }
}

