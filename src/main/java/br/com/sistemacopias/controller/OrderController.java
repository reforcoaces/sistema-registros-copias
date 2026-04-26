package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.ImportResult;
import br.com.sistemacopias.dto.OrderEditForm;
import br.com.sistemacopias.dto.OrderForm;
import br.com.sistemacopias.dto.OrderItemForm;
import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.model.PaymentMethod;
import br.com.sistemacopias.model.OrderRecord;
import br.com.sistemacopias.model.ProductType;
import br.com.sistemacopias.model.UserRole;
import br.com.sistemacopias.service.OrderPdfExportService;
import br.com.sistemacopias.service.OrderService;
import br.com.sistemacopias.service.RetroactiveImportService;
import br.com.sistemacopias.support.ChartBarHelper;
import br.com.sistemacopias.support.ChartPieHelper;
import br.com.sistemacopias.support.DashboardVistaUtil;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final RetroactiveImportService retroactiveImportService;
    private final OrderPdfExportService orderPdfExportService;

    public OrderController(
            OrderService orderService,
            RetroactiveImportService retroactiveImportService,
            OrderPdfExportService orderPdfExportService) {
        this.orderService = orderService;
        this.retroactiveImportService = retroactiveImportService;
        this.orderPdfExportService = orderPdfExportService;
    }

    @GetMapping("/")
    public String index(Model model) {
        OrderForm form = new OrderForm();
        form.getItems().add(new OrderItemForm());
        prepareFormScreen(model, form, BigDecimal.ZERO, null);
        return "index";
    }

    @PostMapping("/pedido/adicionar-item")
    public String addItem(@ModelAttribute("orderForm") OrderForm form, Model model) {
        form.getItems().add(new OrderItemForm());
        BigDecimal total = orderService.calculateTotal(form);
        prepareFormScreen(model, form, total, "Linha de item adicionada. Escolha o produto e a quantidade.");
        return "index";
    }

    @PostMapping("/pedido/recalcular")
    public String recalculate(@ModelAttribute("orderForm") OrderForm form, Model model) {
        BigDecimal total = orderService.calculateTotal(form);
        prepareFormScreen(model, form, total, "Total atualizado com base nos itens e precos.");
        return "index";
    }

    @PostMapping("/pedido/finalizar")
    public String finalizeOrder(@Valid @ModelAttribute("orderForm") OrderForm form,
                                BindingResult bindingResult,
                                Model model) {
        if (form.getItems() == null || form.getItems().isEmpty()) {
            bindingResult.reject("items.empty", "Adicione pelo menos um produto no pedido");
        }

        BigDecimal total = orderService.calculateTotal(form);

        if (bindingResult.hasErrors()) {
            prepareFormScreen(model, form, total, null);
            return "index";
        }

        orderService.finalizeOrder(form);
        return "redirect:/pedidos?sucesso";
    }

    @PostMapping("/pedido/remover-item")
    public String removeItem(@ModelAttribute("orderForm") OrderForm form,
                             @RequestParam("index") int index,
                             Model model) {
        if (form.getItems() != null && index >= 0 && index < form.getItems().size()) {
            form.getItems().remove(index);
        }
        if (form.getItems() == null || form.getItems().isEmpty()) {
            form.getItems().add(new OrderItemForm());
        }
        BigDecimal total = orderService.calculateTotal(form);
        prepareFormScreen(model, form, total, "Item removido. Total recalculado.");
        return "index";
    }

    @GetMapping("/pedidos")
    public String listOrders(@RequestParam(required = false) String dataInicio,
                             @RequestParam(required = false) String dataFim,
                             @RequestParam(required = false) String pagamento,
                             Model model,
                             HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("loggedUser");
        LocalDate from = parseLocalDate(dataInicio);
        LocalDate to = parseLocalDate(dataFim);
        PaymentMethod pay = parsePaymentMethod(pagamento);
        List<OrderRecord> orders = orderService.listOrdersFiltered(from, to, pay);
        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", currentUser != null && currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("filterDataInicio", dataInicio != null ? dataInicio : "");
        model.addAttribute("filterDataFim", dataFim != null ? dataFim : "");
        model.addAttribute("filterPagamento", pagamento != null ? pagamento : "");
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("totalFiltrado", orderService.sumTotal(orders));
        return "orders";
    }

    @GetMapping("/relatorios")
    public String reports(@RequestParam(required = false) String dataInicio,
                          @RequestParam(required = false) String dataFim,
                          @RequestParam(required = false) String pagamento,
                          Model model) {
        LocalDate from = parseLocalDate(dataInicio);
        LocalDate to = parseLocalDate(dataFim);
        PaymentMethod pay = parsePaymentMethod(pagamento);
        List<OrderRecord> filtered = orderService.listOrdersFiltered(from, to, pay);
        model.addAttribute("todayRevenue", orderService.getTodayRevenue());
        model.addAttribute("monthRevenue", orderService.getCurrentMonthRevenue());
        model.addAttribute("dailyReport", orderService.buildDailyRevenueReport(filtered));
        model.addAttribute("monthlyReport", orderService.buildMonthlyRevenueReport(filtered));
        model.addAttribute("totalPeriodo", orderService.sumTotal(filtered));
        model.addAttribute("filterDataInicio", dataInicio != null ? dataInicio : "");
        model.addAttribute("filterDataFim", dataFim != null ? dataFim : "");
        model.addAttribute("filterPagamento", pagamento != null ? pagamento : "");
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "reports";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false, defaultValue = "nenhuma") String vistaExtra,
            Model model) {
        String vista = DashboardVistaUtil.copias(vistaExtra);
        var stats = orderService.buildDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("vistaExtra", vista);
        if ("pizza_pagamento".equals(vista)) {
            ChartPieHelper.buildFromMoneyMap(new LinkedHashMap<>(stats.getPaymentMonthTotals()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("barras_produto".equals(vista)) {
            model.addAttribute("chartBarrasExtra", ChartBarHelper.horizontalFromMoney(orderService.buildProductMonthTotals()));
        }
        return "dashboard";
    }

    @GetMapping("/importacao")
    public String importScreen() {
        return "import";
    }

    @PostMapping("/importacao/processar")
    public String processImport(@RequestParam("rawData") String rawData, Model model) {
        ImportResult result = retroactiveImportService.importFromCsvText(rawData);
        model.addAttribute("importedCount", result.getImportedCount());
        model.addAttribute("skippedCount", result.getSkippedCount());
        model.addAttribute("rawData", rawData);
        return "import";
    }

    @GetMapping("/pedidos/editar")
    public String editOrderPage(@RequestParam("id") String id, Model model) {
        OrderEditForm form = orderService.getOrderForEdit(id);
        model.addAttribute("orderEditForm", form);
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "order-edit";
    }

    @PostMapping("/pedidos/editar")
    public String editOrderSave(@ModelAttribute("orderEditForm") OrderEditForm form, Model model) {
        try {
            orderService.updateOrder(form);
            return "redirect:/pedidos?editado";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "order-edit";
        }
    }

    @GetMapping(value = "/pedidos/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String dataInicio,
                                            @RequestParam(required = false) String dataFim,
                                            @RequestParam(required = false) String pagamento) {
        List<OrderRecord> orders = orderService.listOrdersFiltered(
                parseLocalDate(dataInicio), parseLocalDate(dataFim), parsePaymentMethod(pagamento));
        StringBuilder csv = new StringBuilder();
        csv.append("pedido_id,data_hora,pagamento,produto,quantidade,valor_unitario,valor_item,valor_total_pedido\n");

        for (OrderRecord order : orders) {
            order.getItems().forEach(item -> csv.append(order.getId()).append(",")
                    .append(order.getCreatedAt()).append(",")
                    .append(order.getPaymentMethod().name()).append(",")
                    .append(item.getProductType().name()).append(",")
                    .append(item.getQuantity()).append(",")
                    .append(item.getUnitPrice()).append(",")
                    .append(item.getTotal()).append(",")
                    .append(order.getTotalAmount())
                    .append("\n"));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("pedidos.csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/pedidos/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String dataInicio,
                                             @RequestParam(required = false) String dataFim,
                                             @RequestParam(required = false) String pagamento) throws IOException {
        List<OrderRecord> orders = orderService.listOrdersFiltered(
                parseLocalDate(dataInicio), parseLocalDate(dataFim), parsePaymentMethod(pagamento));
        byte[] pdf = orderPdfExportService.build(orders, buildPdfFilterSummary(dataInicio, dataFim, pagamento));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("pedidos.pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private static String buildPdfFilterSummary(String dataInicio, String dataFim, String pagamento) {
        if ((dataInicio == null || dataInicio.isBlank())
                && (dataFim == null || dataFim.isBlank())
                && (pagamento == null || pagamento.isBlank())) {
            return "Todos os pedidos (sem filtro de data nem pagamento).";
        }
        StringBuilder sb = new StringBuilder();
        if (dataInicio != null && !dataInicio.isBlank()) {
            sb.append("Data inicio: ").append(dataInicio).append("; ");
        }
        if (dataFim != null && !dataFim.isBlank()) {
            sb.append("Data fim: ").append(dataFim).append("; ");
        }
        if (pagamento != null && !pagamento.isBlank()) {
            sb.append("Pagamento: ").append(pagamento).append(".");
        }
        return sb.toString().trim();
    }

    private void prepareFormScreen(Model model, OrderForm form, BigDecimal total, String feedbackMensagem) {
        model.addAttribute("orderForm", form);
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("total", total);
        model.addAttribute("productPricesJson", productPricesJson());
        if (feedbackMensagem != null && !feedbackMensagem.isBlank()) {
            model.addAttribute("feedbackMensagem", feedbackMensagem);
        }
    }

    private static String productPricesJson() {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (ProductType p : ProductType.values()) {
            sj.add("\"" + p.name() + "\":" + p.getUnitPrice().toPlainString());
        }
        return sj.toString();
    }

    private static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private static PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
