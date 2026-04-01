package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketDataRefreshResponse;
import org.group.jet2holiday.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataControllerTest {

    @Mock
    private MarketDataService marketDataService;

    private MarketDataController marketDataController;

    @BeforeEach
    void setUp() {
        marketDataController = new MarketDataController(marketDataService);
    }

    @Test
    void getLatestBySymbol_DelegatesToService() {
        LatestMarketDataResponse expected = new LatestMarketDataResponse(
                "AAPL",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("188.12"),
                "USD",
                "FINNHUB_QUOTE"
        );
        when(marketDataService.getLatestMarketData("AAPL")).thenReturn(expected);

        LatestMarketDataResponse result = marketDataController.getLatestBySymbol("AAPL");

        assertEquals("AAPL", result.getSymbol());
        verify(marketDataService).getLatestMarketData("AAPL");
    }

    @Test
    void getLatestBySymbols_DelegatesToService() {
        LatestMarketDataResponse aapl = new LatestMarketDataResponse(
                "AAPL",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("188.12"),
                "USD",
                "FINNHUB_QUOTE"
        );
        LatestMarketDataResponse msft = new LatestMarketDataResponse(
                "MSFT",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("422.30"),
                "USD",
                "FINNHUB_QUOTE"
        );
        when(marketDataService.getLatestMarketDataBatch("AAPL,MSFT")).thenReturn(List.of(aapl, msft));

        List<LatestMarketDataResponse> result = marketDataController.getLatestBySymbols("AAPL,MSFT");

        assertEquals(2, result.size());
        verify(marketDataService).getLatestMarketDataBatch("AAPL,MSFT");
    }

    @Test
    void refreshAllHoldings_DelegatesToService() {
        MarketDataRefreshResponse expected = new MarketDataRefreshResponse(
                true,
                LocalDate.of(2026, 4, 1),
                2,
                List.of("AAPL", "MSFT"),
                List.of(),
                "Market data refreshed successfully."
        );
        when(marketDataService.refreshAllHoldingsMarketData()).thenReturn(expected);

        MarketDataRefreshResponse result = marketDataController.refreshAllHoldings();

        assertEquals(2, result.getRefreshedCount());
        verify(marketDataService).refreshAllHoldingsMarketData();
    }
}

