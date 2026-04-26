package br.com.sistemacopias.service;

import br.com.sistemacopias.model.OrderItem;
import br.com.sistemacopias.model.OrderRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gera PDF de pedidos com layout em tabelas (PDFBox + Helvetica).
 */
@Service
public class OrderPdfExportService {

    private static final PDRectangle PAGE = PDRectangle.A4;
    private static final float PAGE_W = PAGE.getWidth();
    private static final float PAGE_H = PAGE.getHeight();
    private static final float M = 40f;
    private static final float INNER_W = PAGE_W - 2 * M;
    private static final float FOOTER_H = 28f;
    private static final float BOTTOM_MIN = M + FOOTER_H;

    /** Limites da grelha (evita o nome do produto tapar Qtd / Unit). */
    private static final float COL_PROD_L = M;
    private static final float COL_PROD_R = M + 218f;
    private static final float COL_QTD_L = COL_PROD_R + 5f;
    private static final float COL_QTD_R = COL_PROD_R + 44f;
    private static final float COL_UNIT_L = COL_QTD_R + 5f;
    private static final float COL_UNIT_R = COL_UNIT_L + 82f;
    private static final float COL_SUB_R = PAGE_W - M;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")));

    static {
        MONEY.setMinimumFractionDigits(2);
        MONEY.setMaximumFractionDigits(2);
    }

    public byte[] build(List<OrderRecord> orders, String filtroResumo) throws IOException {
        PDFont font = PDType1Font.HELVETICA;
        PDFont bold = PDType1Font.HELVETICA_BOLD;

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PAGE);
            document.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(document, page);
            int pageNum = 1;

            float y = PAGE_H - M;

            y = drawTitleBlock(cs, bold, font, y, orders.size(), filtroResumo);
            cs.setNonStrokingColor(0f, 0f, 0f);

            for (OrderRecord order : orders) {
                float blockH = estimateOrderBlockHeight(order);
                if (y - blockH < BOTTOM_MIN) {
                    drawFooter(cs, font, pageNum++);
                    cs.close();
                    page = new PDPage(PAGE);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = PAGE_H - M;
                    y = drawContinuationHeader(cs, bold, y);
                }
                y = drawOrderBlock(cs, bold, font, order, y);
            }

            drawFooter(cs, font, pageNum);
            cs.close();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static float drawTitleBlock(PDPageContentStream cs, PDFont bold, PDFont font, float y, int orderCount, String filtroResumo)
            throws IOException {
        y -= drawCenteredTitle(cs, bold, 18, "Relatorio de pedidos", y);
        y -= 6;
        y -= drawTextLeft(cs, font, 10, "Gerado em " + DT.format(LocalDateTime.now()) + "  |  Total de pedidos: " + orderCount, M, y, INNER_W);
        if (filtroResumo != null && !filtroResumo.isBlank()) {
            y -= 4;
            y -= drawTextLeft(cs, font, 9, "Filtros: " + toPdfString(filtroResumo), M, y, INNER_W);
        }
        y -= 14;
        drawThinLine(cs, M, PAGE_W - M, y);
        y -= 10;
        return y;
    }

    private static float drawContinuationHeader(PDPageContentStream cs, PDFont bold, float y) throws IOException {
        y -= drawTextLeft(cs, bold, 11, "Relatorio de pedidos (continuacao)", M, y, INNER_W);
        y -= 16;
        return y;
    }

    private static float estimateOrderBlockHeight(OrderRecord order) {
        float orderHeader = 26f + 10f;
        float tableRows = 16f * (1 + order.getItems().size());
        float totalBand = 6f + 22f + 14f;
        return orderHeader + tableRows + totalBand;
    }

