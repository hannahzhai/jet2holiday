package org.group.jet2holiday.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.group.jet2holiday.client.OllamaClient;
import org.group.jet2holiday.dto.ai.PortfolioInsightResponse;
import org.group.jet2holiday.service.PortfolioInsightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaControllerTest {

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private PortfolioInsightService portfolioInsightService;

    private OllamaController ollamaController;

    @BeforeEach
    void setUp() {
        ollamaController = new OllamaController(ollamaClient, portfolioInsightService);
    }

    @Test
    void ask_delegatesToClient() {
        when(ollamaClient.askModel("How is my portfolio?")).thenReturn("Stable.");

        Map<String, String> response = ollamaController.ask(Map.of("question", "How is my portfolio?"));

        assertEquals("Stable.", response.get("answer"));
        verify(ollamaClient).askModel("How is my portfolio?");
    }

    @Test
    void generatePortfolioInsight_defaultsRangeToOneMonth() {
        PortfolioInsightResponse expected = new PortfolioInsightResponse(
                "- Insight line",
                false,
                "llama3.2:latest",
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );
        when(portfolioInsightService.generatePortfolioInsight("1M")).thenReturn(expected);

        PortfolioInsightResponse response = ollamaController.generatePortfolioInsight(null);

        assertEquals("llama3.2:latest", response.getModel());
        verify(portfolioInsightService).generatePortfolioInsight("1M");
    }
}
