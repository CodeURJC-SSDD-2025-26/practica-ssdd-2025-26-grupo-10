package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Acuerdo;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.AcuerdoRepository;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador encargado de la visualización y registro de nuevos acuerdos comerciales.
 */
@Controller
public class AcuerdoController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;
    private final AcuerdoRepository acuerdoRepository;

    public AcuerdoController(EmpresaRepository empresaRepository,
                             OfertaRepository ofertaRepository,
                             AcuerdoRepository acuerdoRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
        this.acuerdoRepository = acuerdoRepository;
    }

    /**
     * Muestra el formulario para registrar un nuevo acuerdo.
     * @param model Modelo para adjuntar recursos (empresa, ofertas).
     * @return Vista "crear_acuerdo"
     */
    @GetMapping("/acuerdo/nuevo")
    public String mostrarFormularioAcuerdo(Model model) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);

            // Obtener las ofertas de esta empresa para rellenar el selector
            List<Oferta> misOfertas = ofertaRepository.findByEmpresa(empresa);
            model.addAttribute("ofertas", misOfertas);

            return "crear_acuerdo";
        }

        return "redirect:/";
    }

    /**
     * Muestra el historial de acuerdos de la empresa activa.
     */
    @GetMapping("/acuerdos")
    public String mostrarMisAcuerdos(Model model) {
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            model.addAttribute("empresa", empresa);

            List<Acuerdo> misAcuerdos = acuerdoRepository.findByEmpresa(empresa);
            model.addAttribute("acuerdos", misAcuerdos);

            return "mis_acuerdos";
        }

        return "redirect:/";
    }

    /**
     * Procesa la creación de un nuevo acuerdo tras enviar el formulario.
     */
    @PostMapping("/acuerdo/nuevo")
    public String registrarAcuerdo(@RequestParam Long ofertaId, 
                                   @RequestParam String empresaDestino, 
                                   @RequestParam Double cantidadAcordada, 
                                   @RequestParam Double precioAcordado, 
                                   @RequestParam String fechaRecogida, 
                                   @RequestParam String estado, 
                                   @RequestParam(required = false) String notas) {
        
        // 1. Buscamos la oferta que se está vendiendo
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(ofertaId);
        
        if (ofertaOpt.isPresent()) {
            Oferta oferta = ofertaOpt.get();
            
            // 2. LÓGICA DE NEGOCIO: Cambiamos el estado de la oferta a Reservado
            oferta.setEstado("Reservado");
            ofertaRepository.save(oferta); // Guardamos el cambio de estado en BBDD
            
            // 3. Creamos el acuerdo (Asegúrate de adaptar esto a los constructores/setters de tu entidad Acuerdo)
            Optional<Empresa> destinoOpt = empresaRepository.findByNombreComercial(empresaDestino);
            
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
            
            // Buscamos la empresa activa y se la asignamos al acuerdo (Lógica de tu mock)
            Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto("contacto@metalesdelsur.es");
            empresaOpt.ifPresent(nuevoAcuerdo::setEmpresaOrigen);
            
            // Fallback en caso de no encontrar destino por mock estricto a las BD de entidades (ManyToOne required)
            if (!destinoOpt.isPresent() && empresaOpt.isPresent()) {
                nuevoAcuerdo.setEmpresaDestino(empresaOpt.get());
            }
            
            acuerdoRepository.save(nuevoAcuerdo);
        }
        
        return "redirect:/acuerdos";
    }
}
