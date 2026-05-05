package es.urjc.ecomostoles.utility.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import es.urjc.ecomostoles.utility.dto.AgreementReportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Document Generation Service for Agreement PDF reports.
 *
 * Fully self-contained: receives a plain-data {@link AgreementReportRequest}
 * and produces an in-memory PDF byte array using OpenPDF.
 * No database access, no Spring Security context — pure transformation.
 *
 * Design: logic migrated and decoupled from app-service PdfExportController
 * to enforce the microservice boundary.
 */
@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    // ── Corporate design tokens ───────────────────────────────────────────────
    private static final Color CORPORATE_GREEN  = new Color(46, 125, 50);
    private static final Color BORDER_LIGHT     = new Color(230, 230, 230);
    private static final Color SEPARATOR_GRAY   = new Color(210, 210, 210);

    /**
     * Generates a styled PDF agreement report from the provided request payload.
     *
     * @param request all data fields required to render the document.
     * @return PDF binary content as a byte array.
     * @throws PdfGenerationException if OpenPDF fails during document construction.
     */
    public byte[] generateAgreementReport(AgreementReportRequest request) {

        log.info("[PDF] Generating agreement report for ID: {}", request.agreementId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ── Fonts ─────────────────────────────────────────────────────
            Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    16, Color.WHITE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    10, CORPORATE_GREEN);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA,         10, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE,  8, Color.GRAY);

            // ── Header table (title + optional logo) ──────────────────────
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{4f, 1.2f});

            PdfPCell titleCell = new PdfPCell(new Phrase("RESGUARDO DE ACUERDO OFICIAL", titleFont));
            titleCell.setBackgroundColor(CORPORATE_GREEN);
            titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPadding(12f);
            titleCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(titleCell);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if (request.originCompanyLogo() != null && request.originCompanyLogo().length > 0) {
                try {
                    Image logo = Image.getInstance(request.originCompanyLogo());
                    logo.scaleToFit(50, 50);
                    logoCell.setImage(logo);
                } catch (Exception ex) {
                    log.warn("[PDF] Could not embed origin company logo: {}", ex.getMessage());
                }
            }
            headerTable.addCell(logoCell);
            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // ── Main data table ───────────────────────────────────────────
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);
            table.setWidths(new float[]{1.8f, 3.2f});

            NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));

            addRow(table, "ID del Acuerdo",      safeStr(request.agreementId()),                   headerFont, normalFont);
            addRow(table, "Estado",               safeStr(request.status()),                         headerFont, normalFont);
            addRow(table, "Empresa Proveedora",   safeStr(request.originCompanyName()),              headerFont, normalFont);
            addRow(table, "Empresa Receptora",    safeStr(request.destinationCompanyName()),         headerFont, normalFont);
            addRow(table, "Producto / Material",  safeStr(request.exchangedMaterial()),              headerFont, normalFont);
            addRow(table, "Cantidad Pactada",
                    request.quantity() != null
                            ? request.quantity() + " " + safeStr(request.unit())
                            : "N/A",
                    headerFont, normalFont);
            addRow(table, "Importe Acordado",
                    request.agreedPrice() != null ? currencyFmt.format(request.agreedPrice()) : "N/A",
                    headerFont, normalFont);

            if (request.platformCommission() != null) {
                addRow(table, "Comisión Plataforma",
                        currencyFmt.format(request.platformCommission()),
                        headerFont, normalFont);
            }

            addRow(table, "Impacto CO₂ Evitado",
                    request.co2Impact() != null ? String.format("%.2f kg CO₂", request.co2Impact()) : "N/A",
                    headerFont, normalFont);
            addRow(table, "Fecha de Recogida",    safeStr(request.pickupDate()),     headerFont, normalFont);
            addRow(table, "Fecha de Emisión",     safeStr(request.registrationDate()), headerFont, normalFont);

            if (request.notes() != null && !request.notes().isBlank()) {
                addRow(table, "Observaciones", request.notes(), headerFont, normalFont);
            }

            document.add(table);

            // ── Footer ────────────────────────────────────────────────────
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(SEPARATOR_GRAY);
            document.add(new Chunk(ls));

            Paragraph footer = new Paragraph(
                    "Este documento es un comprobante automático generado por la plataforma EcoMóstoles.\n" +
                    "Fomentando la economía circular y la simbiosis industrial.",
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15f);
            document.add(footer);

            document.close();

            log.info("[PDF] Agreement report generated successfully ({} bytes)", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("[PDF] Failed to generate agreement report for ID: {}", request.agreementId(), e);
            throw new PdfGenerationException("PDF generation failed for agreement " + request.agreementId(), e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label.toUpperCase(), labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(BORDER_LIGHT);
        labelCell.setPaddingTop(10f);
        labelCell.setPaddingBottom(10f);
        labelCell.setPaddingLeft(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(BORDER_LIGHT);
        valueCell.setPaddingTop(10f);
        valueCell.setPaddingBottom(10f);
        table.addCell(valueCell);
    }

    private String safeStr(Object value) {
        return value != null ? value.toString() : "N/A";
    }
}
