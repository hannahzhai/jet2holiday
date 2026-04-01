package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.group.jet2holiday.client.OllamaClient;
import org.group.jet2holiday.dto.ai.PortfolioInsightResponse;
import org.group.jet2holiday.dto.dashboard.AttributionAssetTypeItem;
import org.group.jet2holiday.dto.dashboard.AttributionHoldingItem;
import org.group.jet2holiday.dto.dashboard.AttributionResponse;
import org.group.jet2holiday.dto.dashboard.DashboardSummaryResponse;
import org.group.jet2holiday.dto.dashboard.PerformanceResponse;
import org.springframework.stereotype.Service;

@Service
public class PortfolioInsightService {

    private final DashboardService dashboardService;
    private final OllamaClient ollamaClient;

    public PortfolioInsightService(DashboardService dashboardService, OllamaClient ollamaClient) {
        this.dashboardService = dashboardService;
        this.ollamaClient = ollamaClient;
    }

    public PortfolioInsightResponse generatePortfolioInsight(String range) {
        String normalizedRange = normalizeRange(range);
        DashboardSummaryResponse summary = dashboardService.getSummary();
        PerformanceResponse performance = dashboardService.getPerformance(normalizedRange);
        String prompt = buildPrompt(summary, performance, normalizedRange);

        try {
            String insight = safeText(ollamaClient.askModel(prompt));
            if (!insight.isBlank()) {
                return new PortfolioInsightResponse(
                        insight,
                        false,
                        ollamaClient.modelName(),
                        LocalDateTime.now()
                );
            }
        } catch (RuntimeException ignored) {
            // Use deterministic fallback summary when Ollama is unavailable.
        }

        return new PortfolioInsightResponse(
                buildFallbackSummary(summary, performance, normalizedRange),
                true,
                ollamaClient.modelName(),
                LocalDateTime.now()
        );
    }

    private String buildPrompt(DashboardSummaryResponse summary, PerformanceResponse performance, String range) {
        String allocationText = formatAllocation(summary.getAllocation());
        String contributors = formatHoldingList(summary.getAttribution(), true);
        String detractors = formatHoldingList(summary.getAttribution(), false);
        String byAssetType = formatByAssetType(summary.getAttribution());
        String performanceLine = formatPerformanceLine(performance, range);

        return """
                You are a senior portfolio analyst. Generate an English portfolio insight in 5-7 short bullet points.
                Requirements:
                - Cover concentration risk.
                - Mention top return contributors.
                - Mention cash/liquidity buffer.
                - Mention recent performance.
                - Give 1-2 practical suggestions.
                - Keep the tone clear and concise.

                Portfolio snapshot:
                - Total assets: %s
                - Total PnL: %s
                - Total PnL %%: %s%%
                - Cash balance: %s
                - Allocation (%%): %s
                - Top contributors: %s
                - Top detractors: %s
                - Performance by asset type: %s
                - Recent performance (%s): %s
                """.formatted(
                fmt(summary.getTotalAssets()),
                fmt(summary.getTotalProfitLoss()),
                fmt(summary.getTotalProfitLossPercent()),
                fmt(summary.getCashBalance()),
                allocationText,
                contributors,
                detractors,
                byAssetType,
                range,
                performanceLine
        );
    }

    private String buildFallbackSummary(DashboardSummaryResponse summary, PerformanceResponse performance, String range) {
        String stockAllocation = fmt(readAllocation(summary.getAllocation(), "stocks"));
        String bondAllocation = fmt(readAllocation(summary.getAllocation(), "bonds"));
        String cashAllocation = fmt(readAllocation(summary.getAllocation(), "cash"));
        String topContributors = formatHoldingList(summary.getAttribution(), true);
        String topDetractors = formatHoldingList(summary.getAttribution(), false);
        String performanceLine = formatPerformanceLine(performance, range);
        String concentrationLine = buildConcentrationLine(summary.getAllocation());

        return String.join("\n",
                "- " + concentrationLine,
                "- Allocation mix: stocks " + stockAllocation + "%, bonds " + bondAllocation + "%, cash " + cashAllocation + "%.",
                "- Main contributors this period: " + topContributors + ".",
                "- Key detractors to watch: " + topDetractors + ".",
                "- Cash buffer stands at " + fmt(summary.getCashBalance()) + ", helping near-term liquidity management.",
                "- Recent performance (" + range + "): " + performanceLine + ".",
                "- Suggested action: reduce single-name concentration and rebalance gradually toward diversified exposure."
        );
    }

