package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.dto.DashboardStatsDTO;
import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service to handle business logic for the Dashboard.
 * Extracts logic from the controller to follow the Controller-Service pattern.
 */
@Service
public class DashboardService {

    private final OfertaService  ofertaService;
    private final DemandaService demandaService;
    private final AcuerdoService acuerdoService;

    public DashboardService(OfertaService ofertaService, 
                            DemandaService demandaService, 
                            AcuerdoService acuerdoService) {
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
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
            stats.setTotalOfertas((int) ofertaService.contarTodas());
            stats.setTotalDemandas((int) demandaService.contarTodas());
            stats.setAcuerdosActivos((int) acuerdoService.contarTodos());
            
            stats.setChartData(List.of(
                (int) ofertaService.contarTodas(),
                (int) demandaService.contarTodas(),
                (int) acuerdoService.contarTodos()
            ));
        } else {
            // ── Empresa: Personal KPIs ──────────────────────────────────────
            stats.setTotalOfertas((int) ofertaService.contarPorEmpresa(empresa));
            stats.setTotalDemandas((int) demandaService.contarPorEmpresa(empresa));
            stats.setAcuerdosActivos((int) acuerdoService.contarPorEmpresa(empresa));
            
            double reintroducido = acuerdoService.sumarMaterialReintroducido(empresa);
            stats.setMaterialReintroducido(reintroducido);
            stats.setImpactoCO2(reintroducido * 0.45);

            // Smart Matching
            List<Demanda> recommendedDemandas = demandaService.obtenerSmartRecommendations(empresa);
            stats.setSmartRecommendations(recommendedDemandas);
            stats.setHasRecommendations(!recommendedDemandas.isEmpty());
            
            stats.setChartData(List.of(
                (int) ofertaService.contarPorEmpresa(empresa),
                (int) demandaService.contarPorEmpresa(empresa),
                (int) acuerdoService.contarPorEmpresa(empresa)
            ));
        }
        
        return stats;
    }
}
