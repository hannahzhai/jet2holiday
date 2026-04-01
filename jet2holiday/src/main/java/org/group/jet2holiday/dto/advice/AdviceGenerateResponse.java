package org.group.jet2holiday.dto.advice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdviceGenerateResponse {

    private String summary;
    private String riskLevel;
    private List<String> suggestions;
    private List<String> riskWarnings;
    private List<HoldingAdviceSnapshot> holdings;
    private BigDecimal totalPortfolioValue;
    private BigDecimal cashBalance;
    private String model;
    private boolean fallbackUsed;
    private LocalDateTime generatedAt;

    public AdviceGenerateResponse() {
    }

    public AdviceGenerateResponse(String summary, String riskLevel, List<String> suggestions, List<String> riskWarnings,
                                  List<HoldingAdviceSnapshot> holdings, BigDecimal totalPortfolioValue,
                                  BigDecimal cashBalance, String model, boolean fallbackUsed,
                                  LocalDateTime generatedAt) {
        this.summary = summary;
        this.riskLevel = riskLevel;
        this.suggestions = suggestions;
        this.riskWarnings = riskWarnings;
        this.holdings = holdings;
        this.totalPortfolioValue = totalPortfolioValue;
        this.cashBalance = cashBalance;
        this.model = model;
        this.fallbackUsed = fallbackUsed;
        this.generatedAt = generatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public List<String> getRiskWarnings() {
        return riskWarnings;
    }

    public List<HoldingAdviceSnapshot> getHoldings() {
        return holdings;
    }

    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public String getModel() {
        return model;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public void setRiskWarnings(List<String> riskWarnings) {
        this.riskWarnings = riskWarnings;
    }

    public void setHoldings(List<HoldingAdviceSnapshot> holdings) {
        this.holdings = holdings;
    }

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}

