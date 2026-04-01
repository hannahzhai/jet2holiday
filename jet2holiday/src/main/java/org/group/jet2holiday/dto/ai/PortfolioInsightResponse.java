package org.group.jet2holiday.dto.ai;

import java.time.LocalDateTime;

public class PortfolioInsightResponse {

    private String insight;
    private boolean fallbackUsed;
    private String model;
    private LocalDateTime generatedAt;

    public PortfolioInsightResponse() {
    }

    public PortfolioInsightResponse(String insight, boolean fallbackUsed, String model, LocalDateTime generatedAt) {
        this.insight = insight;
        this.fallbackUsed = fallbackUsed;
        this.model = model;
        this.generatedAt = generatedAt;
    }

    public String getInsight() {
        return insight;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public String getModel() {
        return model;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
