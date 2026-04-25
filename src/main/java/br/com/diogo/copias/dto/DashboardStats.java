package br.com.sistemacopias.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardStats {
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private BigDecimal monthRevenue = BigDecimal.ZERO;
    private long ordersToday;
    private long ordersMonth;
    private BigDecimal averageTicketMonth = BigDecimal.ZERO;
    private Map<String, BigDecimal> paymentMonthTotals = new LinkedHashMap<>();
    private List<DashboardDayBar> last7Days;

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public BigDecimal getMonthRevenue() {
        return monthRevenue;
    }

    public void setMonthRevenue(BigDecimal monthRevenue) {
        this.monthRevenue = monthRevenue;
    }

    public long getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(long ordersToday) {
        this.ordersToday = ordersToday;
    }

    public long getOrdersMonth() {
        return ordersMonth;
    }

    public void setOrdersMonth(long ordersMonth) {
        this.ordersMonth = ordersMonth;
    }

    public BigDecimal getAverageTicketMonth() {
        return averageTicketMonth;
    }

    public void setAverageTicketMonth(BigDecimal averageTicketMonth) {
        this.averageTicketMonth = averageTicketMonth;
    }

    public Map<String, BigDecimal> getPaymentMonthTotals() {
        return paymentMonthTotals;
    }

    public void setPaymentMonthTotals(Map<String, BigDecimal> paymentMonthTotals) {
        this.paymentMonthTotals = paymentMonthTotals;
    }

    public List<DashboardDayBar> getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(List<DashboardDayBar> last7Days) {
        this.last7Days = last7Days;
    }
}
