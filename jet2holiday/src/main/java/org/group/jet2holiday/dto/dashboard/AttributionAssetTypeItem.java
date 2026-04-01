package org.group.jet2holiday.dto.dashboard;

import java.math.BigDecimal;

public class AttributionAssetTypeItem {

    private String assetType;
    private BigDecimal profitLoss;
    private BigDecimal contributionPercent;

    public AttributionAssetTypeItem() {
    }

    public AttributionAssetTypeItem(String assetType, BigDecimal profitLoss, BigDecimal contributionPercent) {
        this.assetType = assetType;
        this.profitLoss = profitLoss;
        this.contributionPercent = contributionPercent;
    }

    public String getAssetType() {
        return assetType;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public BigDecimal getContributionPercent() {
        return contributionPercent;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public void setContributionPercent(BigDecimal contributionPercent) {
        this.contributionPercent = contributionPercent;
    }
}

