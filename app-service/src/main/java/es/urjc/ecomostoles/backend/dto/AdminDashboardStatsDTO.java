package es.urjc.ecomostoles.backend.dto;

/**
 * Analytical payload aggregate for Administrative Dashboard monitoring.
 * 
 * Consolidates platform-wide KPIs including financial and operational metrics.
 */
public record AdminDashboardStatsDTO(
    long totalUsers,
    long totalOffers,
    long totalDemands,
    long totalAgreements,
    long totalPending,
    long totalReported,
    long totalCompleted,
    String co2Tons,
    String totalCommission
) {}
