package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.group.jet2holiday.client.FinnhubQuoteClient;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketInstrumentPageResponse;
import org.group.jet2holiday.entity.MarketInstrument;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.MarketInstrumentRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private MarketInstrumentRepository marketInstrumentRepository;

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
                marketInstrumentRepository,
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

    @Test
    void getMarketInstruments_FiltersAndPaginatesByAssetType() {
        MarketInstrument row1 = createInstrument(1L, "AAPL", "Apple Inc.", "STOCK", "US", "USD", 1);
        MarketInstrument row2 = createInstrument(2L, "MSFT", "Microsoft Corporation", "STOCK", "US", "USD", 2);
        when(marketInstrumentRepository.findByAssetTypeAndEnabledTrue(any(String.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(row1, row2),
                        PageRequest.of(0, 15),
                        20
                ));

        MarketInstrumentPageResponse response = marketDataService.getMarketInstruments("STOCK", 1);

        assertEquals(1, response.getPage());
        assertEquals(15, response.getSize());
        assertEquals(20, response.getTotal());
        assertEquals(2, response.getTotalPages());
        assertEquals(2, response.getItems().size());
        assertEquals("STOCK", response.getItems().get(0).getAssetType());
        assertEquals("US", response.getItems().get(0).getMarket());
    }

    @Test
    void getMarketInstruments_SecondPageForBonds_ReturnsRemainingRows() {
        MarketInstrument row = createInstrument(5L, "2821.HK", "iShares Core Hong Kong Bond ETF", "BOND", "HK", "HKD", 35);
        when(marketInstrumentRepository.findByAssetTypeAndEnabledTrue(any(String.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(row),
                        PageRequest.of(1, 15),
                        16
                ));

        MarketInstrumentPageResponse response = marketDataService.getMarketInstruments("BOND", 2);

        assertEquals(2, response.getPage());
        assertEquals(16, response.getTotal());
        assertEquals(2, response.getTotalPages());
        assertEquals(1, response.getItems().size());
        assertEquals("BOND", response.getItems().get(0).getAssetType());
    }

    @Test
    void getMarketInstruments_InvalidAssetTypeThrows400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> marketDataService.getMarketInstruments("CRYPTO", 1));
        assertEquals(BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getMarketInstruments_InvalidPageThrows400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> marketDataService.getMarketInstruments("STOCK", 0));
        assertEquals(BAD_REQUEST, ex.getStatusCode());
    }

    private MarketInstrument createInstrument(
            Long id,
            String symbol,
            String companyName,
            String assetType,
            String market,
            String currency,
            Integer sortOrder
    ) {
        MarketInstrument instrument = new MarketInstrument();
        instrument.setId(id);
        instrument.setSymbol(symbol);
        instrument.setCompanyName(companyName);
        instrument.setAssetType(assetType);
        instrument.setMarket(market);
        instrument.setCurrency(currency);
        instrument.setSortOrder(sortOrder);
        instrument.setEnabled(true);
        return instrument;
    }
}

