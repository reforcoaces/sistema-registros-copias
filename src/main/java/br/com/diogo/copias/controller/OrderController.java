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
import br.com.sistemacopias.service.OrderService;
import br.com.sistemacopias.service.RetroactiveImportService;
import jakarta.validation.Valid;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final RetroactiveImportService retroactiveImportService;

    public OrderController(OrderService orderService, RetroactiveImportService retroactiveImportService) {
        this.orderService = orderService;
        this.retroactiveImportService = retroactiveImportService;
    }

    @GetMapping("/")
    public String index(Model model) {
        OrderForm form = new OrderForm();
        form.getItems().add(new OrderItemForm());
        prepareFormScreen(model, form, BigDecimal.ZERO);
        return "index";
    }

    @PostMapping("/pedido/adicionar-item")
    public String addItem(@ModelAttribute("orderForm") OrderForm form, Model model) {
        form.getItems().add(new OrderItemForm());
        BigDecimal total = orderService.calculateTotal(form);
        prepareFormScreen(model, form, total);
        return "index";
    }

    @PostMapping("/pedido/recalcular")
    public String recalculate(@ModelAttribute("orderForm") OrderForm form, Model model) {
        BigDecimal total = orderService.calculateTotal(form);
        prepareFormScreen(model, form, total);
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
            prepareFormScreen(model, form, total);
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
        prepareFormScreen(model, form, total);
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
    public String dashboard(Model model) {
        model.addAttribute("stats", orderService.buildDashboardStats());
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
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);

            float y = 800;
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            stream.newLineAtOffset(50, y);
            stream.showText("Relatorio de Pedidos");
            stream.endText();
            y -= 25;

            stream.setFont(PDType1Font.HELVETICA, 10);
            for (OrderRecord order : orders) {
                if (y < 80) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    y = 800;
                    stream.setFont(PDType1Font.HELVETICA, 10);
                }

                writeLine(stream, 50, y, "Pedido " + order.getId() + " | " + formatter.format(order.getCreatedAt())
                        + " | " + order.getPaymentMethod().getDescricao() + " | Total R$ " + order.getTotalAmount());
                y -= 14;
                for (var item : order.getItems()) {
                    writeLine(stream, 70, y, item.getProductType().getDescricao() + " | Qtd " + item.getQuantity()
                            + " | Unit R$ " + item.getUnitPrice() + " | Item R$ " + item.getTotal());
                    y -= 12;
                }
                y -= 8;
            }
            stream.close();
            document.save(output);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("pedidos.pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(output.toByteArray());
    }

    private void prepareFormScreen(Model model, OrderForm form, BigDecimal total) {
        model.addAttribute("orderForm", form);
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("total", total);
    }

    private void writeLine(PDPageContentStream stream, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
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
