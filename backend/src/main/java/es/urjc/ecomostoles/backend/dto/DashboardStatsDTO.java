package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Demanda;
import java.util.List;

/**
 * Data Transfer Object for Dashboard statistics.
 * Replaces the generic Map<String, Object> to provide strong typing and better maintainability.
 */
public class DashboardStatsDTO {

    private boolean esAdmin;
    private int totalOfertas;
    private int totalDemandas;
    private int acuerdosActivos;
    private List<Integer> chartData;
    private double materialReintroducido;
    private double impactoCO2;
    private List<Demanda> smartRecommendations;
    private boolean hasRecommendations;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(boolean esAdmin, int totalOfertas, int totalDemandas, int acuerdosActivos, 
                             List<Integer> chartData, double materialReintroducido, double impactoCO2, 
                             List<Demanda> smartRecommendations, boolean hasRecommendations) {
        this.esAdmin = esAdmin;
        this.totalOfertas = totalOfertas;
        this.totalDemandas = totalDemandas;
        this.acuerdosActivos = acuerdosActivos;
        this.chartData = chartData;
        this.materialReintroducido = materialReintroducido;
        this.impactoCO2 = impactoCO2;
        this.smartRecommendations = smartRecommendations;
        this.hasRecommendations = hasRecommendations;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public int getTotalOfertas() {
        return totalOfertas;
    }

    public void setTotalOfertas(int totalOfertas) {
        this.totalOfertas = totalOfertas;
    }

    public int getTotalDemandas() {
        return totalDemandas;
    }

    public void setTotalDemandas(int totalDemandas) {
        this.totalDemandas = totalDemandas;
    }

    public int getAcuerdosActivos() {
        return acuerdosActivos;
    }

    public void setAcuerdosActivos(int acuerdosActivos) {
        this.acuerdosActivos = acuerdosActivos;
    }

    public List<Integer> getChartData() {
        return chartData;
    }

    public void setChartData(List<Integer> chartData) {
        this.chartData = chartData;
    }

    public double getMaterialReintroducido() {
        return materialReintroducido;
    }

    public void setMaterialReintroducido(double materialReintroducido) {
        this.materialReintroducido = materialReintroducido;
    }

    public double getImpactoCO2() {
        return impactoCO2;
    }

    public void setImpactoCO2(double impactoCO2) {
        this.impactoCO2 = impactoCO2;
    }

    public List<Demanda> getSmartRecommendations() {
        return smartRecommendations;
    }

    public void setSmartRecommendations(List<Demanda> smartRecommendations) {
        this.smartRecommendations = smartRecommendations;
    }

    public boolean isHasRecommendations() {
        return hasRecommendations;
    }

    public void setHasRecommendations(boolean hasRecommendations) {
        this.hasRecommendations = hasRecommendations;
    }
}
