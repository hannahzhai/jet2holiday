package org.group.jet2holiday.client;

import java.util.List;

public class LlmAdviceResult {

    private String summary;
    private String riskLevel;
    private List<String> suggestions;
    private List<String> riskWarnings;

    public LlmAdviceResult() {
    }

    public LlmAdviceResult(String summary, String riskLevel, List<String> suggestions, List<String> riskWarnings) {
        this.summary = summary;
        this.riskLevel = riskLevel;
        this.suggestions = suggestions;
        this.riskWarnings = riskWarnings;
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
}

