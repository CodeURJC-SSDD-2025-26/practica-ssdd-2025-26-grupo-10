package es.urjc.ecomostoles.backend.service;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AcuerdoService {

    private final AcuerdoRepository acuerdoRepository;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public AcuerdoService(AcuerdoRepository acuerdoRepository, EmpresaService empresaService, OfertaService ofertaService) {
        this.acuerdoRepository = acuerdoRepository;
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
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
        return acuerdoRepository.count();
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

        oferta.setEstado(EstadoOferta.RESERVADA);
        ofertaService.guardar(oferta);

        acuerdo.setOferta(oferta);
        acuerdo.setMaterialIntercambiado(oferta.getTitulo() != null ? oferta.getTitulo() : "Material Acordado");
        acuerdo.setUnidad("kg/uds");
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
    public long contarPorEmpresaYEstado(Empresa empresa, String estado) {
        return acuerdoRepository.countByEmpresaAndEstado(empresa, estado);
    }
}
