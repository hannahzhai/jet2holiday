package org.group.jet2holiday.dto.marketdata;

import java.util.List;

public class MarketInstrumentPageResponse {

    private int page;
    private int size;
    private int total;
    private int totalPages;
    private List<MarketInstrumentItemResponse> items;

    public MarketInstrumentPageResponse() {
    }

    public MarketInstrumentPageResponse(
            int page,
            int size,
            int total,
            int totalPages,
            List<MarketInstrumentItemResponse> items
    ) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<MarketInstrumentItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MarketInstrumentItemResponse> items) {
        this.items = items;
    }
}
