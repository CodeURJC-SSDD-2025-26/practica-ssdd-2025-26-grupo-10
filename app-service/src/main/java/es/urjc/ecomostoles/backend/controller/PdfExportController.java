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

/**
 * Reporting controller managing dynamic generation of binary PDF artifacts.
 * 
 * Utilizes the iText runtime framework to render compliant document structures directly from 
 * database entities. Streamlines reporting overheads bypassing intermediary files via in-memory 
 * byte array streams (ByteArrayOutputStream). Enforces tight data-leakage boundaries before compilation.
 */
@Controller
public class PdfExportController {

    private final AgreementService agreementService;
    private final CompanyService companyService;

    public PdfExportController(AgreementService agreementService, CompanyService companyService) {
        this.agreementService = agreementService;
        this.companyService = companyService;
    }

    /**
     * Executes dynamic PDF construction for an individual completed agreement.
     * 
     * Checks data boundaries to ensure the accessor physically partook in the negotiation, 
     * preventing lateral leakage of transaction price metrics.
     * 
     * @param id key routing to the specific materialized agreement.
     * @param principal security node resolving the requester's organizational map.
     * @return binary HTTP packet wrapping the PDF byte stream under attachment disposition.
     */
    @GetMapping("/acuerdo/{id}/pdf")
    public ResponseEntity<byte[]> generateAgreementPdf(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Agreement agreement = agreementService.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resource not found"));

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
                    "You do not have permission to access this resource");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Colors and Fonts ---
            Color corporateGreen = new Color(46, 125, 50);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, corporateGreen);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // --- Header Section ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{4f, 1.2f});

            // Title with background
            PdfPCell titleCell = new PdfPCell(new Phrase("RESGUARDO DE ACUERDO OFICIAL", titleFont));
            titleCell.setBackgroundColor(corporateGreen);
            titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPadding(12f);
            titleCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(titleCell);

            // Logo cell
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if (agreement.getOriginCompany() != null && agreement.getOriginCompany().getLogo() != null) {
                try {
                    Image logo = Image.getInstance(agreement.getOriginCompany().getLogo());
                    logo.scaleToFit(50, 50);
                    logoCell.setImage(logo);
                } catch (Exception ignored) {}
            }
            headerTable.addCell(logoCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // --- Detail Table ---
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);
            table.setWidths(new float[] { 1.8f, 3.2f });

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(java.util.Locale.of("es", "ES"));
            String formattedPrice = currencyFormat.format(agreement.getAgreedPrice());

            addProfessionalCell(table, "ID del Acuerdo", agreement.getId().toString(), headerFont, normalFont);
            addProfessionalCell(table, "Producto / Material", agreement.getExchangedMaterial(), headerFont, normalFont);
            addProfessionalCell(table, "Cantidad Pactada",
                    agreement.getQuantity() + " " + (agreement.getUnit() != null ? agreement.getUnit() : "uds"),
                    headerFont, normalFont);
            addProfessionalCell(table, "Importe Acordado", formattedPrice, headerFont, normalFont);

            String originName = agreement.getOriginCompany() != null ? agreement.getOriginCompany().getCommercialName()
                    : "No disponible";
            String destName = agreement.getDestinationCompany() != null
                    ? agreement.getDestinationCompany().getCommercialName()
                    : "No disponible";

            addProfessionalCell(table, "Empresa Proveedora", originName, headerFont, normalFont);
            addProfessionalCell(table, "Empresa Receptora", destName, headerFont, normalFont);
            
            // Spanish translation from Enum displayName
            addProfessionalCell(table, "Estado del Acuerdo", agreement.getStatus().getDisplayName(), headerFont, normalFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm", java.util.Locale.of("es", "ES"));
            String formattedDate = agreement.getRegistrationDate() != null
                    ? agreement.getRegistrationDate().format(formatter)
                    : "Fecha no disponible";
            addProfessionalCell(table, "Fecha de Emisión", formattedDate, headerFont, normalFont);

            document.add(table);

            // --- Footer ---
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(new Color(210, 210, 210));
            document.add(new Chunk(ls));
            
            Paragraph footer = new Paragraph(
                    "Este documento es un comprobante automático generado por la plataforma EcoMóstoles.\n" +
                    "Fomentando la economía circular y la simbiosis industrial.",
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15f);
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

    private void addProfessionalCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label.toUpperCase(), labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(new Color(230, 230, 230));
        labelCell.setPaddingTop(10f);
        labelCell.setPaddingBottom(10f);
        labelCell.setPaddingLeft(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(new Color(230, 230, 230));
        valueCell.setPaddingTop(10f);
        valueCell.setPaddingBottom(10f);
        table.addCell(valueCell);
    }
}
