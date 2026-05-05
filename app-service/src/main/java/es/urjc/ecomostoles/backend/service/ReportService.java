package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.model.Company;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Report generation service for administrative data exports.
 *
 * PDF generation has been fully migrated to utility-service and is no longer
 * a responsibility of this class. This service now exclusively handles
 * lightweight text-based exports (CSV) that do not require an external library.
 *
 * If you need to add new report types (Excel, JSON exports, etc.), add them
 * here and keep PDF-specific work in utility-service.
 */
@Service
public class ReportService {

    /**
     * Generates a CSV report of registered companies.
     *
     * @param companies list of companies to include.
     * @return UTF-8 encoded byte array of the CSV content.
     */
    public byte[] generateUsersCsv(List<Company> companies) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Nombre Comercial;Email;Rol\n");

        for (Company comp : companies) {
            sb.append(String.format("%d;\"%s\";\"%s\";\"%s\"\n",
                    comp.getId(),
                    comp.getCommercialName(),
                    comp.getContactEmail(),
                    comp.getRole()));
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generates a CSV report of offers.
     *
     * @param offers list of offer summaries.
     * @return UTF-8 encoded byte array of the CSV content.
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
