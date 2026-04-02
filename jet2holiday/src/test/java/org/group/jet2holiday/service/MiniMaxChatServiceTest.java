package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.client.MiniMaxClient;
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
class MiniMaxChatServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private MiniMaxClient miniMaxClient;

    private MiniMaxChatService miniMaxChatService;

    @BeforeEach
    void setUp() {
        miniMaxChatService = new MiniMaxChatService(dashboardService, miniMaxClient);
        when(miniMaxClient.modelName()).thenReturn("MiniMax-Text-01");
        when(dashboardService.getSummary()).thenReturn(buildSummary());
        when(dashboardService.getPerformance("1M")).thenReturn(buildPerformance());
    }

    @Test
    void askQuestion_usesMiniMaxWhenAvailable() {
        when(miniMaxClient.generateInsight(anyString())).thenReturn("- Keep equity concentration under review.");

        var response = miniMaxChatService.askQuestion("Where is my risk?", "1M");

        assertFalse(response.isFallbackUsed());
        assertTrue(response.getAnswer().contains("equity"));
    }

    @Test
    void askQuestion_fallsBackWhenMiniMaxFails() {
        when(miniMaxClient.generateInsight(anyString())).thenThrow(new RuntimeException("timeout"));

        var response = miniMaxChatService.askQuestion("How is my portfolio?", "1M");

        assertTrue(response.isFallbackUsed());
        assertTrue(response.getAnswer().contains("Current allocation"));
        assertTrue(response.getAnswer().contains("Performance over 1M"));
    }

    private DashboardSummaryResponse buildSummary() {
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
                new AttributionResponse(List.of(), List.of(), List.of())
        );
    }

    private PerformanceResponse buildPerformance() {
        return new PerformanceResponse("1M", List.of(
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 1), new BigDecimal("100")),
                new PerformanceResponse.PerformancePoint(LocalDate.of(2026, 3, 31), new BigDecimal("180"))
        ));
    }
}
