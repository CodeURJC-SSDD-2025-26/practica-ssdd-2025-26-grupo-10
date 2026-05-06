package es.urjc.ecomostoles.utility.dto;

import java.time.LocalDate;

/**
 * Inbound payload DTO for PDF agreement report generation.
 *
 * All fields are plain scalars (String, Double, LocalDate) — no JPA entities
 * or references to app-service internals. The caller (app-service) is
 * responsible for projecting its AgreementDTO onto this record before
 * dispatching the HTTP request to utility-service.
 *
 * Field naming mirrors the PDF layout sections in PdfGeneratorService to keep
 * the mapping self-documenting.
 */
public record AgreementReportRequest(
        // ── Identity ───────────────────────────────────────────────────────
        Long    agreementId,
        String  status,

        // ── Parties (Detailed) ─────────────────────────────────────────────
        String  originCompanyName,
        String  originCompanyTaxId,
        String  originCompanyAddress,
        String  originCompanyPhone,
        String  originCompanySector,
        byte[]  originCompanyLogo,

        String  destinationCompanyName,
        String  destinationCompanyTaxId,
        String  destinationCompanyAddress,
        String  destinationCompanyPhone,
        String  destinationCompanySector,

        // ── Material ───────────────────────────────────────────────────────
        String  exchangedMaterial,
        Double  quantity,
        String  unit,

        // ── Financials ─────────────────────────────────────────────────────
        Double  agreedPrice,
        Double  platformCommission,

        // ── Sustainability ─────────────────────────────────────────────────
        Double  co2Impact,

        // ── Dates ──────────────────────────────────────────────────────────
        LocalDate pickupDate,
        String    registrationDate,

        // ── Notes ──────────────────────────────────────────────────────────
        String  notes,

        // ── Brand Assets ───────────────────────────────────────────────────
        byte[]  platformSeal              // Official platform seal/stamp
) {}
