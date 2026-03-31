package org.group.jet2holiday.dto.marketdata;

import java.time.LocalDate;
import java.util.List;

public class MarketDataRefreshResponse {

    private boolean success;
    private LocalDate snapshotDate;
    private int refreshedCount;
    private List<String> symbols;
    private List<String> failedSymbols;
    private String message;

    public MarketDataRefreshResponse() {
    }

    public MarketDataRefreshResponse(
            boolean success,
            LocalDate snapshotDate,
            int refreshedCount,
            List<String> symbols,
            List<String> failedSymbols,
            String message
    ) {
        this.success = success;
        this.snapshotDate = snapshotDate;
        this.refreshedCount = refreshedCount;
        this.symbols = symbols;
        this.failedSymbols = failedSymbols;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public int getRefreshedCount() {
        return refreshedCount;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public List<String> getFailedSymbols() {
        return failedSymbols;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public void setRefreshedCount(int refreshedCount) {
        this.refreshedCount = refreshedCount;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    public void setFailedSymbols(List<String> failedSymbols) {
        this.failedSymbols = failedSymbols;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}