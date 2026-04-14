package es.urjc.ecomostoles.backend;

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
    private final OfertaRepository ofertaRepository;

    public WebController(EmpresaRepository empresaRepository, OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository  = ofertaRepository;
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
        List<Oferta> recientes = ofertaRepository.findAll().stream()
                .filter(o -> "Activa".equals(o.getEstado()))
                .filter(o -> o.getFechaPublicacion() != null)   // evita NPE en sorted
                .sorted((a, b) -> b.getFechaPublicacion().compareTo(a.getFechaPublicacion()))
                .limit(3)
                .toList();
        model.addAttribute("ofertasRecientes", recientes);

        return "index";
    }
}
