package org.group.jet2holiday.dto.marketdata;

public class MarketInstrumentItemResponse {

    private String symbol;
    private String companyName;
    private String assetType;
    private String market;
    private String currency;

    public MarketInstrumentItemResponse() {
    }

    public MarketInstrumentItemResponse(
            String symbol,
            String companyName,
            String assetType,
            String market,
            String currency
    ) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.assetType = assetType;
        this.market = market;
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
