package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.group.jet2holiday.client.FinnhubQuoteClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private FinnhubQuoteClient finnhubQuoteClient;

    private MarketDataService marketDataService;

    @BeforeEach
    void setUp() {
        marketDataService = new MarketDataService(
                accountRepository,
                portfolioItemRepository,
                priceSnapshotRepository,
                finnhubQuoteClient
        );
    }

    @Test
    void getLatestMarketData_UsesRealtimeQuoteAndSavesSnapshot() {
        LocalDate today = LocalDate.now();
        when(finnhubQuoteClient.fetchQuote("AAPL")).thenReturn(Optional.of(new BigDecimal("210.50")));
        when(priceSnapshotRepository.findBySymbolAndSnapshotDate("AAPL", today)).thenReturn(Optional.empty());
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LatestMarketDataResponse response = marketDataService.getLatestMarketData("AAPL");

        assertEquals("AAPL", response.getSymbol());
        assertEquals(new BigDecimal("210.50"), response.getCurrentPrice());
        assertEquals("FINNHUB_QUOTE", response.getSource());
    }

    @Test
    void getLatestMarketData_Throws502WhenClientFails() {
        when(finnhubQuoteClient.fetchQuote("MSFT")).thenThrow(new ExternalApiException("upstream error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> marketDataService.getLatestMarketData("MSFT"));
        assertEquals(BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void getLatestMarketData_Throws502WhenQuoteMissing() {
        when(finnhubQuoteClient.fetchQuote("MSFT")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> marketDataService.getLatestMarketData("MSFT"));
        assertEquals(BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void getLatestMarketDataBatch_ReturnsAllWhenAllQuotesAvailable() {
        LocalDate today = LocalDate.now();
        when(finnhubQuoteClient.fetchQuote("AAPL")).thenReturn(Optional.of(new BigDecimal("200.00")));
        when(finnhubQuoteClient.fetchQuote("MSFT")).thenReturn(Optional.of(new BigDecimal("300.00")));
        when(priceSnapshotRepository.findBySymbolAndSnapshotDate("AAPL", today)).thenReturn(Optional.empty());
        when(priceSnapshotRepository.findBySymbolAndSnapshotDate("MSFT", today)).thenReturn(Optional.empty());
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<LatestMarketDataResponse> responses = marketDataService.getLatestMarketDataBatch("AAPL,MSFT");

        assertEquals(2, responses.size());
        assertEquals(Set.of("AAPL", "MSFT"), Set.of(responses.get(0).getSymbol(), responses.get(1).getSymbol()));
    }

    @Test
    void getLatestMarketDataBatch_BlankSymbolsThrows400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> marketDataService.getLatestMarketDataBatch(" "));
        assertEquals(BAD_REQUEST, ex.getStatusCode());
    }
}

