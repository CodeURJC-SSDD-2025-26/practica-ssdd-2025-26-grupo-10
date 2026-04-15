package es.urjc.ecomostoles.backend.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.format.DateTimeFormatter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.text.NumberFormat;
import java.util.Locale;

@Controller
public class PdfExportController {

    private final AcuerdoService acuerdoService;

    public PdfExportController(AcuerdoService acuerdoService) {
        this.acuerdoService = acuerdoService;
    }

    @GetMapping("/acuerdo/{id}/pdf")
    public ResponseEntity<byte[]> generateAgreementPdf(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Acuerdo agreement = acuerdoService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado"));

        String userEmail = principal.getName();
        boolean isOwner = (agreement.getEmpresaOrigen() != null && agreement.getEmpresaOrigen().getEmailContacto().equals(userEmail)) ||
                          (agreement.getEmpresaDestino() != null && agreement.getEmpresaDestino().getEmailContacto().equals(userEmail));
        
        if (!isOwner) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Estilos de Fuente ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);

            // --- Logo de la Empresa (si existe) ---
            if (agreement.getEmpresaOrigen() != null && agreement.getEmpresaOrigen().getLogo() != null) {
                try {
                    Image logo = Image.getInstance(agreement.getEmpresaOrigen().getLogo());
                    logo.scaleToFit(80, 80);
                    logo.setAlignment(Image.RIGHT);
                    document.add(logo);
                } catch (Exception imgEx) {
                    // Silently ignore logo if it fails to process
                }
            }

            // --- Título ---
            Paragraph title = new Paragraph("ECO-MÓSTOLES: RESGUARDO DE ACUERDO OFICIAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // --- Separador ---
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(Color.LIGHT_GRAY);
            document.add(new Chunk(separator));
            document.add(new Paragraph("\n"));

            // --- Tabla de Detalles ---
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{1.5f, 3.5f});

            NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
            String precioFormateado = formatoMoneda.format(agreement.getPrecioAcordado());

            addTableCell(table, "ID de Acuerdo:", agreement.getId().toString(), labelFont, normalFont);
            addTableCell(table, "Material:", agreement.getMaterialIntercambiado(), labelFont, normalFont);
            addTableCell(table, "Cantidad:", agreement.getCantidad() + " " + (agreement.getUnidad() != null ? agreement.getUnidad() : "uds"), labelFont, normalFont);
            addTableCell(table, "Precio Acordado:", precioFormateado, labelFont, normalFont);
            
            String originName = agreement.getEmpresaOrigen() != null ? agreement.getEmpresaOrigen().getNombreComercial() : "Empresa no disponible";
            String destName = agreement.getEmpresaDestino() != null ? agreement.getEmpresaDestino().getNombreComercial() : "Empresa no disponible";
            
            addTableCell(table, "Empresa Proveedora:", originName, labelFont, normalFont);
            addTableCell(table, "Empresa Receptora:", destName, labelFont, normalFont);
            addTableCell(table, "Estado del Acuerdo:", agreement.getEstado().name(), labelFont, normalFont);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = agreement.getFechaRegistro() != null ? agreement.getFechaRegistro().format(formatter) : "Fecha no disponible";
            addTableCell(table, "Fecha de Registro:", fechaFormateada, labelFont, normalFont);

            document.add(table);

            // --- Pie de página ---
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("Este documento es un comprobante automático generado por la plataforma Eco-Móstoles para el fomento de la simbiosis industrial.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "albaran_acuerdo_" + id + ".pdf");
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addTableCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8f);
        table.addCell(valueCell);
    }
}
