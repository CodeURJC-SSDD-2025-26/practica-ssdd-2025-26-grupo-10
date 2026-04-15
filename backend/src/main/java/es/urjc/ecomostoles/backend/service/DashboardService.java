package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.DashboardStatsDTO;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.stereotype.Service;
import java.util.List;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;

/**
 * Service to handle business logic for the Dashboard.
 * Extracts logic from the controller to follow the Controller-Service pattern.
 */
@Service
public class DashboardService {

    private final OfertaService  ofertaService;
    private final DemandaService demandaService;
    private final AcuerdoService acuerdoService;
    private final SustainabilityEngine sustainabilityEngine;

    public DashboardService(OfertaService ofertaService, 
                            DemandaService demandaService, 
                            AcuerdoService acuerdoService,
                            SustainabilityEngine sustainabilityEngine) {
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
        this.sustainabilityEngine = sustainabilityEngine;
    }

    /**
     * Calculates all KPIs, charts data and recommendations for a given company (or admin).
     * 
     * @param empresa The company for which stats are calculated.
     * @return A DTO containing all dashboard attributes.
     */
    public DashboardStatsDTO obtenerEstadisticas(Empresa empresa) {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        boolean esAdmin = empresa.getRoles() != null && empresa.getRoles().contains("ADMIN");
        stats.setEsAdmin(esAdmin);

        if (esAdmin) {
            // ── Admin: Global KPIs ──────────────────────────────────────────
            int totalOfertas = (int) ofertaService.contarTodas();
            int totalDemandas = (int) demandaService.contarTodas();
            int acuerdosActivos = (int) acuerdoService.contarTodos();

            stats.setTotalOfertas(totalOfertas);
            stats.setTotalDemandas(totalDemandas);
            stats.setAcuerdosActivos(acuerdosActivos);
            
            stats.setChartData(List.of(totalOfertas, totalDemandas, acuerdosActivos));
            
            // Admin Global Impact Stats
            stats.setMaterialReintroducido(acuerdoService.sumarTotalMaterialReintroducido());
            stats.setImpactoCO2(Double.parseDouble(acuerdoService.calcularCO2Ahorrado().replace(".", "").replace(",", ".")));
        } else {
            // ── Empresa: Personal KPIs ──────────────────────────────────────
            int misOfertas = (int) ofertaService.contarPorEmpresa(empresa);
            int misDemandas = (int) demandaService.contarPorEmpresa(empresa);
            int misAcuerdos = (int) acuerdoService.contarPorEmpresa(empresa);

            stats.setTotalOfertas(misOfertas);
            stats.setTotalDemandas(misDemandas);
            stats.setAcuerdosActivos(misAcuerdos);
            
            double reintroducido = acuerdoService.sumarMaterialReintroducido(empresa);
            stats.setMaterialReintroducido(reintroducido);
            stats.setImpactoCO2(acuerdoService.calcularCO2AhorradoPorEmpresa(empresa.getId()));

            // Smart Matching
            List<Demanda> recommendedDemandas = demandaService.obtenerSmartRecommendations(empresa);
            stats.setSmartRecommendations(recommendedDemandas);
            stats.setHasRecommendations(!recommendedDemandas.isEmpty());
            
            stats.setChartData(List.of(misOfertas, misDemandas, misAcuerdos));
        }
        
        return stats;
    }
}
