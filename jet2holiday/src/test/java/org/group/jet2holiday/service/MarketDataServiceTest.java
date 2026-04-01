package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.group.jet2holiday.client.YahooFinanceClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ResourceNotFoundException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketDataServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private YahooFinanceClient yahooFinanceClient;

    private MarketDataService marketDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        marketDataService = new MarketDataService(
                accountRepository,
                portfolioItemRepository,
                priceSnapshotRepository,
                yahooFinanceClient
        );
    }

    @Test
    void getLatestMarketData_YahooFirst() {
        LocalDate today = LocalDate.now();
        when(yahooFinanceClient.fetchLatestPrices(Set.of("AAPL")))
                .thenReturn(Map.of("AAPL", new BigDecimal("210.50")));
        when(priceSnapshotRepository.findBySymbolAndSnapshotDate("AAPL", today))
                .thenReturn(Optional.empty());
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        LatestMarketDataResponse response = marketDataService.getLatestMarketData("aapl");

        assertEquals("AAPL", response.getSymbol());
        assertEquals(new BigDecimal("210.50"), response.getCurrentPrice());
        verify(priceSnapshotRepository, never()).findTopBySymbolOrderBySnapshotDateDesc("AAPL");
    }

    @Test
    void getLatestMarketData_FallbackToDb() {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setSymbol("MSFT");
        snapshot.setSnapshotDate(LocalDate.of(2026, 3, 31));
        snapshot.setCurrentPrice(new BigDecimal("310.00"));
        snapshot.setCurrency("USD");

        when(yahooFinanceClient.fetchLatestPrices(Set.of("MSFT"))).thenReturn(Map.of());
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("MSFT"))
                .thenReturn(Optional.of(snapshot));

        LatestMarketDataResponse response = marketDataService.getLatestMarketData("MSFT");
        assertEquals("MSFT", response.getSymbol());
        assertEquals(new BigDecimal("310.00"), response.getCurrentPrice());
    }

    @Test
    void getLatestMarketData_NotFoundWhenYahooAndDbBothMissing() {
        when(yahooFinanceClient.fetchLatestPrices(Set.of("SAM"))).thenReturn(Map.of());
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("SAM"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> marketDataService.getLatestMarketData("SAM"));
    }

    @Test
    void getLatestMarketDataBatch_ReturnsPartialResults() {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setSymbol("MSFT");
        snapshot.setSnapshotDate(LocalDate.of(2026, 3, 31));
        snapshot.setCurrentPrice(new BigDecimal("300.00"));
        snapshot.setCurrency("USD");

        when(yahooFinanceClient.fetchLatestPrices(Set.of("AAPL", "MSFT", "MISSING")))
                .thenReturn(Map.of("AAPL", new BigDecimal("200.00")));
        when(priceSnapshotRepository.findBySymbolAndSnapshotDate(any(), any())).thenReturn(Optional.empty());
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(i -> i.getArgument(0));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("MSFT"))
                .thenReturn(Optional.of(snapshot));
        when(priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc("MISSING"))
                .thenReturn(Optional.empty());

        List<LatestMarketDataResponse> responses = marketDataService.getLatestMarketDataBatch("AAPL,MSFT,MISSING");

        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> "AAPL".equals(r.getSymbol())));
        assertTrue(responses.stream().anyMatch(r -> "MSFT".equals(r.getSymbol())));
    }

    @Test
    void getLatestMarketDataBatch_BlankSymbolsIsBadRequest() {
        assertThrows(ResponseStatusException.class, () -> marketDataService.getLatestMarketDataBatch(" "));
    }
}

