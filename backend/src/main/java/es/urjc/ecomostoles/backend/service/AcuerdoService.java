package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;

@Service
@Transactional
public class AcuerdoService {

    private final AcuerdoRepository acuerdoRepository;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;
    private final SustainabilityEngine sustainabilityEngine;
    private final ConfiguracionService configuracionService;

    public AcuerdoService(AcuerdoRepository acuerdoRepository, 
                          EmpresaService empresaService, 
                          OfertaService ofertaService,
                          SustainabilityEngine sustainabilityEngine,
                          ConfiguracionService configuracionService) {
        this.acuerdoRepository = acuerdoRepository;
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
        this.sustainabilityEngine = sustainabilityEngine;
        this.configuracionService = configuracionService;
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> obtenerTodos() {
        return acuerdoRepository.findTop50ByOrderByFechaRegistroDesc();
    }

    @Transactional(readOnly = true)
    public Page<Acuerdo> obtenerTodosPaginados(Pageable pageable) {
        return acuerdoRepository.findAllPaginated(pageable);
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> obtenerPorEmpresa(Empresa empresa) {
        return acuerdoRepository.findByEmpresa(empresa);
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> obtenerPorEmpresaOrigen(Empresa empresa) {
        return acuerdoRepository.findByEmpresaOrigen(empresa);
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> obtenerPorEmpresaDestino(Empresa empresa) {
        return acuerdoRepository.findByEmpresaDestino(empresa);
    }

    @Transactional(readOnly = true)
    public Optional<Acuerdo> buscarPorId(Long id) {
        return acuerdoRepository.findById(id);
    }

    public Acuerdo guardar(Acuerdo acuerdo) {
        return acuerdoRepository.save(acuerdo);
    }

    public void eliminar(Long id) {
        acuerdoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long contarTodos() {
        return contarTodos(null);
    }

    @Transactional(readOnly = true)
    public long contarTodos(String filtro) {
        if ("semana".equals(filtro)) {
            return acuerdoRepository.countByFechaRegistroAfter(LocalDateTime.now().minusDays(7));
        }
        return acuerdoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(es.urjc.ecomostoles.backend.model.EstadoAcuerdo estado) {
        return contarPorEstado(estado, null);
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(es.urjc.ecomostoles.backend.model.EstadoAcuerdo estado, String filtro) {
        if ("semana".equals(filtro)) {
            return acuerdoRepository.countByEstadoAndFechaRegistroAfter(estado, LocalDateTime.now().minusDays(7));
        }
        return acuerdoRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public long contarPorEmpresa(Empresa empresa) {
        return acuerdoRepository.countByEmpresa(empresa);
    }

    public void registrarNuevoAcuerdo(Acuerdo acuerdo, String emailUsuario, Long ofertaId, Long empresaDestinoId) {
        Oferta oferta = ofertaService.buscarPorId(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada"));

        Empresa origen = empresaService.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (origen.getId().equals(empresaDestinoId)) {
            throw new SelfAgreementException("El origen y el destino no pueden ser la misma entidad.");
        }

        oferta.setEstado(EstadoOferta.RESERVADA);
        ofertaService.guardar(oferta);

        acuerdo.setOferta(oferta);
        acuerdo.setMaterialIntercambiado(oferta.getTitulo() != null ? oferta.getTitulo() : "Material Acordado");
        acuerdo.setFechaRegistro(LocalDateTime.now());
        acuerdo.setEmpresaOrigen(origen);

        // --- CALCULO DE IMPACTO CO2 ---
        double co2 = sustainabilityEngine.calcularImpactoCO2(acuerdo.getCantidad(), 
                        oferta.getTipoResiduo());
        acuerdo.setImpactoCO2(co2);
        // ------------------------------

        // --- CALCULO DE COMISION ---
        String comisionStr = configuracionService.obtenerValorAuto("comisionPlataforma");
        double porcentaje = 0.0;
        try {
            porcentaje = Double.parseDouble(comisionStr);
        } catch (NumberFormatException e) {
            // Log fallback or handle error
        }

        if (acuerdo.getPrecioAcordado() != null) {
            double beneficio = acuerdo.getPrecioAcordado() * (porcentaje / 100.0);
            beneficio = Math.round(beneficio * 100.0) / 100.0;
            acuerdo.setBeneficioPlataforma(beneficio);
        }
        // ---------------------------

        if (empresaDestinoId != null) {
            Empresa destino = empresaService.buscarPorId(empresaDestinoId).orElse(origen);
            acuerdo.setEmpresaDestino(destino);
        } else {
            acuerdo.setEmpresaDestino(origen);
        }

        acuerdoRepository.save(acuerdo);
    }

    @Transactional(readOnly = true)
    public double sumarMaterialReintroducido(Empresa empresa) {
        Double total = acuerdoRepository.sumCantidadByEmpresaAndEstado(empresa, EstadoAcuerdo.COMPLETADO);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double sumarTotalMaterialReintroducido() {
        Double total = acuerdoRepository.sumTotalCantidadByEstado(EstadoAcuerdo.COMPLETADO);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double calcularCO2AhorradoPorEmpresa(Long empresaId) {
        Optional<Empresa> empresa = empresaService.buscarPorId(empresaId);
        if (empresa.isEmpty()) return 0.0;
        
        Double total = acuerdoRepository.sumImpactoCO2ByEmpresaCompletado(empresa.get());
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public long contarPorEmpresaYEstado(Empresa empresa, EstadoAcuerdo estado) {
        return acuerdoRepository.countByEmpresaAndEstado(empresa, estado);
    }
 
    @Transactional(readOnly = true)
    public Map<Long, Double> obtenerRankingCO2() {
        // Correctly filter by state in the query instead of in memory
        List<Acuerdo> completados = acuerdoRepository.findAllByEstado(es.urjc.ecomostoles.backend.model.EstadoAcuerdo.COMPLETADO);
        
        Map<Long, Double> ranking = new HashMap<>();
        for (Acuerdo a : completados) {
            double co2 = (a.getImpactoCO2() != null) ? a.getImpactoCO2() : 0.0;
            
            if (a.getEmpresaOrigen() != null) {
                ranking.merge(a.getEmpresaOrigen().getId(), co2, Double::sum);
            }
            if (a.getEmpresaDestino() != null) {
                ranking.merge(a.getEmpresaDestino().getId(), co2, Double::sum);
            }
        }
        return ranking;
    }

    @Transactional(readOnly = true)
    public Double obtenerImpactoCO2Crudo() {
        Double total = acuerdoRepository.sumTotalImpactoCO2Completado();
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public String calcularCO2Ahorrado() {
        Double total = acuerdoRepository.sumTotalImpactoCO2Completado();
        
        if (total == null || total == 0) return "0";
   
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.of("es", "ES"));
        df.applyPattern("#,###.###");
        return df.format(total);
    }

    /**
     * Updates an existing agreement with the provided data.
     */
    public Acuerdo actualizarAcuerdo(Long id, Acuerdo datosActualizados) {
        Acuerdo acuerdoExistente = acuerdoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        // If transitioning to COMPLETADO, freeze the CO2 impact
        if (EstadoAcuerdo.COMPLETADO.equals(datosActualizados.getEstado()) && 
           !EstadoAcuerdo.COMPLETADO.equals(acuerdoExistente.getEstado())) {
            
            double co2 = sustainabilityEngine.calcularImpactoCO2(datosActualizados.getCantidad(), 
                            acuerdoExistente.getOferta() != null ? acuerdoExistente.getOferta().getTipoResiduo() : null);
            acuerdoExistente.setImpactoCO2(co2);
        }

        // Update allowed fields
        acuerdoExistente.setMaterialIntercambiado(datosActualizados.getMaterialIntercambiado());
        acuerdoExistente.setCantidad(datosActualizados.getCantidad());
        acuerdoExistente.setUnidad(datosActualizados.getUnidad());
        acuerdoExistente.setFechaRecogida(datosActualizados.getFechaRecogida());
        acuerdoExistente.setEstado(datosActualizados.getEstado());

        // Recalculate profit if price changed
        if (datosActualizados.getPrecioAcordado() != null && 
           !datosActualizados.getPrecioAcordado().equals(acuerdoExistente.getPrecioAcordado())) {
            
            acuerdoExistente.setPrecioAcordado(datosActualizados.getPrecioAcordado());
            
            String comisionStr = configuracionService.obtenerValorAuto("comisionPlataforma");
            double porcentaje = 0.0;
            try {
                porcentaje = Double.parseDouble(comisionStr);
            } catch (NumberFormatException e) {
                // Ignore or log
            }
            
            double beneficio = acuerdoExistente.getPrecioAcordado() * (porcentaje / 100.0);
            beneficio = Math.round(beneficio * 100.0) / 100.0;
            acuerdoExistente.setBeneficioPlataforma(beneficio);
        } else {
            acuerdoExistente.setPrecioAcordado(datosActualizados.getPrecioAcordado());
        }

        return acuerdoRepository.save(acuerdoExistente);
    }
}
