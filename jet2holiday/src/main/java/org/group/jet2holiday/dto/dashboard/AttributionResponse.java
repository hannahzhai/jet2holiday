package org.group.jet2holiday.dto.dashboard;

import java.util.List;

public class AttributionResponse {

    private List<AttributionHoldingItem> topContributors;
    private List<AttributionHoldingItem> topDetractors;
    private List<AttributionAssetTypeItem> byAssetType;

    public AttributionResponse() {
    }

    public AttributionResponse(List<AttributionHoldingItem> topContributors,
                               List<AttributionHoldingItem> topDetractors,
                               List<AttributionAssetTypeItem> byAssetType) {
        this.topContributors = topContributors;
        this.topDetractors = topDetractors;
        this.byAssetType = byAssetType;
    }

    public List<AttributionHoldingItem> getTopContributors() {
        return topContributors;
    }

    public List<AttributionHoldingItem> getTopDetractors() {
        return topDetractors;
    }

    public List<AttributionAssetTypeItem> getByAssetType() {
        return byAssetType;
    }

    public void setTopContributors(List<AttributionHoldingItem> topContributors) {
        this.topContributors = topContributors;
    }

    public void setTopDetractors(List<AttributionHoldingItem> topDetractors) {
        this.topDetractors = topDetractors;
    }

    public void setByAssetType(List<AttributionAssetTypeItem> byAssetType) {
        this.byAssetType = byAssetType;
    }
}

