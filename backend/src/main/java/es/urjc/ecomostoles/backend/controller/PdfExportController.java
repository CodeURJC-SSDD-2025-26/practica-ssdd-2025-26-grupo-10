package es.urjc.ecomostoles.backend.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import es.urjc.ecomostoles.backend.model.Agreement;
import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.service.AgreementService;
import es.urjc.ecomostoles.backend.service.CompanyService;
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

    private final AgreementService agreementService;
    private final CompanyService companyService;

    public PdfExportController(AgreementService agreementService, CompanyService companyService) {
        this.agreementService = agreementService;
        this.companyService = companyService;
    }

    @GetMapping("/acuerdo/{id}/pdf")
    public ResponseEntity<byte[]> generateAgreementPdf(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Recurso no encontrado"));

        String userEmail = principal.getName();
        Company loggedCompany = companyService.findByEmail(userEmail)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean isAdmin = loggedCompany.getRoles() != null && loggedCompany.getRoles().contains("ADMIN");
        boolean isOwner = (agreement.getOriginCompany() != null
                && agreement.getOriginCompany().getContactEmail().equals(userEmail)) ||
                (agreement.getDestinationCompany() != null
                        && agreement.getDestinationCompany().getContactEmail().equals(userEmail));

        if (!isAdmin && !isOwner) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a este recurso");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Font Styles ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);

            // --- Company Logo (if exists) ---
            if (agreement.getOriginCompany() != null && agreement.getOriginCompany().getLogo() != null) {
                try {
                    Image logo = Image.getInstance(agreement.getOriginCompany().getLogo());
                    logo.scaleToFit(80, 80);
                    logo.setAlignment(Image.RIGHT);
                    document.add(logo);
                } catch (Exception imgEx) {
                    // Silently ignore logo if it fails to process
                }
            }

            // --- Title ---
            Paragraph title = new Paragraph("ECO-MÓSTOLES: RESGUARDO DE ACUERDO OFICIAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // --- Separator ---
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(Color.LIGHT_GRAY);
            document.add(new Chunk(separator));
            document.add(new Paragraph("\n"));

            // --- Detail Table ---
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[] { 1.5f, 3.5f });

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
            String formattedPrice = currencyFormat.format(agreement.getAgreedPrice());

            addTableCell(table, "ID de Acuerdo:", agreement.getId().toString(), labelFont, normalFont);
            addTableCell(table, "Material:", agreement.getExchangedMaterial(), labelFont, normalFont);
            addTableCell(table, "Cantidad:",
                    agreement.getQuantity() + " " + (agreement.getUnit() != null ? agreement.getUnit() : "uds"),
                    labelFont, normalFont);
            addTableCell(table, "Precio Acordado:", formattedPrice, labelFont, normalFont);

            String originName = agreement.getOriginCompany() != null ? agreement.getOriginCompany().getCommercialName()
                    : "Empresa no disponible";
            String destName = agreement.getDestinationCompany() != null
                    ? agreement.getDestinationCompany().getCommercialName()
                    : "Empresa no disponible";

            addTableCell(table, "Empresa Proveedora:", originName, labelFont, normalFont);
            addTableCell(table, "Empresa Receptora:", destName, labelFont, normalFont);
            addTableCell(table, "Estado del Acuerdo:", agreement.getStatus().name(), labelFont, normalFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = agreement.getRegistrationDate() != null
                    ? agreement.getRegistrationDate().format(formatter)
                    : "Fecha no disponible";
            addTableCell(table, "Fecha de Registro:", formattedDate, labelFont, normalFont);

            document.add(table);

            // --- Footer ---
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph(
                    "Este documento es un comprobante automático generado por la plataforma Eco-Móstoles para el fomento de la simbiosis industrial.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
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
