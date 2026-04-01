package org.group.jet2holiday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.group.jet2holiday.client.LlmAdviceClient;
import org.group.jet2holiday.client.LlmAdviceResult;
import org.group.jet2holiday.dto.advice.AdviceGenerateRequest;
import org.group.jet2holiday.dto.advice.AdviceGenerateResponse;
import org.group.jet2holiday.dto.advice.HoldingAdviceSnapshot;
import org.group.jet2holiday.entity.Account;
import org.group.jet2holiday.entity.PortfolioItem;
import org.group.jet2holiday.entity.PriceSnapshot;
import org.group.jet2holiday.exception.ExternalApiException;
import org.group.jet2holiday.repository.AccountRepository;
import org.group.jet2holiday.repository.PortfolioItemRepository;
import org.group.jet2holiday.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdviceService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final LlmAdviceClient llmAdviceClient;

    public AdviceService(AccountRepository accountRepository,
                         PortfolioItemRepository portfolioItemRepository,
                         PriceSnapshotRepository priceSnapshotRepository,
                         LlmAdviceClient llmAdviceClient) {
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.llmAdviceClient = llmAdviceClient;
    }

    @Transactional(readOnly = true)
    public AdviceGenerateResponse generateAdvice(AdviceGenerateRequest request) {
        Account account = getCurrentAccount();
        List<PortfolioItem> holdings = portfolioItemRepository.findByAccountId(account.getId());
        BigDecimal cashBalance = safeAmount(account.getCashBalance());

        List<HoldingAdviceSnapshot> snapshots = buildHoldingSnapshots(holdings);
        BigDecimal stockValue = snapshots.stream()
                .map(HoldingAdviceSnapshot::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValue = stockValue.add(cashBalance);
        decorateAllocations(snapshots, totalValue);

        LlmAdviceResult llmResult = null;
        boolean fallbackUsed = false;
        if (!snapshots.isEmpty()) {
            try {
                llmResult = llmAdviceClient.generateAdvice(buildPrompt(request, cashBalance, totalValue, snapshots));
            } catch (ExternalApiException ex) {
                fallbackUsed = true;
            }
        } else {
            fallbackUsed = true;
        }

        if (llmResult == null) {
            llmResult = buildRuleBasedAdvice(snapshots, cashBalance, totalValue);
        }

        return new AdviceGenerateResponse(
                llmResult.getSummary(),
                normalizeRiskLevel(llmResult.getRiskLevel()),
                llmResult.getSuggestions(),
                llmResult.getRiskWarnings(),
                snapshots,
                totalValue.setScale(2, RoundingMode.HALF_UP),
                cashBalance.setScale(2, RoundingMode.HALF_UP),
                llmAdviceClient.modelName(),
                fallbackUsed,
                LocalDateTime.now()
        );
    }

    private List<HoldingAdviceSnapshot> buildHoldingSnapshots(List<PortfolioItem> holdings) {
        List<HoldingAdviceSnapshot> result = new ArrayList<>();
        for (PortfolioItem item : holdings) {
            BigDecimal shares = safeAmount(item.getShares());
            BigDecimal costBasis = safeAmount(item.getCostBasis());
            BigDecimal latestPrice = priceSnapshotRepository.findTopBySymbolOrderBySnapshotDateDesc(item.getSymbol())
                    .map(PriceSnapshot::getCurrentPrice)
                    .orElse(costBasis);
            latestPrice = safeAmount(latestPrice);

            BigDecimal marketValue = shares.multiply(latestPrice);
            BigDecimal pnlPct = BigDecimal.ZERO;
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                pnlPct = latestPrice.subtract(costBasis)
                        .divide(costBasis, 6, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);
            }

            result.add(new HoldingAdviceSnapshot(
                    item.getSymbol(),
                    item.getCompanyName(),
                    shares.setScale(4, RoundingMode.HALF_UP),
                    costBasis.setScale(4, RoundingMode.HALF_UP),
                    latestPrice.setScale(4, RoundingMode.HALF_UP),
                    marketValue.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO,
                    pnlPct.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        result.sort(Comparator.comparing(HoldingAdviceSnapshot::getMarketValue).reversed());
        return result;
    }

    private void decorateAllocations(List<HoldingAdviceSnapshot> snapshots, BigDecimal totalValue) {
        if (totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        for (HoldingAdviceSnapshot snapshot : snapshots) {
            BigDecimal allocation = snapshot.getMarketValue()
                    .divide(totalValue, 6, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
            snapshot.setAllocationPct(allocation);
        }
    }

    private String buildPrompt(AdviceGenerateRequest request,
                               BigDecimal cashBalance,
                               BigDecimal totalValue,
                               List<HoldingAdviceSnapshot> snapshots) {
        StringBuilder builder = new StringBuilder();
        builder.append("Provide investment suggestions and risk warnings for this portfolio. ");
        builder.append("Output JSON only.\n");
        builder.append("cashBalance=").append(cashBalance.setScale(2, RoundingMode.HALF_UP)).append("\n");
        builder.append("totalPortfolioValue=").append(totalValue.setScale(2, RoundingMode.HALF_UP)).append("\n");
        if (request != null) {
            if (hasText(request.getInvestmentGoal())) {
                builder.append("investmentGoal=").append(request.getInvestmentGoal().trim()).append("\n");
            }
            if (hasText(request.getRiskPreference())) {
                builder.append("riskPreference=").append(request.getRiskPreference().trim()).append("\n");
            }
            if (hasText(request.getExtraContext())) {
                builder.append("extraContext=").append(request.getExtraContext().trim()).append("\n");
            }
        }

        builder.append("holdings:\n");
        for (HoldingAdviceSnapshot snapshot : snapshots) {
            builder.append("- symbol=").append(snapshot.getSymbol())
                    .append(", company=").append(snapshot.getCompanyName())
                    .append(", allocationPct=").append(snapshot.getAllocationPct())
                    .append(", pnlPct=").append(snapshot.getUnrealizedPnLPct())
                    .append(", shares=").append(snapshot.getShares())
                    .append("\n");
        }

        return builder.toString();
    }

    private LlmAdviceResult buildRuleBasedAdvice(List<HoldingAdviceSnapshot> snapshots,
                                                 BigDecimal cashBalance,
                                                 BigDecimal totalValue) {
        if (snapshots.isEmpty()) {
            return new LlmAdviceResult(
                    "No holdings found. Add assets before generating AI advice.",
                    "LOW",
                    List.of("Add 3-5 diversified holdings to start building allocation."),
                    List.of("With no holdings, the portfolio has no market exposure.")
            );
        }

        HoldingAdviceSnapshot largest = snapshots.get(0);
        BigDecimal cashRatio = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            cashRatio = cashBalance.divide(totalValue, 6, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
        }

        String riskLevel = "MEDIUM";
        List<String> suggestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (largest.getAllocationPct().compareTo(new BigDecimal("45")) > 0) {
            riskLevel = "HIGH";
            warnings.add("Single-position concentration is high (" + largest.getSymbol() + " above 45%).");
            suggestions.add("Reduce concentration by adding uncorrelated sectors or broad index ETFs.");
        }

        if (cashRatio.compareTo(new BigDecimal("5")) < 0) {
            warnings.add("Cash buffer is low (<5%), reducing flexibility in volatile markets.");
            suggestions.add("Keep a tactical cash buffer between 5% and 15%.");
        }

        long lossCount = snapshots.stream()
                .filter(snapshot -> snapshot.getUnrealizedPnLPct().compareTo(new BigDecimal("-10")) <= 0)
                .count();
        if (lossCount > 0) {
            warnings.add(lossCount + " position(s) are down more than 10% from cost basis.");
            suggestions.add("Review conviction and stop-loss/rebalance rules for underperforming holdings.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Maintain current allocation and rebalance monthly based on target weights.");
        }
        if (warnings.isEmpty()) {
            warnings.add("No major structural risk detected by the fallback rule engine.");
        }

        String summary = "Fallback analysis generated from latest holdings and price snapshots.";
        return new LlmAdviceResult(summary, riskLevel, suggestions, warnings);
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (!hasText(riskLevel)) {
            return "MEDIUM";
        }
        String value = riskLevel.trim().toUpperCase();
        if (!value.equals("LOW") && !value.equals("MEDIUM") && !value.equals("HIGH")) {
            return "MEDIUM";
        }
        return value;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Account getCurrentAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> accountRepository.save(new Account()));
    }
}

