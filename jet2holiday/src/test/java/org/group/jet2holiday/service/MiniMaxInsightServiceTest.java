package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.client.MiniMaxClient;
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
class MiniMaxInsightServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private MiniMaxClient miniMaxClient;

    private MiniMaxInsightService miniMaxInsightService;

    @BeforeEach
    void setUp() {
        miniMaxInsightService = new MiniMaxInsightService(dashboardService, miniMaxClient);
        when(miniMaxClient.modelName()).thenReturn("MiniMax-Text-01");
        when(dashboardService.getSummary()).thenReturn(buildSummary());
        when(dashboardService.getPerformance("1M")).thenReturn(buildPerformance());
    }

    @Test
    void generateMiniMaxInsight_usesMiniMaxWhenAvailable() {
        when(miniMaxClient.generateInsight(anyString())).thenReturn("- Portfolio risk is concentrated in equities.");

        var response = miniMaxInsightService.generateMiniMaxInsight("1M");

        assertFalse(response.isFallbackUsed());
        assertTrue(response.getInsight().contains("concentrated"));
    }

    @Test
    void generateMiniMaxInsight_fallsBackWhenMiniMaxFails() {
        when(miniMaxClient.generateInsight(anyString())).thenThrow(new RuntimeException("timeout"));

        var response = miniMaxInsightService.generateMiniMaxInsight("1M");

        assertTrue(response.isFallbackUsed());
        assertTrue(response.getInsight().contains("Allocation mix"));
        assertTrue(response.getInsight().contains("Recent performance"));
    }

    private DashboardSummaryResponse buildSummary() {
        AttributionHoldingItem contributor = new AttributionHoldingItem(
                "AAPL", "Apple", "STOCK",
                new BigDecimal("1600"), new BigDecimal("300"), new BigDecimal("14"), new BigDecimal("35")
        );
        AttributionHoldingItem detractor = new AttributionHoldingItem(
                "TSLA", "Tesla", "STOCK",
                new BigDecimal("1000"), new BigDecimal("-80"), new BigDecimal("-7"), new BigDecimal("-9")
        );
        AttributionResponse attribution = new AttributionResponse(
                List.of(contributor),
                List.of(detractor),
                List.of(new AttributionAssetTypeItem("stocks", new BigDecimal("220"), new BigDecimal("88")))
        );

        return new DashboardSummaryResponse(
                new BigDecimal("1500"),
                new BigDecimal("7000"),
                new BigDecimal("5500"),
                new BigDecimal("5200"),
                new BigDecimal("300"),
                new BigDecimal("5.7"),
                Map.of("stocks", new BigDecimal("70"), "bonds", new BigDecimal("12"), "cash", new BigDecimal("18")),
                Map.of("stocks", new BigDecimal("4900"), "bonds", new BigDecimal("600"), "cash", new BigDecimal("1500")),
                List.of(new DashboardSummaryItemResponse()),
                attribution
        );
    }

    private PerformanceResponse buildPerformance() {
        return new PerformanceResponse("1M", List.of(
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 1), new BigDecimal("110")),
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 31), new BigDecimal("170"))
        ));
    }
}
