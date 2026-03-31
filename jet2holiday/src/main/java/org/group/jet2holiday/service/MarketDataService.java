package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.group.jet2holiday.client.YahooFinanceClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketDataRefreshResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class MarketDataService {

    private static final String SOURCE = "YAHOO_FINANCE";
    private static final String DEFAULT_CURRENCY = "USD";

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final YahooFinanceClient yahooFinanceClient;

    public MarketDataService(
            AccountRepository accountRepository,
            PortfolioItemRepository portfolioItemRepository,
            PriceSnapshotRepository priceSnapshotRepository,
            YahooFinanceClient yahooFinanceClient
    ) {
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.yahooFinanceClient = yahooFinanceClient;
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

        Set<String> uniqueSymbols = new LinkedHashSet<>();
        for (PortfolioItem holding : holdings) {
            uniqueSymbols.add(normalizeSymbol(holding.getSymbol()));
        }

        List<String> successSymbols = new ArrayList<>();
        List<String> failedSymbols = new ArrayList<>(uniqueSymbols);

        try {
            Map<String, BigDecimal> latestPrices = yahooFinanceClient.fetchLatestPrices(uniqueSymbols);
            for (Map.Entry<String, BigDecimal> entry : latestPrices.entrySet()) {
                String symbol = entry.getKey();
                BigDecimal latestPrice = entry.getValue();
                if (latestPrice == null) {
                    continue;
                }
                upsertSnapshot(symbol, snapshotDate, latestPrice, DEFAULT_CURRENCY);
                successSymbols.add(symbol);
            }
            failedSymbols.removeAll(successSymbols);
        } catch (ExternalApiException ex) {
            // Keep all symbols in failed list when upstream provider request fails.
        }

        String message;
        boolean success;
        if (successSymbols.isEmpty()) {
            success = false;
            message = "Market data refresh failed for all symbols.";
        } else if (failedSymbols.isEmpty()) {
            success = true;
            message = "Market data refreshed successfully.";
        } else {
            success = false;
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

    @Transactional(readOnly = true)
    public LatestMarketDataResponse getLatestMarketData(String rawSymbol) {
        String symbol = normalizeSymbol(rawSymbol);
        PriceSnapshot latestSnapshot = priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("No market data found for symbol: " + symbol));

        return new LatestMarketDataResponse(
                latestSnapshot.getSymbol(),
                latestSnapshot.getSnapshotDate(),
                latestSnapshot.getCurrentPrice(),
                latestSnapshot.getCurrency(),
                SOURCE
        );
    }

    @Transactional(readOnly = true)
    public List<LatestMarketDataResponse> getLatestMarketDataBatch(String symbolsCsv) {
        if (symbolsCsv == null || symbolsCsv.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "symbols query param must not be blank");
        }

        List<String> symbols = Arrays.stream(symbolsCsv.split(","))
                .map(this::normalizeSymbol)
                .distinct()
                .toList();

        List<LatestMarketDataResponse> responses = new ArrayList<>();
        for (String symbol : symbols) {
            PriceSnapshot latestSnapshot = priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc(symbol)
                    .orElseThrow(() -> new ResourceNotFoundException("No market data found for symbol: " + symbol));

            responses.add(new LatestMarketDataResponse(
                    latestSnapshot.getSymbol(),
                    latestSnapshot.getSnapshotDate(),
                    latestSnapshot.getCurrentPrice(),
                    latestSnapshot.getCurrency(),
                    SOURCE
            ));
        }

        return responses;
    }

    private void upsertSnapshot(String symbol, LocalDate snapshotDate, BigDecimal currentPrice, String currency) {
        PriceSnapshot snapshot = priceSnapshotRepository.findBySymbolAndSnapshotDate(symbol, snapshotDate)
                .orElseGet(PriceSnapshot::new);

        snapshot.setSymbol(symbol);
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setCurrentPrice(currentPrice);
        snapshot.setCurrency(currency);
        priceSnapshotRepository.save(snapshot);
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
