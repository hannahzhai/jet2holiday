package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.group.jet2holiday.client.FinnhubQuoteClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketInstrumentItemResponse;
import org.group.jet2holiday.dto.marketdata.MarketInstrumentPageResponse;
import org.group.jet2holiday.dto.marketdata.MarketDataRefreshResponse;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.MarketInstrument;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.MarketInstrumentRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class MarketDataService {

    private static final String SOURCE = "FINNHUB_QUOTE";
    private static final String DEFAULT_CURRENCY = "USD";
    private static final int MARKET_PAGE_SIZE = 15;
    private static final Set<String> SUPPORTED_ASSET_TYPES = Set.of("STOCK", "BOND");

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final MarketInstrumentRepository marketInstrumentRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final FinnhubQuoteClient finnhubQuoteClient;

    public MarketDataService(
            AccountRepository accountRepository,
            PortfolioItemRepository portfolioItemRepository,
            MarketInstrumentRepository marketInstrumentRepository,
            PriceSnapshotRepository priceSnapshotRepository,
            FinnhubQuoteClient finnhubQuoteClient
    ) {
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.marketInstrumentRepository = marketInstrumentRepository;
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

    @Transactional
    public MarketInstrumentPageResponse getMarketInstruments(String rawAssetType, Integer rawPage) {
        String assetType = normalizeAssetType(rawAssetType);
        int page = normalizePage(rawPage);
        Pageable pageable = PageRequest.of(
                page - 1,
                MARKET_PAGE_SIZE,
                Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("id"))
        );
        Page<MarketInstrument> result = marketInstrumentRepository.findByAssetTypeAndEnabledTrue(assetType, pageable);

        List<MarketInstrumentItemResponse> items = result.getContent().stream()
                .map(item -> new MarketInstrumentItemResponse(
                        item.getSymbol(),
                        item.getCompanyName(),
                        item.getAssetType(),
                        item.getMarket(),
                        item.getCurrency()
                ))
                .toList();

        return new MarketInstrumentPageResponse(
                page,
                MARKET_PAGE_SIZE,
                (int) result.getTotalElements(),
                result.getTotalPages(),
                items
        );
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

    private String normalizeAssetType(String assetType) {
        if (assetType == null || assetType.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "assetType query param must not be blank");
        }
        String normalized = assetType.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_ASSET_TYPES.contains(normalized)) {
            throw new ResponseStatusException(BAD_REQUEST, "assetType must be STOCK or BOND");
        }
        return normalized;
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return 1;
        }
        if (page <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "page must be greater than zero");
        }
        return page;
    }

}

