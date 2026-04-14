package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador encargado de la visualización y registro de nuevos acuerdos comerciales.
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to AcuerdoService, EmpresaService and OfertaService.
 */
@Controller
public class AcuerdoController {

    private final AcuerdoService acuerdoService;
    private final EmpresaService empresaService;
    private final OfertaService ofertaService;

    public AcuerdoController(AcuerdoService acuerdoService,
                             EmpresaService empresaService,
                             OfertaService ofertaService) {
        this.acuerdoService = acuerdoService;
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
    }

    /** Muestra el formulario para registrar un nuevo acuerdo. */
    @GetMapping("/acuerdo/nuevo")
    public String mostrarFormularioAcuerdo(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            List<Oferta> misOfertas = ofertaService.obtenerPorEmpresa(empresa);
            model.addAttribute("ofertas", misOfertas);
            return "crear_acuerdo";
        }

        return "redirect:/";
    }

    /** Muestra el historial de acuerdos de la empresa activa. */
    @GetMapping("/acuerdos")
    public String mostrarMisAcuerdos(Model model, Principal principal) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);
            List<Acuerdo> misAcuerdos = acuerdoService.obtenerPorEmpresa(empresa);
            model.addAttribute("acuerdos", misAcuerdos);
            return "mis_acuerdos";
        }

        return "redirect:/";
    }

    /**
     * Procesa la creación de un nuevo acuerdo con validación manual de campos.
     * El formulario envía parámetros individuales (no un objeto Acuerdo completo),
     * por lo que la validación se realiza comprobando cada campo antes de persistir.
     */
    @PostMapping("/acuerdo/nuevo")
    public String registrarAcuerdo(@RequestParam(required = false) Long ofertaId,
                                   @RequestParam(required = false) String empresaDestino,
                                   @RequestParam(required = false) Double cantidadAcordada,
                                   @RequestParam(required = false) Double precioAcordado,
                                   @RequestParam(required = false) String fechaRecogida,
                                   @RequestParam(required = false) String estado,
                                   @RequestParam(required = false) String notas,
                                   Model model,
                                   Principal principal) {

        // ── Validación manual de campos obligatorios ───────────────────────────
        List<String> errores = new ArrayList<>();

        if (ofertaId == null) {
            errores.add("Debes seleccionar una oferta relacionada.");
        }
        if (empresaDestino == null || empresaDestino.isBlank()) {
            errores.add("El nombre de la empresa destino es obligatorio.");
        }
        if (cantidadAcordada == null) {
            errores.add("La cantidad acordada es obligatoria.");
        } else if (cantidadAcordada <= 0) {
            errores.add("La cantidad debe ser mayor que cero.");
        }
        if (precioAcordado == null) {
            errores.add("El precio acordado es obligatorio.");
        } else if (precioAcordado < 0) {
            errores.add("El precio no puede ser negativo.");
        }
        if (fechaRecogida == null || fechaRecogida.isBlank()) {
            errores.add("La fecha de recogida es obligatoria.");
        }
        if (estado == null || estado.isBlank()) {
            errores.add("El estado del acuerdo es obligatorio.");
        }

        // Si hay errores, volvemos al formulario mostrando los mensajes
        if (!errores.isEmpty()) {
            Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
            if (empresaOpt.isPresent()) {
                Empresa empresa = empresaOpt.get();
                model.addAttribute("empresa", empresa);
                model.addAttribute("ofertas", ofertaService.obtenerPorEmpresa(empresa));
            }
            model.addAttribute("errores", errores);
            return "crear_acuerdo";
        }

        // ── Procesamiento: todos los campos son válidos ─────────────────────────
        Optional<Oferta> ofertaOpt = ofertaService.buscarPorId(ofertaId);

        if (ofertaOpt.isPresent()) {
            Oferta oferta = ofertaOpt.get();

            // Lógica de negocio: marcar la oferta como Reservado
            oferta.setEstado("Reservado");
            ofertaService.guardar(oferta);

            Optional<Empresa> destinoOpt = empresaService.buscarPorNombreComercial(empresaDestino);

            Acuerdo nuevoAcuerdo = new Acuerdo();
            nuevoAcuerdo.setOferta(oferta);
            nuevoAcuerdo.setMaterialIntercambiado(oferta.getTitulo() != null ? oferta.getTitulo() : "Material Acordado");
            nuevoAcuerdo.setUnidad("kg/uds");

            if (destinoOpt.isPresent()) {
                nuevoAcuerdo.setEmpresaDestino(destinoOpt.get());
            }

            nuevoAcuerdo.setCantidad(cantidadAcordada);
            nuevoAcuerdo.setPrecioAcordado(precioAcordado);
            nuevoAcuerdo.setFechaRecogida(LocalDate.parse(fechaRecogida));
            nuevoAcuerdo.setEstado(estado);
            nuevoAcuerdo.setNotas(notas);
            nuevoAcuerdo.setFechaRegistro(LocalDateTime.now());

            Optional<Empresa> empresaOpt = empresaService.buscarPorEmail(principal.getName());
            empresaOpt.ifPresent(nuevoAcuerdo::setEmpresaOrigen);

            // Fallback: si no se encuentra la empresa destino, usamos la empresa origen
            if (destinoOpt.isEmpty() && empresaOpt.isPresent()) {
                nuevoAcuerdo.setEmpresaDestino(empresaOpt.get());
            }

            acuerdoService.guardar(nuevoAcuerdo);
        }

        return "redirect:/acuerdos";
    }
}
