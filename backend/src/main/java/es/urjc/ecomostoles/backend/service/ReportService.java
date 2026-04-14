package es.urjc.ecomostoles.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import es.urjc.ecomostoles.backend.dto.OfertaResumen;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service to handle document generation (PDF, CSV).
 * Extracted from AdminController to improve separation of concerns.
 */
@Service
public class ReportService {

    /**
     * Generates a PDF report containing a table of registered companies.
     * 
     * @param empresas List of companies to include.
     * @return Byte array of the generated PDF.
     */
    public byte[] generarPdfUsuarios(List<Empresa> empresas) {
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
            table.setWidths(new float[] {1.5f, 3.5f, 3.5f, 2.0f});
        } catch (Exception ignored) {}
        table.setSpacingBefore(10);

        writeTableHeader(table);

        for (Empresa emp : empresas) {
            table.addCell(String.valueOf(emp.getId()));
            table.addCell(emp.getNombreComercial());
            table.addCell(emp.getEmailContacto());
            table.addCell(emp.getRol());
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
     * @param ofertas List of offer summaries.
     * @return Byte array of the generated CSV (UTF-8).
     */
    public byte[] generarCsvOfertas(List<OfertaResumen> ofertas) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Titulo;Empresa;Cantidad;Estado\n");

        for (OfertaResumen o : ofertas) {
            sb.append(String.format("%d;%s;%s;%s;%s\n",
                    o.getId(),
                    o.getTitulo(),
                    o.getEmpresa() != null ? o.getEmpresa().getNombreComercial() : "N/A",
                    o.getCantidad() != null ? o.getCantidad().toString() : "0",
                    o.getEstado() != null ? o.getEstado().toString() : "N/A"
            ));
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