    private static float drawOrderBlock(PDPageContentStream cs, PDFont bold, PDFont font, OrderRecord order, float y)
            throws IOException {
        float bandBottom = y - 26;
        cs.setNonStrokingColor(0.12f, 0.31f, 0.65f);
        cs.addRect(M, bandBottom, INNER_W, 26);
        cs.fill();
        cs.setNonStrokingColor(1f, 1f, 1f);
        String head = "Pedido " + order.getId() + "   |   " + DT.format(order.getCreatedAt()) + "   |   "
                + toPdfString(order.getPaymentMethod().getDescricao());
        drawTextLeft(cs, bold, 10, head, M + 8, y - 6, INNER_W - 16);
        cs.setNonStrokingColor(0f, 0f, 0f);

        float rowH = 16f;
        float tableTop = bandBottom - 10;
        int bodyRows = order.getItems().size();
        float totalBandH = 22f;
        float tableBottom = tableTop - rowH - bodyRows * rowH - totalBandH;

        cs.setStrokingColor(0.55f, 0.62f, 0.7f);
        cs.setLineWidth(0.7f);
        cs.addRect(M, tableBottom, INNER_W, tableTop - tableBottom);
        cs.stroke();

        for (float vx1 : new float[]{COL_PROD_R, COL_QTD_R, COL_UNIT_R}) {
            cs.moveTo(vx1, tableBottom);
            cs.lineTo(vx1, tableTop);
            cs.stroke();
        }
        cs.setStrokingColor(0f, 0f, 0f);

        float ry = tableTop;

        cs.setNonStrokingColor(0.93f, 0.94f, 0.96f);
        cs.addRect(M, ry - rowH, INNER_W, rowH);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);
        ry -= rowH;
        drawRowHeader(cs, bold, 9, ry + 3);
        drawHLine(cs, M, PAGE_W - M, ry);

        boolean alt = false;
        for (OrderItem item : order.getItems()) {
            float rowBottom = ry - rowH;
            if (alt) {
                cs.setNonStrokingColor(0.97f, 0.98f, 1f);
                cs.addRect(M, rowBottom, INNER_W, rowH);
                cs.fill();
                cs.setNonStrokingColor(0f, 0f, 0f);
            }
            alt = !alt;
            ry = rowBottom;
            drawItemRow(cs, font, 9, item, rowBottom, rowH);
            drawHLine(cs, M, PAGE_W - M, ry);
        }

        float totalBottom = ry - 6;
        cs.setNonStrokingColor(0.9f, 0.93f, 0.97f);
        cs.addRect(M, totalBottom, INNER_W, totalBandH);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.setStrokingColor(0.55f, 0.62f, 0.7f);
        cs.setLineWidth(0.5f);
        cs.addRect(M, totalBottom, INNER_W, totalBandH);
        cs.stroke();
        cs.setStrokingColor(0f, 0f, 0f);

        float baselineTotal = totalBottom + 7;
        String totalLabel = "Total do pedido";
        String totalVal = "R$ " + MONEY.format(order.getTotalAmount());
        cs.beginText();
        cs.setFont(bold, 10);
        cs.newLineAtOffset(M + 8, baselineTotal);
        cs.showText(toPdfString(totalLabel));
        cs.endText();
        drawRightText(cs, bold, 10, totalVal, COL_SUB_R - 6, baselineTotal);

