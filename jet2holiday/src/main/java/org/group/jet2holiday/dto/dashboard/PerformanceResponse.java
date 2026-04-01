package org.group.jet2holiday.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PerformanceResponse {
    private String range;
    private List<PerformancePoint> points;

    public PerformanceResponse() {
    }

    public PerformanceResponse(String range, List<PerformancePoint> points) {
        this.range = range;
        this.points = points;
    }

    public String getRange() {
        return range;
    }

    public List<PerformancePoint> getPoints() {
        return points;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setPoints(List<PerformancePoint> points) {
        this.points = points;
    }

    public static class PerformancePoint {
        private LocalDate date;
        private BigDecimal totalProfitLoss;

        public PerformancePoint() {
        }

        public PerformancePoint(LocalDate date, BigDecimal totalProfitLoss) {
            this.date = date;
            this.totalProfitLoss = totalProfitLoss;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getTotalProfitLoss() {
            return totalProfitLoss;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public void setTotalProfitLoss(BigDecimal totalProfitLoss) {
            this.totalProfitLoss = totalProfitLoss;
        }
    }
}