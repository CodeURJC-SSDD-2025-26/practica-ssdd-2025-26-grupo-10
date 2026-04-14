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

/**
 * Service layer for Acuerdo (Agreement) business logic.
 * Intermediary between controllers and the AcuerdoRepository, following
 * the Controller > Service > Repository architecture pattern.
 */
@Service
public class AcuerdoService {

    private final AcuerdoRepository acuerdoRepository;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public AcuerdoService(AcuerdoRepository acuerdoRepository, EmpresaService empresaService, OfertaService ofertaService) {
        this.acuerdoRepository = acuerdoRepository;
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
    }

    /** Returns all agreements in the system. */
    public List<Acuerdo> obtenerTodos() {
        return acuerdoRepository.findAll();
    }

    /**
     * Returns all agreements where the company is involved as origin OR destination.
     * Used by the Dashboard KPI counter and Mis Acuerdos view.
     */
    public List<Acuerdo> obtenerPorEmpresa(Empresa empresa) {
        return acuerdoRepository.findByEmpresa(empresa);
    }

    /** Returns agreements where the company is the originator (source). */
    public List<Acuerdo> obtenerPorEmpresaOrigen(Empresa empresa) {
        return acuerdoRepository.findByEmpresaOrigen(empresa);
    }

    /** Returns agreements where the company is the recipient (destination). */
    public List<Acuerdo> obtenerPorEmpresaDestino(Empresa empresa) {
        return acuerdoRepository.findByEmpresaDestino(empresa);
    }

    /** Returns an agreement by its ID, or empty if not found. */
    public Optional<Acuerdo> buscarPorId(Long id) {
        return acuerdoRepository.findById(id);
    }

    /** Persists a new or updated agreement. */
    public Acuerdo guardar(Acuerdo acuerdo) {
        return acuerdoRepository.save(acuerdo);
    }

    /** Deletes an agreement by its ID. */
    public void eliminar(Long id) {
        acuerdoRepository.deleteById(id);
    }

    /** Returns the total count of agreements. */
    public long contarTodos() {
        return acuerdoRepository.count();
    }

    /** Registra un nuevo acuerdo y reserva la oferta, todo de forma transaccional. */
    @Transactional
    public void registrarNuevoAcuerdo(Acuerdo acuerdo, String emailUsuario, Long ofertaId, Long empresaDestinoId) {
        Oferta oferta = ofertaService.buscarPorId(ofertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada"));

        Empresa origen = empresaService.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Business logic: mark the offer as Reserved
        oferta.setEstado(EstadoOferta.RESERVADA);
        ofertaService.guardar(oferta);

        acuerdo.setOferta(oferta);
        acuerdo.setMaterialIntercambiado(oferta.getTitulo() != null ? oferta.getTitulo() : "Material Acordado");
        acuerdo.setUnidad("kg/uds");
        acuerdo.setFechaRegistro(java.time.LocalDateTime.now());
        acuerdo.setEmpresaOrigen(origen);

        if (empresaDestinoId != null) {
            Empresa destino = empresaService.buscarPorId(empresaDestinoId).orElse(origen);
            acuerdo.setEmpresaDestino(destino);
        } else {
            acuerdo.setEmpresaDestino(origen);
        }

        acuerdoRepository.save(acuerdo);
    }
}
