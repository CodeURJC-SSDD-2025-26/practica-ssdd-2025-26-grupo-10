package es.urjc.ecomostoles.backend.dto;

import java.util.List;

/**
 * Analytical payload aggregate for Dashboard monitoring.
 * 
 * Consolidates complex multi-dimensional database metrics (Offer counts, CO2 Impact, 
 * interaction workflows) into a single strongly-typed schema. Bypasses the overhead 
 * of generic Maps, guaranteeing compile-time type safety across the presentation layer.
 */
public class DashboardStatsDTO {

    private boolean isAdmin;
    private int totalOffers;
    private int totalDemands;
    private int activeAgreements;
    private List<Integer> chartData;
    private double reintroducedMaterial;
    private double co2Impact;
    private List<?> smartRecommendations;
    private boolean hasRecommendations;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(boolean isAdmin, int totalOffers, int totalDemands, int activeAgreements,
            List<Integer> chartData, double reintroducedMaterial, double co2Impact,
            List<?> smartRecommendations, boolean hasRecommendations) {
        this.isAdmin = isAdmin;
        this.totalOffers = totalOffers;
        this.totalDemands = totalDemands;
        this.activeAgreements = activeAgreements;
        this.chartData = chartData;
        this.reintroducedMaterial = reintroducedMaterial;
        this.co2Impact = co2Impact;
        this.smartRecommendations = smartRecommendations;
        this.hasRecommendations = hasRecommendations;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getTotalOffers() {
        return totalOffers;
    }

    public void setTotalOffers(int totalOffers) {
        this.totalOffers = totalOffers;
    }

    public int getTotalDemands() {
        return totalDemands;
    }

    public void setTotalDemands(int totalDemands) {
        this.totalDemands = totalDemands;
    }

    public int getActiveAgreements() {
        return activeAgreements;
    }

    public void setActiveAgreements(int activeAgreements) {
        this.activeAgreements = activeAgreements;
    }

    public List<Integer> getChartData() {
        return chartData;
    }

    public void setChartData(List<Integer> chartData) {
        this.chartData = chartData;
    }

    public double getReintroducedMaterial() {
        return reintroducedMaterial;
    }

    public void setReintroducedMaterial(double reintroducedMaterial) {
        this.reintroducedMaterial = reintroducedMaterial;
    }

    public double getCo2Impact() {
        return co2Impact;
    }

    public void setCo2Impact(double co2Impact) {
        this.co2Impact = co2Impact;
    }

    public List<?> getSmartRecommendations() {
        return smartRecommendations;
    }

    public void setSmartRecommendations(List<?> smartRecommendations) {
        this.smartRecommendations = smartRecommendations;
    }

    public boolean isHasRecommendations() {
        return hasRecommendations;
    }

    public void setHasRecommendations(boolean hasRecommendations) {
        this.hasRecommendations = hasRecommendations;
    }
}
