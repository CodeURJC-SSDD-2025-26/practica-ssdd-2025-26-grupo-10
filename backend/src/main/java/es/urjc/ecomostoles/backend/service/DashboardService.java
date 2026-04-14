package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Demanda;
import es.urjc.ecomostoles.backend.model.Empresa;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @return A map containing all dashboard attributes.
     */
    public Map<String, Object> obtenerEstadisticas(Empresa empresa) {
        Map<String, Object> stats = new HashMap<>();
        
        boolean esAdmin = empresa.getRoles() != null && empresa.getRoles().contains("ADMIN");
        stats.put("esAdmin", esAdmin);

        if (esAdmin) {
            // ── Admin: Global KPIs ──────────────────────────────────────────
            stats.put("totalOfertas",  (int) ofertaService.contarTodas());
            stats.put("totalDemandas", (int) demandaService.contarTodas());
            stats.put("acuerdosActivos", (int) acuerdoService.contarTodos());
            
            stats.put("chartData", List.of(
                (int) ofertaService.contarTodas(),
                (int) demandaService.contarTodas(),
                (int) acuerdoService.contarTodos()
            ));
        } else {
            // ── Empresa: Personal KPIs ──────────────────────────────────────
            stats.put("totalOfertas",  (int) ofertaService.contarPorEmpresa(empresa));
            stats.put("totalDemandas", (int) demandaService.contarPorEmpresa(empresa));
            stats.put("acuerdosActivos", (int) acuerdoService.contarPorEmpresa(empresa));
            stats.put("impactoCO2", acuerdoService.sumarMaterialReintroducido(empresa));

            // Smart Matching
            List<Demanda> recommendedDemandas = demandaService.obtenerSmartRecommendations(empresa);
            stats.put("smartRecommendations", recommendedDemandas);
            stats.put("hasRecommendations", !recommendedDemandas.isEmpty());
            
            stats.put("chartData", List.of(
                (int) ofertaService.contarPorEmpresa(empresa),
                (int) demandaService.contarPorEmpresa(empresa),
                (int) acuerdoService.contarPorEmpresa(empresa)
            ));
        }
        
        return stats;
    }
}
