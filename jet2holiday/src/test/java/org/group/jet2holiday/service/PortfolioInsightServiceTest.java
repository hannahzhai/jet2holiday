package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.client.OllamaClient;
import org.group.jet2holiday.dto.dashboard.AttributionAssetTypeItem;
import org.group.jet2holiday.dto.dashboard.AttributionHoldingItem;
import org.group.jet2holiday.dto.dashboard.AttributionResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryItemResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioInsightServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private OllamaClient ollamaClient;

    private PortfolioInsightService portfolioInsightService;

    @BeforeEach
    void setUp() {
        portfolioInsightService = new PortfolioInsightService(dashboardService, ollamaClient);
        when(ollamaClient.modelName()).thenReturn("llama3.2:latest");
        when(dashboardService.getSummary()).thenReturn(buildSummary());
        when(dashboardService.getPerformance("1M")).thenReturn(buildPerformance());
    }

    @Test
    void generatePortfolioInsight_usesOllamaWhenAvailable() {
        when(ollamaClient.askModel(anyString())).thenReturn("- Stocks are concentrated in growth names.");

        var response = portfolioInsightService.generatePortfolioInsight("1M");

        assertFalse(response.isFallbackUsed());
        assertTrue(response.getInsight().contains("Stocks are concentrated"));
    }

    @Test
    void generatePortfolioInsight_fallsBackWhenOllamaFails() {
        when(ollamaClient.askModel(anyString())).thenThrow(new RuntimeException("Ollama down"));

        var response = portfolioInsightService.generatePortfolioInsight("1M");

        assertTrue(response.isFallbackUsed());
        assertTrue(response.getInsight().contains("Allocation mix"));
        assertTrue(response.getInsight().contains("Recent performance"));
    }

    private DashboardSummaryResponse buildSummary() {
        AttributionHoldingItem contributor = new AttributionHoldingItem(
                "AAPL", "Apple", "STOCK",
                new BigDecimal("1500"), new BigDecimal("320"), new BigDecimal("18"), new BigDecimal("40")
        );
        AttributionHoldingItem detractor = new AttributionHoldingItem(
                "TSLA", "Tesla", "STOCK",
                new BigDecimal("900"), new BigDecimal("-60"), new BigDecimal("-6"), new BigDecimal("-8")
        );
        AttributionResponse attribution = new AttributionResponse(
                List.of(contributor),
                List.of(detractor),
                List.of(new AttributionAssetTypeItem("stocks", new BigDecimal("260"), new BigDecimal("90")))
        );

        return new DashboardSummaryResponse(
                new BigDecimal("1200"),
                new BigDecimal("6500"),
                new BigDecimal("5300"),
                new BigDecimal("5000"),
                new BigDecimal("300"),
                new BigDecimal("6.0"),
                Map.of("stocks", new BigDecimal("72"), "bonds", new BigDecimal("10"), "cash", new BigDecimal("18")),
                Map.of("stocks", new BigDecimal("4680"), "bonds", new BigDecimal("620"), "cash", new BigDecimal("1200")),
                List.of(new DashboardSummaryItemResponse()),
                attribution
        );
    }

    private PerformanceResponse buildPerformance() {
        return new PerformanceResponse("1M", List.of(
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 1), new BigDecimal("100")),
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 31), new BigDecimal("180"))
        ));
    }
}
