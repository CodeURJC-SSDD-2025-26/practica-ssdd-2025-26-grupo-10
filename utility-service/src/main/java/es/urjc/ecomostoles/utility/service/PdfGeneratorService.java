package es.urjc.ecomostoles.utility.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
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
 * Design: uses a PdfPageEventHelper to correctly paint the green sidebar
 * stripe on every page without creating a second PdfWriter (which was the
 * root cause of the previous plain/corrupt PDF output).
 */
@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    // ── Corporate design tokens ───────────────────────────────────────────────
    private static final Color CORPORATE_GREEN  = new Color(22, 101, 52);   // Deep forest green
    private static final Color ACCENT_GREEN     = new Color(34, 197, 94);   // Light accent
    private static final Color TEXT_DARK        = new Color(31, 41, 55);
    private static final Color TEXT_MEDIUM      = new Color(75, 85, 99);
    private static final Color BG_LIGHT         = new Color(240, 253, 244); // Very light green tint
    private static final Color BG_CARD          = new Color(248, 250, 252);
    private static final Color BORDER_LIGHT     = new Color(209, 250, 229);
    private static final Color BORDER_MEDIUM    = new Color(167, 243, 208);
    private static final Color HEADER_BG        = new Color(6, 78, 59);     // Very dark green header
    private static final Color STRIPE_BG        = new Color(16, 185, 129);  // Sidebar stripe

    /** Width (pts) of the decorative left-side stripe */
    private static final float SIDEBAR_WIDTH = 18f;

    // ── Page event helper — draws sidebar and page numbers ───────────────────
    private static class StripePageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();

            // Left stripe
            cb.setColorFill(STRIPE_BG);
            cb.rectangle(0, 0, SIDEBAR_WIDTH, PageSize.A4.getHeight());
            cb.fill();

            // Thin accent line next to stripe
            cb.setColorFill(ACCENT_GREEN);
            cb.rectangle(SIDEBAR_WIDTH, 0, 2f, PageSize.A4.getHeight());
            cb.fill();

            // Bottom bar
            cb.setColorFill(HEADER_BG);
            cb.rectangle(0, 0, PageSize.A4.getWidth(), 22f);
            cb.fill();

            // Page number on bottom bar
            try {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
                cb.beginText();
                cb.setFontAndSize(bf, 7f);
                cb.setColorFill(Color.WHITE);
                String pageText = "EcoMostoles \u00b7 Plataforma de Simbiosis Industrial \u00b7 Pagina " + writer.getPageNumber();
                cb.showTextAligned(Element.ALIGN_CENTER, pageText,
                        PageSize.A4.getWidth() / 2f, 8f, 0);
                cb.endText();
            } catch (Exception ignored) {
                // Font load failure is non-fatal; page number is decorative
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public byte[] generateAgreementReport(AgreementReportRequest request) {
        log.info("[PDF] Generating premium agreement report for ID: {}", request.agreementId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Margins: left=60 (sidebar+gap), right=40, top=50, bottom=40
            Document document = new Document(PageSize.A4, 60, 40, 50, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Register page event BEFORE opening the document
            writer.setPageEvent(new StripePageEvent());
            document.open();

            // ── Fonts ──────────────────────────────────────────────────────
            Font titleFont      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
            Font subtitleFont   = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(187, 247, 208));
            Font sectionFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, CORPORATE_GREEN);
            Font labelFont      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, TEXT_MEDIUM);
            Font valueFont      = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);
            Font valueBoldFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
            Font footerFont     = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);
            Font statusFont     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, CORPORATE_GREEN);
            Font smallFont      = FontFactory.getFont(FontFactory.HELVETICA, 7, TEXT_MEDIUM);
            Font idFont         = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(156, 163, 175));
            Font signLabelFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, TEXT_MEDIUM);

            // ── Header Banner ─────────────────────────────────────────────
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3.2f, 1.2f});

            // Title cell
            Phrase titlePhrase = new Phrase();
            titlePhrase.add(new Chunk("CERTIFICADO DE ACUERDO\n", titleFont));
            titlePhrase.add(new Chunk("Simbiosis Industrial · EcoMóstoles", subtitleFont));
            PdfPCell titleCell = new PdfPCell(titlePhrase);
            titleCell.setBackgroundColor(HEADER_BG);
            titleCell.setPaddingTop(22f);
            titleCell.setPaddingBottom(22f);
            titleCell.setPaddingLeft(20f);
            titleCell.setPaddingRight(10f);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);

            // Logo / seal cell
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBackgroundColor(HEADER_BG);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setPadding(12f);
            if (request.originCompanyLogo() != null && request.originCompanyLogo().length > 0) {
                try {
                    Image logo = Image.getInstance(request.originCompanyLogo());
                    logo.scaleToFit(60, 60);
                    logoCell.setImage(logo);
                } catch (Exception ex) {
                    log.warn("[PDF] Logo load error: {}", ex.getMessage());
                }
            }
            headerTable.addCell(logoCell);
            document.add(headerTable);

            // ── Document metadata strip ────────────────────────────────────
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(187, 247, 208));
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);

            String validationCode = "ECO-" +
                    java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                    "-" + request.agreementId();

            PdfPCell metaLeft = new PdfPCell(new Phrase("Código de Validación: " + validationCode, metaFont));
            metaLeft.setBackgroundColor(CORPORATE_GREEN);
            metaLeft.setBorder(Rectangle.NO_BORDER);
            metaLeft.setPaddingTop(6f);
            metaLeft.setPaddingBottom(6f);
            metaLeft.setPaddingLeft(15f);
            metaLeft.setVerticalAlignment(Element.ALIGN_MIDDLE);
            metaTable.addCell(metaLeft);

            PdfPCell metaRight = new PdfPCell(new Phrase("Emitido: " + request.registrationDate(), metaFont));
            metaRight.setBackgroundColor(CORPORATE_GREEN);
            metaRight.setBorder(Rectangle.NO_BORDER);
            metaRight.setPaddingTop(6f);
            metaRight.setPaddingBottom(6f);
            metaRight.setPaddingRight(15f);
            metaRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaRight.setVerticalAlignment(Element.ALIGN_MIDDLE);
            metaTable.addCell(metaRight);
            document.add(metaTable);

            document.add(new Paragraph("\n"));

            // ── Section 1: Parties ────────────────────────────────────────
            addSectionTitle(document, "ENTIDADES PARTICIPANTES", sectionFont);

            PdfPTable partiesTable = new PdfPTable(2);
            partiesTable.setWidthPercentage(100);
            partiesTable.setSpacingBefore(8f);
            partiesTable.setSpacingAfter(4f);
            partiesTable.setWidths(new float[]{1f, 1f});

            partiesTable.addCell(createPartyCell(
                    "▶  EMPRESA PROVEEDORA",
                    request.originCompanyName(),
                    request.originCompanyTaxId(),
                    request.originCompanyAddress(),
                    request.originCompanyPhone(),
                    request.originCompanySector(),
                    labelFont, valueFont, valueBoldFont));

            partiesTable.addCell(createPartyCell(
                    "▶  EMPRESA RECEPTORA",
                    request.destinationCompanyName(),
                    request.destinationCompanyTaxId(),
                    request.destinationCompanyAddress(),
                    request.destinationCompanyPhone(),
                    request.destinationCompanySector(),
                    labelFont, valueFont, valueBoldFont));

            document.add(partiesTable);
            document.add(new Paragraph("\n"));

            // ── Section 2: Agreement specs ────────────────────────────────
            addSectionTitle(document, "ESPECIFICACIONES DEL ACUERDO", sectionFont);

            PdfPTable detailsTable = new PdfPTable(4);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(8f);
            detailsTable.setSpacingAfter(4f);
            detailsTable.setWidths(new float[]{1.2f, 1.8f, 1.2f, 1.8f});

            addDetailCell(detailsTable, "RECURSO INTERCAMBIADO", request.exchangedMaterial(), labelFont, valueFont);
            addDetailCell(detailsTable, "ESTADO CONTRACTUAL",
                    request.status() != null ? request.status().toUpperCase() : "PENDIENTE", labelFont, statusFont);
            addDetailCell(detailsTable, "VOLUMEN TOTAL",
                    request.quantity() + " " + request.unit(), labelFont, valueFont);
            addDetailCell(detailsTable, "FECHA DE RECOGIDA",
                    request.pickupDate() != null ? request.pickupDate().toString() : "A DETERMINAR", labelFont, valueFont);
            document.add(detailsTable);
            document.add(new Paragraph("\n"));

            // ── Section 3: Financials & Sustainability ────────────────────
            addSectionTitle(document, "VALORACIÓN ECONÓMICA E IMPACTO AMBIENTAL", sectionFont);

            NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));

            PdfPTable impactTable = new PdfPTable(3);
            impactTable.setWidthPercentage(100);
            impactTable.setSpacingBefore(8f);
            impactTable.setSpacingAfter(4f);

            addImpactCard(impactTable, "💶  VALOR ESTIMADO",
                    currencyFmt.format(request.agreedPrice() != null ? request.agreedPrice() : 0.0),
                    labelFont, valueBoldFont, BG_CARD, BORDER_MEDIUM);
            addImpactCard(impactTable, "📊  TASA DE GESTIÓN",
                    currencyFmt.format(request.platformCommission() != null ? request.platformCommission() : 0.0),
                    labelFont, valueBoldFont, BG_CARD, BORDER_MEDIUM);
            addImpactCard(impactTable, "🌱  AHORRO CO₂",
                    String.format("%.2f kg CO₂e", request.co2Impact() != null ? request.co2Impact() : 0.0),
                    labelFont, statusFont, BG_LIGHT, BORDER_MEDIUM);
            document.add(impactTable);

            // ── Section 4: Notes (optional) ───────────────────────────────
            if (request.notes() != null && !request.notes().isBlank()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "CLÁUSULAS Y NOTAS ADICIONALES", sectionFont);

                PdfPTable nt = new PdfPTable(1);
                nt.setWidthPercentage(100);
                nt.setSpacingBefore(8f);
                PdfPCell notesCell = new PdfPCell(new Phrase(request.notes(), valueFont));
                notesCell.setPadding(12f);
                notesCell.setBackgroundColor(BG_CARD);
                notesCell.setBorderColor(BORDER_MEDIUM);
                notesCell.setBorderWidth(1f);
                nt.addCell(notesCell);
                document.add(nt);
            }

            // ── Section 5: Signatures & Seal ──────────────────────────────
            document.add(new Paragraph("\n\n"));

            PdfPTable signTable = new PdfPTable(3);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{1.5f, 1f, 1.5f});

            // Supplier signature
            PdfPCell s1 = buildSignatureCell(
                    request.originCompanyName() != null ? request.originCompanyName() : "Empresa Proveedora",
                    "Firma Proveedor", signLabelFont, smallFont);
            signTable.addCell(s1);

            // Platform seal (centre)
            PdfPCell s2 = new PdfPCell();
            s2.setBorder(Rectangle.NO_BORDER);
            s2.setHorizontalAlignment(Element.ALIGN_CENTER);
            s2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            s2.setPadding(5f);
            if (request.platformSeal() != null && request.platformSeal().length > 0) {
                try {
                    Image seal = Image.getInstance(request.platformSeal());
                    seal.scaleToFit(80, 80);
                    s2.setImage(seal);
                } catch (Exception ex) {
                    // Seal is decorative; silently skip
                }
            } else {
                // Fallback text seal
                Paragraph sealTxt = new Paragraph("✦ SELLO OFICIAL ✦\nEcoMóstoles",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, CORPORATE_GREEN));
                sealTxt.setAlignment(Element.ALIGN_CENTER);
                s2.addElement(sealTxt);
            }
            signTable.addCell(s2);

            // Receiver signature
            PdfPCell s3 = buildSignatureCell(
                    request.destinationCompanyName() != null ? request.destinationCompanyName() : "Empresa Receptora",
                    "Firma Receptor", signLabelFont, smallFont);
            signTable.addCell(s3);

            document.add(signTable);

            // ── Legal Footer ───────────────────────────────────────────────
            document.add(new Paragraph("\n"));
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(BORDER_MEDIUM);
            ls.setLineWidth(0.5f);
            document.add(new Chunk(ls));

            Paragraph footer = new Paragraph(
                    "Documento oficial generado por EcoMóstoles — Plataforma de Simbiosis Industrial.\n" +
                    "La autenticidad de este certificado puede verificarse en ecomostoles.urjc.es mediante el código de validación.\n" +
                    "Este acuerdo contribuye a los Objetivos de Desarrollo Sostenible (ODS 12) de economía circular.",
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(8f);
            document.add(footer);

            document.close();
            log.info("[PDF] Successfully generated {} bytes for agreement #{}", baos.size(), request.agreementId());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("[PDF] Generation failed for ID: {}", request.agreementId(), e);
            throw new PdfGenerationException("PDF generation failed", e);
        }
    }

    // ── Layout Helpers ────────────────────────────────────────────────────────

    /** Adds a bold green section title with a divider line. */
    private void addSectionTitle(Document doc, String text, Font font) throws DocumentException {
        // Decorative pill background via table
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(4f);
        titleTable.setSpacingAfter(2f);

        PdfPCell titleCell = new PdfPCell(new Phrase("  " + text, font));
        titleCell.setBackgroundColor(BG_LIGHT);
        titleCell.setBorderWidthLeft(3f);
        titleCell.setBorderColorLeft(CORPORATE_GREEN);
        titleCell.setBorderWidthRight(0f);
        titleCell.setBorderWidthTop(0f);
        titleCell.setBorderWidthBottom(0f);
        titleCell.setPaddingTop(5f);
        titleCell.setPaddingBottom(5f);
        titleCell.setPaddingLeft(8f);
        titleTable.addCell(titleCell);
        doc.add(titleTable);
    }

    /** Creates a party (company) info card cell. */
    private PdfPCell createPartyCell(String title, String name, String taxId,
                                     String addr, String phone, String sector,
                                     Font labelFont, Font valueFont, Font nameBoldFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_MEDIUM);
        cell.setBorderWidth(1f);
        cell.setPadding(12f);
        cell.setBackgroundColor(BG_CARD);

        cell.addElement(new Phrase(title + "\n", labelFont));
        cell.addElement(new Phrase(name != null ? name.toUpperCase() : "N/A", nameBoldFont));

        Paragraph sep = new Paragraph(" ");
        sep.setSpacingAfter(3f);
        cell.addElement(sep);

        if (taxId   != null && !taxId.isBlank())   cell.addElement(buildLabelValue("CIF", taxId, labelFont, valueFont));
        if (addr    != null && !addr.isBlank())    cell.addElement(buildLabelValue("DIR", addr, labelFont, valueFont));
        if (phone   != null && !phone.isBlank())   cell.addElement(buildLabelValue("TEL", phone, labelFont, valueFont));
        if (sector  != null && !sector.isBlank())  cell.addElement(buildLabelValue("SECTOR", sector, labelFont, valueFont));

        return cell;
    }

    /** Single-line "LABEL: value" helper. */
    private Phrase buildLabelValue(String label, String value, Font labelFont, Font valueFont) {
        Phrase p = new Phrase();
        p.add(new Chunk(label + ": ", labelFont));
        p.add(new Chunk(value, valueFont));
        p.add(Chunk.NEWLINE);
        return p;
    }

    /** Adds a 2-column label/value pair spanning the full row. */
    private void addDetailCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBackgroundColor(BG_LIGHT);
        lCell.setBorderColor(BORDER_LIGHT);
        lCell.setBorderWidth(1f);
        lCell.setPaddingTop(8f);
        lCell.setPaddingBottom(8f);
        lCell.setPaddingLeft(10f);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        vCell.setBorderColor(BORDER_LIGHT);
        vCell.setBorderWidth(1f);
        vCell.setPaddingTop(8f);
        vCell.setPaddingBottom(8f);
        vCell.setPaddingLeft(10f);
        table.addCell(vCell);
    }

    /** Adds a centered impact/financial metric card. */
    private void addImpactCard(PdfPTable table, String label, String value,
                                Font labelFont, Font valueFont,
                                Color bgColor, Color borderColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(borderColor);
        cell.setBorderWidth(1f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(14f);
        cell.setPaddingBottom(14f);
        cell.setPaddingLeft(8f);
        cell.setPaddingRight(8f);
        cell.setBackgroundColor(bgColor);

        Paragraph l = new Paragraph(label, labelFont);
        l.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l);

        Paragraph v = new Paragraph(value, valueFont);
        v.setAlignment(Element.ALIGN_CENTER);
        v.setSpacingBefore(4f);
        cell.addElement(v);

        table.addCell(cell);
    }

    /** Builds a signature block cell. */
    private PdfPCell buildSignatureCell(String companyName, String role, Font labelFont, Font smallFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);

        Paragraph nameParagraph = new Paragraph(companyName, smallFont);
        nameParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(nameParagraph);

        // Signature line
        Paragraph line = new Paragraph("\n\n_______________________________", smallFont);
        line.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(line);

        Paragraph roleP = new Paragraph(role, labelFont);
        roleP.setAlignment(Element.ALIGN_CENTER);
        roleP.setSpacingBefore(3f);
        cell.addElement(roleP);

        return cell;
    }
}
