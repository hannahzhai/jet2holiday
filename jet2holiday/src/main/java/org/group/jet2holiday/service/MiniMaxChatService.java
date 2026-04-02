package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.group.jet2holiday.client.MiniMaxClient;
import org.group.jet2holiday.dto.ai.MiniMaxChatResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse;
import org.springframework.stereotype.Service;

@Service
public class MiniMaxChatService {

    private final DashboardService dashboardService;
    private final MiniMaxClient miniMaxClient;

    public MiniMaxChatService(DashboardService dashboardService, MiniMaxClient miniMaxClient) {
        this.dashboardService = dashboardService;
        this.miniMaxClient = miniMaxClient;
    }

    public MiniMaxChatResponse askQuestion(String question, String range) {
        String normalizedRange = normalizeRange(range);
        DashboardSummaryResponse summary = dashboardService.getSummary();
        PerformanceResponse performance = dashboardService.getPerformance(normalizedRange);
        String prompt = buildPrompt(question, summary, performance, normalizedRange);

        try {
            String answer = safeText(miniMaxClient.generateInsight(prompt));
            if (!answer.isBlank()) {
                return new MiniMaxChatResponse(
                        answer,
                        false,
                        miniMaxClient.modelName(),
                        LocalDateTime.now()
                );
            }
        } catch (RuntimeException ignored) {
            // Fall through to deterministic fallback.
        }

        return new MiniMaxChatResponse(
                buildFallbackAnswer(question, summary, performance, normalizedRange),
                true,
                miniMaxClient.modelName(),
                LocalDateTime.now()
        );
    }

    private String buildPrompt(String question, DashboardSummaryResponse summary, PerformanceResponse performance, String range) {
        return """
                You are a portfolio copilot. Answer the user's question with concise and practical guidance.
                Constraints:
                - Use only portfolio context provided below.
                - Be direct, 4-7 short bullet points.
                - Mention risks and one concrete next step.

                User question:
                %s

                Portfolio context:
                - Total assets: %s
                - Total PnL: %s
                - Total PnL %%: %s%%
                - Cash balance: %s
                - Allocation (%%): %s
                - Recent performance (%s): %s
                """.formatted(
                question,
                fmt(summary.getTotalAssets()),
                fmt(summary.getTotalProfitLoss()),
                fmt(summary.getTotalProfitLossPercent()),
                fmt(summary.getCashBalance()),
                formatAllocation(summary.getAllocation()),
                range,
                formatPerformanceLine(performance, range)
        );
    }

    private String buildFallbackAnswer(String question, DashboardSummaryResponse summary, PerformanceResponse performance, String range) {
        return String.join("\n",
                "I could not reach MiniMax just now, but here is a context-aware answer:",
                "- Your question: \"" + question + "\"",
                "- Current allocation is " + formatAllocation(summary.getAllocation()) + ".",
                "- Cash buffer is " + fmt(summary.getCashBalance()) + ", which supports near-term flexibility.",
                "- Performance over " + range + " is " + formatPerformanceLine(performance, range) + ".",
                "- Consider reducing concentration if one bucket dominates and rebalance in small steps."
        );
    }

    private String formatPerformanceLine(PerformanceResponse performance, String range) {
        List<PerformanceResponse.PerformancePoint> points = performance == null ? List.of() : performance.getPoints();
        if (points == null || points.size() < 2) {
            return "insufficient " + range + " data";
        }

        List<PerformanceResponse.PerformancePoint> sorted = points.stream()
                .sorted(Comparator.comparing(PerformanceResponse.PerformancePoint::getDate))
                .toList();
        BigDecimal first = safe(sorted.getFirst().getTotalProfitLoss());
        BigDecimal last = safe(sorted.getLast().getTotalProfitLoss());
        BigDecimal delta = last.subtract(first);
        String direction = delta.compareTo(BigDecimal.ZERO) >= 0 ? "up" : "down";
        return direction + " " + fmt(delta.abs()) + " (" + fmt(first) + " -> " + fmt(last) + ")";
    }

    private String formatAllocation(Map<String, BigDecimal> allocation) {
        if (allocation == null || allocation.isEmpty()) {
            return "N/A";
        }
        return allocation.entrySet().stream()
                .map(e -> e.getKey() + " " + fmt(e.getValue()) + "%")
                .collect(Collectors.joining(", "));
    }

    private String fmt(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String normalizeRange(String range) {
        if (range == null) return "1M";
        return switch (range.trim().toUpperCase(Locale.ROOT)) {
            case "1W", "1M", "3M", "1Y" -> range.trim().toUpperCase(Locale.ROOT);
            default -> "1M";
        };
    }
}
