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

        // ── Parties ────────────────────────────────────────────────────────
        String  originCompanyName,
        String  destinationCompanyName,
        byte[]  originCompanyLogo,        // optional, nullable — used for PDF header logo

        // ── Material ───────────────────────────────────────────────────────
        String  exchangedMaterial,
        Double  quantity,
        String  unit,

        // ── Financials ─────────────────────────────────────────────────────
        Double  agreedPrice,
        Double  platformCommission,       // optional, may be null

        // ── Sustainability ─────────────────────────────────────────────────
        Double  co2Impact,

        // ── Dates ──────────────────────────────────────────────────────────
        LocalDate pickupDate,
        String    registrationDate,       // pre-formatted string (dd/MM/yyyy HH:mm)

        // ── Notes ──────────────────────────────────────────────────────────
        String  notes                     // optional, may be null
) {}
