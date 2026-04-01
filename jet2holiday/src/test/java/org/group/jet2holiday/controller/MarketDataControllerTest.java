package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketDataControllerTest {

    @Mock
    private MarketDataService marketDataService;

    private MarketDataController marketDataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        marketDataController = new MarketDataController(marketDataService);
    }

    @Test
    void getLatestBySymbol_ReturnsServiceResult() {
        LatestMarketDataResponse response = new LatestMarketDataResponse(
                "AAPL",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("222.22"),
                "USD",
                "YAHOO_FINANCE"
        );
        when(marketDataService.getLatestMarketData("AAPL")).thenReturn(response);

        LatestMarketDataResponse result = marketDataController.getLatestBySymbol("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        verify(marketDataService).getLatestMarketData("AAPL");
    }

    @Test
    void getLatestBySymbols_ReturnsArray() {
        LatestMarketDataResponse aapl = new LatestMarketDataResponse(
                "AAPL",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("222.22"),
                "USD",
                "YAHOO_FINANCE"
        );
        LatestMarketDataResponse msft = new LatestMarketDataResponse(
                "MSFT",
                LocalDate.of(2026, 4, 1),
                new BigDecimal("321.11"),
                "USD",
                "YAHOO_FINANCE"
        );
        when(marketDataService.getLatestMarketDataBatch("AAPL,MSFT")).thenReturn(List.of(aapl, msft));

        List<LatestMarketDataResponse> result = marketDataController.getLatestBySymbols("AAPL,MSFT");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("MSFT", result.get(1).getSymbol());
        verify(marketDataService).getLatestMarketDataBatch("AAPL,MSFT");
    }
}

