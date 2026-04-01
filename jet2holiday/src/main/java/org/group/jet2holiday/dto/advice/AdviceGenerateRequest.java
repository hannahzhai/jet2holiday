package org.group.jet2holiday.dto.advice;

public class AdviceGenerateRequest {

    private String investmentGoal;
    private String riskPreference;
    private String extraContext;

    public AdviceGenerateRequest() {
    }

    public String getInvestmentGoal() {
        return investmentGoal;
    }

    public String getRiskPreference() {
        return riskPreference;
    }

    public String getExtraContext() {
        return extraContext;
    }

    public void setInvestmentGoal(String investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public void setRiskPreference(String riskPreference) {
        this.riskPreference = riskPreference;
    }

    public void setExtraContext(String extraContext) {
        this.extraContext = extraContext;
    }
}