        return totalBottom - 12;
    }

    private static void drawRowHeader(PDPageContentStream cs, PDFont bold, float size, float baselineY) throws IOException {
        cs.setNonStrokingColor(0f, 0f, 0f);
        drawLeftText(cs, bold, size, "Produto", COL_PROD_L + 4, baselineY);
        drawLeftText(cs, bold, size, "Qtd", COL_QTD_L, baselineY);
        drawLeftText(cs, bold, size, "Unit. (R$)", COL_UNIT_L, baselineY);
        drawRightText(cs, bold, size, "Subtotal (R$)", COL_SUB_R - 4, baselineY);
    }

    private static void drawItemRow(PDPageContentStream cs, PDFont font, float size, OrderItem item, float rowBottom, float rowH)
            throws IOException {
        float baselineY = rowBottom + (rowH - size) / 2f;
        String desc = item.getProductType() != null ? item.getProductType().getDescricao() : "-";
        if (desc.isEmpty()) {
            desc = "-";
        }
        String raw = desc + " | Qtd " + item.getQuantity()
                + " | Unit R$ " + fmtPdfMoney(item.getUnitPrice())
                + " | Item R$ " + fmtPdfMoney(item.getTotal());
        String line = truncate(raw, font, size, INNER_W - 14f);

        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(M + 6f, baselineY);
        cs.showText(toPdfString(line));
        cs.endText();
    }

    private static String fmtPdfMoney(BigDecimal v) {
        if (v == null) {
            return "0,00";
        }
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private static void drawLeftText(PDPageContentStream cs, PDFont font, float size, String text, float x, float baselineY)
            throws IOException {
        String t = toPdfString(text);
        if (t.isEmpty()) {
            t = "-";
        }
        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, baselineY);
        cs.showText(t);
        cs.endText();
    }

    private static void drawRightText(PDPageContentStream cs, PDFont font, float size, String text, float rightX, float baselineY)
            throws IOException {
        String t = toPdfString(text);
        if (t.isEmpty()) {
            t = "-";
        }
        float w = font.getStringWidth(t) / 1000f * size;
        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(rightX - w, baselineY);
        cs.showText(t);
        cs.endText();
    }

    private static float drawCenteredTitle(PDPageContentStream cs, PDFont bold, float size, String text, float yTop)
            throws IOException {
        String t = toPdfString(text);
        float w = bold.getStringWidth(t) / 1000f * size;
        float x = (PAGE_W - w) / 2f;
        float baseline = yTop - size;
        cs.beginText();
        cs.setFont(bold, size);
        cs.newLineAtOffset(x, baseline);
        cs.showText(t);
        cs.endText();
        return size + 8;
    }

    private static float drawTextLeft(PDPageContentStream cs, PDFont font, float size, String text, float x, float yTop, float maxW)
            throws IOException {
        String t = toPdfString(text);
        List<String> lines = wrapText(t, font, size, maxW);
        float h = 0;
        float lineH = size + 3;
        float baseline = yTop - size;
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, baseline);
            cs.showText(line);
            cs.endText();
            baseline -= lineH;
            h += lineH;
        }
        return Math.max(h, lineH);
    }

    private static List<String> wrapText(String text, PDFont font, float size, float maxW) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String trial = cur.isEmpty() ? w : cur + " " + w;
            if (font.getStringWidth(trial) / 1000f * size <= maxW) {
                cur = new StringBuilder(trial);
            } else {
                if (!cur.isEmpty()) {
                    lines.add(cur.toString());
                }
                cur = new StringBuilder(w);
                while (cur.length() > 0 && font.getStringWidth(cur.toString()) / 1000f * size > maxW) {
                    cur.deleteCharAt(cur.length() - 1);
                }
                if (cur.isEmpty()) {
                    cur = new StringBuilder("...");
                }
            }
        }
        if (!cur.isEmpty()) {
            lines.add(cur.toString());
        }
        return lines;
    }

    private static String truncate(String text, PDFont font, float size, float maxW) throws IOException {
        String t = toPdfString(text);
        if (font.getStringWidth(t) / 1000f * size <= maxW) {
            return t;
        }
        String ell = "...";
        float ellW = font.getStringWidth(ell) / 1000f * size;
        while (t.length() > 1) {
            t = t.substring(0, t.length() - 1);
            if (font.getStringWidth(t) / 1000f * size + ellW <= maxW) {
                return t + ell;
            }
        }
        return ell;
    }

    private static void drawThinLine(PDPageContentStream cs, float x1, float x2, float y) throws IOException {
        cs.setStrokingColor(0.55f, 0.6f, 0.65f);
        cs.setLineWidth(0.8f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
        cs.setStrokingColor(0f, 0f, 0f);
    }

    private static void drawHLine(PDPageContentStream cs, float x1, float x2, float y) throws IOException {
        cs.setStrokingColor(0.75f, 0.78f, 0.82f);
        cs.setLineWidth(0.4f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
        cs.setStrokingColor(0f, 0f, 0f);
    }

    private static void drawFooter(PDPageContentStream cs, PDFont font, int pageNum) throws IOException {
        String ft = "Pagina " + pageNum;
        float size = 8.5f;
        float w = font.getStringWidth(ft) / 1000f * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(0.35f, 0.35f, 0.35f);
        cs.newLineAtOffset(PAGE_W - M - w, M * 0.35f);
        cs.showText(ft);
        cs.endText();
        cs.setNonStrokingColor(0f, 0f, 0f);
    }

    /**
     * Converte texto para algo seguro em Helvetica (WinAnsi / Latin-1).
     */
    private static String toPdfString(String s) {
        if (s == null) {
            return "";
        }
        try {
            CharsetEncoder enc = StandardCharsets.ISO_8859_1.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            ByteBuffer bb = enc.encode(CharBuffer.wrap(s));
            return StandardCharsets.ISO_8859_1.decode(bb).toString();
        } catch (CharacterCodingException e) {
            return "";
        }
    }
}
