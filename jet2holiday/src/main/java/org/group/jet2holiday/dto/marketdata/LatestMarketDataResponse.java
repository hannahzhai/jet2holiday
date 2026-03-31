package org.group.jet2holiday.dto.marketdata;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LatestMarketDataResponse {

    private String symbol;
    private LocalDate snapshotDate;
    private BigDecimal currentPrice;
    private String currency;
    private String source;

    public LatestMarketDataResponse() {
    }

    public LatestMarketDataResponse(String symbol, LocalDate snapshotDate, BigDecimal currentPrice, String currency, String source) {
        this.symbol = symbol;
        this.snapshotDate = snapshotDate;
        this.currentPrice = currentPrice;
        this.currency = currency;
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSource() {
        return source;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