    private String buildConcentrationLine(Map<String, BigDecimal> allocation) {
        if (allocation == null || allocation.isEmpty()) {
            return "Portfolio concentration risk appears moderate based on current allocation.";
        }
        Map.Entry<String, BigDecimal> dominant = allocation.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (dominant == null || dominant.getValue() == null) {
            return "Portfolio concentration risk appears moderate based on current allocation.";
        }

        String bucket = dominant.getKey().toLowerCase(Locale.ROOT);
        BigDecimal weight = dominant.getValue();
        if ("stocks".equals(bucket) && weight.compareTo(new BigDecimal("60")) >= 0) {
            return "Equity allocation is elevated at " + fmt(weight) + "%, indicating concentration risk in risk assets.";
        }
        return "Largest allocation is " + bucket + " at " + fmt(weight) + "%, which should be monitored for concentration risk.";
    }

    private String formatPerformanceLine(PerformanceResponse performance, String range) {
        List<PerformanceResponse.PerformancePoint> points = performance == null
                ? List.of()
                : performance.getPoints();
        if (points == null || points.size() < 2) {
            return "insufficient " + range + " data points";
        }

        List<PerformanceResponse.PerformancePoint> sorted = points.stream()
                .sorted(Comparator.comparing(PerformanceResponse.PerformancePoint::getDate))
                .toList();
        BigDecimal first = safe(sorted.getFirst().getTotalProfitLoss());
        BigDecimal last = safe(sorted.getLast().getTotalProfitLoss());
        BigDecimal delta = last.subtract(first);
        String direction = delta.compareTo(BigDecimal.ZERO) >= 0 ? "up" : "down";

        return direction + " " + fmt(delta.abs()) + " from " + fmt(first) + " to " + fmt(last);
    }

    private String formatAllocation(Map<String, BigDecimal> allocation) {
        if (allocation == null || allocation.isEmpty()) {
            return "N/A";
        }
        return allocation.entrySet().stream()
                .map(entry -> entry.getKey() + " " + fmt(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private String formatByAssetType(AttributionResponse attribution) {
        List<AttributionAssetTypeItem> byAssetType = attribution == null ? List.of() : attribution.getByAssetType();
        if (byAssetType == null || byAssetType.isEmpty()) {
            return "N/A";
        }
        return byAssetType.stream()
                .map(item -> item.getAssetType() + " " + fmt(item.getProfitLoss()))
                .collect(Collectors.joining(", "));
    }

    private String formatHoldingList(AttributionResponse attribution, boolean contributors) {
        List<AttributionHoldingItem> list = attribution == null
                ? List.of()
                : (contributors ? attribution.getTopContributors() : attribution.getTopDetractors());
        if (list == null || list.isEmpty()) {
            return "N/A";
        }
        return list.stream()
                .limit(3)
                .map(item -> item.getSymbol() + " (" + fmt(item.getProfitLoss()) + ")")
                .collect(Collectors.joining(", "));
    }

    private BigDecimal readAllocation(Map<String, BigDecimal> allocation, String key) {
        if (allocation == null) {
            return BigDecimal.ZERO;
        }
        return safe(allocation.get(key));
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
        if (range == null) {
            return "1M";
        }
        return switch (range.trim().toUpperCase(Locale.ROOT)) {
            case "1W", "1M", "3M", "1Y" -> range.trim().toUpperCase(Locale.ROOT);
            default -> "1M";
        };
    }
}
