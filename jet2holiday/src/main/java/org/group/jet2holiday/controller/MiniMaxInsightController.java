package org.group.jet2holiday.controller;

import org.group.jet2holiday.dto.ai.PortfolioInsightRequest;
import org.group.jet2holiday.dto.ai.PortfolioInsightResponse;
import org.group.jet2holiday.service.MiniMaxInsightService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class MiniMaxInsightController {

    private final MiniMaxInsightService miniMaxInsightService;

    public MiniMaxInsightController(MiniMaxInsightService miniMaxInsightService) {
        this.miniMaxInsightService = miniMaxInsightService;
    }

    @PostMapping("/minimax-insight")
    public PortfolioInsightResponse generateMiniMaxInsight(@RequestBody(required = false) PortfolioInsightRequest request) {
        String range = request == null ? "1M" : request.getRange();
        return miniMaxInsightService.generateMiniMaxInsight(range);
    }
}
