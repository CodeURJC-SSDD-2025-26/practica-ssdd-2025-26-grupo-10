package es.urjc.ecomostoles.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Document Generation Factory for Administrative Intelligence.
 * 
 * Orchestrates the programmatic construction of structured data exports (PDF and CSV). 
 * Implements in-memory streaming via ByteArrayOutputStream to ensure high performance 
 * and avoid localized file-system persistence overhead during concurrent request 
 * processing.
 */
@Service
public class ReportService {

    /**
     * Generates a PDF report containing a table of registered companies.
     * 
     * @param companies List of companies to include.
     * @return Byte array of the generated PDF.
     */
    public byte[] generateUsersPdf(List<Company> companies) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(new Color(25, 135, 84)); // EcoMostoles Green

        Paragraph p = new Paragraph("Reporte Administrativo: EcoMostoles", fontTitle);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        document.add(new Paragraph(" ")); // Spacer

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        try {
            table.setWidths(new float[] { 1.5f, 3.5f, 3.5f, 2.0f });
        } catch (Exception ignored) {
        }
        table.setSpacingBefore(10);

        writeTableHeader(table);

        for (Company comp : companies) {
            table.addCell(String.valueOf(comp.getId()));
            table.addCell(comp.getCommercialName());
            table.addCell(comp.getContactEmail());
            table.addCell(comp.getRole());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(25, 135, 84));
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new com.lowagie.text.Phrase("ID", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Nombre", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Email", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Rol", font));
        table.addCell(cell);
    }

    /**
     * Generates a CSV report of offers.
     * 
     * @param offers List of offer summaries.
     * @return Byte array of the generated CSV (UTF-8).
     */
    public byte[] generateOffersCsv(List<OfferSummary> offers) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Titulo;Empresa;Cantidad;Estado\n");

        for (OfferSummary o : offers) {
            sb.append(String.format("%d;\"%s\";\"%s\";%s;\"%s\"\n",
                    o.getId(),
                    o.getTitle(),
                    o.getCompany() != null ? o.getCompany().getCommercialName() : "N/A",
                    o.getQuantity() != null ? o.getQuantity().toString() : "0",
                    o.getStatus() != null ? o.getStatus().toString() : "N/A"));
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
