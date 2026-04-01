package org.group.jet2holiday.controller;

import org.group.jet2holiday.client.OllamaClient;
import org.group.jet2holiday.dto.ai.PortfolioInsightRequest;
import org.group.jet2holiday.dto.ai.PortfolioInsightResponse;
import org.group.jet2holiday.service.PortfolioInsightService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class OllamaController {

    private final OllamaClient client;
    private final PortfolioInsightService portfolioInsightService;

    public OllamaController(OllamaClient client, PortfolioInsightService portfolioInsightService) {
        this.client = client;
        this.portfolioInsightService = portfolioInsightService;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = client.askModel(question);
        return Map.of("answer", answer);
    }

    @PostMapping("/portfolio-insight")
    public PortfolioInsightResponse generatePortfolioInsight(@RequestBody(required = false) PortfolioInsightRequest request) {
        String range = request == null ? "1M" : request.getRange();
        return portfolioInsightService.generatePortfolioInsight(range);
    }
}
