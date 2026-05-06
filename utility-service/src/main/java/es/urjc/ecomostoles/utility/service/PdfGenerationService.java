package es.urjc.ecomostoles.utility.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import es.urjc.ecomostoles.utility.dto.AgreementDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.awt.Color;

@Service
public class PdfGenerationService {

    public byte[] generateCertificate(AgreementDTO agreement) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph title = new Paragraph("Certificado de Impacto Ecológico", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            
            String company = agreement.getCompanyName() != null ? agreement.getCompanyName() : "Empresa Participante";
            String waste = agreement.getWasteCategory() != null ? agreement.getWasteCategory() : "Material Reciclado";
            Double qty = agreement.getQuantity() != null ? agreement.getQuantity() : 0.0;
            Double co2 = agreement.getCo2Saved() != null ? agreement.getCo2Saved() : 0.0;

            Paragraph p1 = new Paragraph(
                "La plataforma EcoMóstoles certifica que " + company + 
                " ha completado satisfactoriamente un intercambio de residuos sostenibles.",
                textFont
            );
            p1.setSpacingAfter(10);
            document.add(p1);

            Paragraph p2 = new Paragraph(
                "Detalles de la Operación:\n" +
                "- Material procesado: " + waste + "\n" +
                "- Cantidad movilizada: " + qty + " kg\n" +
                "- CO2 Ahorrado estimado: " + co2 + " kg CO2 eq.",
                textFont
            );
            document.add(p2);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de impacto: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }
}
