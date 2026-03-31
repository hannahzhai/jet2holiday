package org.group.jet2holiday.controller;

import java.util.List;
import org.group.jet2holiday.dto.marketdata.LatestMarketDataResponse;
import org.group.jet2holiday.dto.marketdata.MarketDataRefreshResponse;
import org.group.jet2holiday.service.MarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @PostMapping("/refresh")
    public MarketDataRefreshResponse refreshAllHoldings() {
        return marketDataService.refreshAllHoldingsMarketData();
    }

    @GetMapping("/latest/{symbol}")
    public LatestMarketDataResponse getLatestBySymbol(@PathVariable("symbol") String symbol) {
        return marketDataService.getLatestMarketData(symbol);
    }

    @GetMapping("/latest")
    public List<LatestMarketDataResponse> getLatestBySymbols(@RequestParam("symbols") String symbols) {
        return marketDataService.getLatestMarketDataBatch(symbols);
    }
}
