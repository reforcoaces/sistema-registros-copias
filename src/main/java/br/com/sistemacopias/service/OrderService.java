package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.DashboardDayBar;
import br.com.sistemacopias.dto.DashboardStats;
import br.com.sistemacopias.dto.OrderForm;
import br.com.sistemacopias.dto.OrderEditForm;
import br.com.sistemacopias.dto.OrderItemForm;
import br.com.sistemacopias.dto.RevenueSummary;
import br.com.sistemacopias.model.OrderItem;
import br.com.sistemacopias.model.OrderRecord;
import br.com.sistemacopias.model.PaymentMethod;
import br.com.sistemacopias.model.ProductType;
import br.com.sistemacopias.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter EDIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public BigDecimal calculateTotal(OrderForm form) {
        if (form.getItems() == null || form.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return form.getItems().stream()
                .map(this::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void finalizeOrder(OrderForm form) {
        List<OrderItem> items = form.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());
        BigDecimal total = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderRecord orderRecord = OrderRecord.createNew(items, total, form.getPaymentMethod());
        orderRepository.save(orderRecord);
    }

    public List<OrderRecord> listAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderRecord> listOrdersFiltered(LocalDate fromInclusive, LocalDate toInclusive, PaymentMethod payment) {
        return listAllOrders().stream()
                .filter(o -> fromInclusive == null || !o.getCreatedAt().toLocalDate().isBefore(fromInclusive))
                .filter(o -> toInclusive == null || !o.getCreatedAt().toLocalDate().isAfter(toInclusive))
                .filter(o -> payment == null || o.getPaymentMethod() == payment)
                .collect(Collectors.toList());
    }

    public BigDecimal sumTotal(List<OrderRecord> orders) {
        return orders.stream()
                .map(OrderRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRevenueForDate(LocalDate date) {
        return listAllOrders().stream()
                .filter(o -> o.getCreatedAt().toLocalDate().equals(date))
                .map(OrderRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DashboardStats buildDashboardStats() {
        LocalDate today = LocalDate.now(APP_ZONE);
        YearMonth month = YearMonth.from(today);
        List<OrderRecord> all = listAllOrders();

        DashboardStats stats = new DashboardStats();
        BigDecimal todayRev = all.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().equals(today))
                .map(OrderRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRevenue(todayRev);
        stats.setOrdersToday(all.stream().filter(o -> o.getCreatedAt().toLocalDate().equals(today)).count());

        List<OrderRecord> monthOrders = all.stream()
                .filter(o -> YearMonth.from(o.getCreatedAt()).equals(month))
                .collect(Collectors.toList());
        BigDecimal monthRev = monthOrders.stream().map(OrderRecord::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthRevenue(monthRev);
        stats.setOrdersMonth(monthOrders.size());
        if (!monthOrders.isEmpty()) {
            stats.setAverageTicketMonth(monthRev.divide(BigDecimal.valueOf(monthOrders.size()), 2, RoundingMode.HALF_UP));
        }

        Map<String, BigDecimal> pay = new LinkedHashMap<>();
        for (PaymentMethod pm : PaymentMethod.values()) {
            BigDecimal sum = monthOrders.stream()
                    .filter(o -> o.getPaymentMethod() == pm)
                    .map(OrderRecord::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(BigDecimal.ZERO) > 0) {
                pay.put(pm.getDescricao(), sum);
            }
        }
        stats.setPaymentMonthTotals(pay);

        List<DashboardDayBar> bars = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        DateTimeFormatter shortFmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal dayTotal = getRevenueForDate(d);
            amounts.add(dayTotal);
        }
        BigDecimal max = amounts.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            max = BigDecimal.ONE;
        }
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal dayTotal = amounts.get(6 - i);
            int pct = max.compareTo(BigDecimal.ZERO) > 0
                    ? dayTotal.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue()
                    : 0;
            bars.add(new DashboardDayBar(d.format(shortFmt), dayTotal, Math.max(pct, 2)));
        }
        stats.setLast7Days(bars);
        return stats;
    }

    /** Faturamento por produto no mes corrente (descricao -> total). */
    public Map<String, BigDecimal> buildProductMonthTotals() {
        YearMonth month = YearMonth.now(APP_ZONE);
        Map<ProductType, BigDecimal> byType = new EnumMap<>(ProductType.class);
        for (ProductType p : ProductType.values()) {
            byType.put(p, BigDecimal.ZERO);
        }
        for (OrderRecord o : listAllOrders()) {
            if (!YearMonth.from(o.getCreatedAt()).equals(month)) {
                continue;
            }
            for (OrderItem it : o.getItems()) {
                byType.merge(it.getProductType(), it.getTotal(), BigDecimal::add);
            }
        }
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        byType.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(Map.Entry<ProductType, BigDecimal>::getValue).reversed())
                .forEach(e -> out.put(e.getKey().getDescricao(), e.getValue()));
        return out;
    }

    public OrderEditForm getOrderForEdit(String id) {
        OrderRecord order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        OrderEditForm form = new OrderEditForm();
        form.setId(order.getId());
        form.setCreatedAt(order.getCreatedAt().format(EDIT_DATE_FORMAT));
        form.setPaymentMethod(order.getPaymentMethod());
        List<OrderItemForm> items = order.getItems().stream().map(item -> {
            OrderItemForm f = new OrderItemForm();
            f.setProductType(item.getProductType());
            f.setQuantity(item.getQuantity());
            return f;
        }).collect(Collectors.toList());
        form.setItems(items);
        return form;
    }

    public void updateOrder(OrderEditForm form) {
        OrderRecord existing = orderRepository.findById(form.getId()).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        List<OrderItem> items = form.getItems().stream().map(this::toOrderItem).collect(Collectors.toList());
        BigDecimal total = items.stream().map(OrderItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        existing.setCreatedAt(LocalDateTime.parse(form.getCreatedAt(), EDIT_DATE_FORMAT));
        existing.setPaymentMethod(form.getPaymentMethod());
        existing.setItems(items);
        existing.setTotalAmount(total);
        orderRepository.update(existing);
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

    public BigDecimal getTodayRevenue() {
        return getRevenueForDate(LocalDate.now(APP_ZONE));
    }

    public BigDecimal getCurrentMonthRevenue() {
        YearMonth currentMonth = YearMonth.now(APP_ZONE);
        return listAllOrders().stream()
                .filter(order -> YearMonth.from(order.getCreatedAt()).equals(currentMonth))
                .map(OrderRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<RevenueSummary> getDailyRevenueReport() {
        return buildDailyRevenueReport(listAllOrders());
    }

    public List<RevenueSummary> getMonthlyRevenueReport() {
        return buildMonthlyRevenueReport(listAllOrders());
    }

    public List<RevenueSummary> buildDailyRevenueReport(List<OrderRecord> orders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<LocalDate, BigDecimal> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.mapping(OrderRecord::getTotalAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, BigDecimal>comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new RevenueSummary(entry.getKey().format(formatter), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<RevenueSummary> buildMonthlyRevenueReport(List<OrderRecord> orders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        Map<YearMonth, BigDecimal> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> YearMonth.from(order.getCreatedAt()),
                        Collectors.mapping(OrderRecord::getTotalAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, BigDecimal>comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new RevenueSummary(entry.getKey().format(formatter), entry.getValue()))
                .collect(Collectors.toList());
    }

    private OrderItem toOrderItem(OrderItemForm itemForm) {
        ProductType productType = itemForm.getProductType();
        BigDecimal unitPrice = productType.getUnitPrice();
        BigDecimal lineTotal = calculateLineTotal(itemForm);
        return new OrderItem(productType, itemForm.getQuantity(), unitPrice, lineTotal);
    }

    private BigDecimal calculateLineTotal(OrderItemForm itemForm) {
        if (itemForm.getProductType() == null || itemForm.getQuantity() < 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal unitPrice = itemForm.getProductType().getUnitPrice();
        return unitPrice.multiply(BigDecimal.valueOf(itemForm.getQuantity()));
    }
}
