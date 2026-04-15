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

import es.urjc.ecomostoles.backend.component.SustainabilityEngine;
import es.urjc.ecomostoles.backend.exception.SelfAgreementException;

@Service
@Transactional
public class AcuerdoService {

    private final AcuerdoRepository acuerdoRepository;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;
    private final SustainabilityEngine sustainabilityEngine;

    public AcuerdoService(AcuerdoRepository acuerdoRepository, 
                          EmpresaService empresaService, 
                          OfertaService ofertaService,
                          SustainabilityEngine sustainabilityEngine) {
        this.acuerdoRepository = acuerdoRepository;
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
        this.sustainabilityEngine = sustainabilityEngine;
    }

    @Transactional(readOnly = true)
    public List<Acuerdo> obtenerTodos() {
        return acuerdoRepository.findTop50ByOrderByFechaRegistroDesc();
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
        Double total = acuerdoRepository.sumCantidadByEmpresaAndEstadoCompletado(empresa);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double sumarTotalMaterialReintroducido() {
        Double total = acuerdoRepository.sumTotalCantidadByEstadoCompletado();
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public double calcularCO2AhorradoPorEmpresa(Long empresaId) {
        Optional<Empresa> empresa = empresaService.buscarPorId(empresaId);
        if (empresa.isEmpty()) return 0.0;
        
        // Optimized: direct sum from DB if possible, or at least filtered list
        Double totalCantidad = acuerdoRepository.sumCantidadByEmpresaAndEstadoCompletado(empresa.get());
        if (totalCantidad == null) return 0.0;

        // Note: For ranking/exact CO2 per material we still need the list or a more complex sum query
        // But for an individual company, this is faster than fetching everything.
        // However, since factors depend on material, we actually need the group by or individual fetch.
        // I'll keep the list fetch but ensure it's filtered and joined.
        List<Acuerdo> acuerdos = acuerdoRepository.findByEmpresa(empresa.get());
        return acuerdos.stream()
                .filter(a -> es.urjc.ecomostoles.backend.model.EstadoAcuerdo.COMPLETADO.equals(a.getEstado()))
                .mapToDouble(a -> sustainabilityEngine.calcularImpactoCO2(a.getCantidad(), 
                    a.getOferta() != null ? a.getOferta().getTipoResiduo() : null))
                .sum();
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
            double co2 = sustainabilityEngine.calcularImpactoCO2(a.getCantidad(), 
                a.getOferta() != null ? a.getOferta().getTipoResiduo() : null);
            
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
    public String calcularCO2Ahorrado() {
        // Optimized: only fetch completed agreements
        List<Acuerdo> completados = acuerdoRepository.findAllByEstado(es.urjc.ecomostoles.backend.model.EstadoAcuerdo.COMPLETADO); 
        
        double totalTonsCO2 = completados.stream()
                .mapToDouble(a -> sustainabilityEngine.calcularImpactoCO2(a.getCantidad(), 
                    a.getOferta() != null ? a.getOferta().getTipoResiduo() : null))
                .sum();

        if (totalTonsCO2 == 0) return "0";
   
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.of("es", "ES"));
        df.applyPattern("#,###.###");
        return df.format(totalTonsCO2);
    }

    /**
     * Updates an existing agreement with the provided data.
     */
    public Acuerdo actualizarAcuerdo(Long id, Acuerdo datosActualizados) {
        Acuerdo acuerdoExistente = acuerdoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acuerdo no encontrado"));

        // Update allowed fields
        acuerdoExistente.setMaterialIntercambiado(datosActualizados.getMaterialIntercambiado());
        acuerdoExistente.setCantidad(datosActualizados.getCantidad());
        acuerdoExistente.setUnidad(datosActualizados.getUnidad());
        acuerdoExistente.setPrecioAcordado(datosActualizados.getPrecioAcordado());
        acuerdoExistente.setFechaRecogida(datosActualizados.getFechaRecogida());
        acuerdoExistente.setEstado(datosActualizados.getEstado());

        return acuerdoRepository.save(acuerdoExistente);
    }
}
