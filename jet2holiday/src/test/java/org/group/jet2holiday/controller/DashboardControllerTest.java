package org.group.jet2holiday.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.group.jet2holiday.dto.dashboard.AttributionResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryItemResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
import org.group.jet2holiday.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(dashboardService);
    }

    @Test
    void getSummary_DelegatesToService() {
        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                new BigDecimal("1000"),
                new BigDecimal("3000"),
                new BigDecimal("2000"),
                new BigDecimal("1800"),
                new BigDecimal("200"),
                new BigDecimal("11.11"),
                Map.of("stocks", new BigDecimal("50"), "bonds", new BigDecimal("20"), "cash", new BigDecimal("30")),
                Map.of("stocks", new BigDecimal("1500"), "bonds", new BigDecimal("600"), "cash", new BigDecimal("900")),
                List.of(new DashboardSummaryItemResponse()),
                new AttributionResponse(List.of(), List.of(), List.of())
        );
        when(dashboardService.getSummary()).thenReturn(summary);

        DashboardSummaryResponse result = dashboardController.getSummary();

        assertEquals(new BigDecimal("3000"), result.getTotalAssets());
        assertEquals(0, result.getAttribution().getTopContributors().size());
        verify(dashboardService).getSummary();
    }
}
