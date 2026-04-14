package es.urjc.ecomostoles.backend;

import es.urjc.ecomostoles.backend.model.EstadoOferta;

import java.security.Principal;
import java.util.List;
import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final EmpresaRepository empresaRepository;
    private final es.urjc.ecomostoles.backend.service.OfertaService ofertaService;

    public WebController(EmpresaRepository empresaRepository, es.urjc.ecomostoles.backend.service.OfertaService ofertaService) {
        this.empresaRepository = empresaRepository;
        this.ofertaService  = ofertaService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        // ── Usuario logueado: inyectar empresa para el dropdown del navbar ──────
        if (principal != null) {
            Optional<Empresa> empresaOpt = empresaRepository.findByEmailContacto(principal.getName());
            model.addAttribute("empresa", empresaOpt.orElseGet(Empresa::new));
        } else {
            // Sin sesión: empresa vacía → {{empresa.id}} y {{empresa.nombreComercial}}
            // renderizan cadenas vacías en lugar de lanzar NullPointerException
            model.addAttribute("empresa", new Empresa());
        }

        // ── Últimas 3 ofertas activas (null-safe en fecha) ─────────────────────
        List<Oferta> recientes = ofertaService.obtenerRecientesActivas();
        model.addAttribute("ofertasRecientes", recientes);

        return "index";
    }
}
