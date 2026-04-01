package org.group.jet2holiday.controller;

import java.time.LocalDateTime;
import org.group.jet2holiday.dto.ai.PortfolioInsightResponse;
import org.group.jet2holiday.service.MiniMaxInsightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniMaxInsightControllerTest {

    @Mock
    private MiniMaxInsightService miniMaxInsightService;

    private MiniMaxInsightController miniMaxInsightController;

    @BeforeEach
    void setUp() {
        miniMaxInsightController = new MiniMaxInsightController(miniMaxInsightService);
    }

    @Test
    void generateMiniMaxInsight_defaultsRangeToOneMonth() {
        PortfolioInsightResponse expected = new PortfolioInsightResponse(
                "- MiniMax insight",
                false,
                "MiniMax-Text-01",
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );
        when(miniMaxInsightService.generateMiniMaxInsight("1M")).thenReturn(expected);

        PortfolioInsightResponse response = miniMaxInsightController.generateMiniMaxInsight(null);

        assertEquals("MiniMax-Text-01", response.getModel());
        verify(miniMaxInsightService).generateMiniMaxInsight("1M");
    }
}
