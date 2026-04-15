package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import es.urjc.ecomostoles.backend.dto.RegistroDTO;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import java.util.Arrays;
import java.util.List;

@Controller
public class RegistroController {

    private static final Logger log = LoggerFactory.getLogger(RegistroController.class);

    private final EmpresaService empresaService;
    private final ConfiguracionService configuracionService;

    public RegistroController(EmpresaService empresaService, ConfiguracionService configuracionService) {
        this.empresaService = empresaService;
        this.configuracionService = configuracionService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        injectDynamicOptions(model);
        return "registro";
    }

    private void injectDynamicOptions(Model model) {
        // Fetch sectors dynamically from the platform settings
        String categoriasStr = configuracionService.obtenerValorAuto("listaCategorias");
        List<String> sectores = Arrays.asList(categoriasStr.split("\\r?\\n"));

        model.addAttribute("sectores", sectores);
        model.addAttribute("poligonos", List.of("Polígono Industrial Regordoño", "Polígono Industrial Las Nieves", "Polígono Arroyomolinos", "Polígono Industrial Norte", "Móstoles Tecnológico"));
    }

    @PostMapping("/registro")
    public String registrarEmpresa(@Valid @ModelAttribute("registroDTO") RegistroDTO dto, 
                                   BindingResult bindingResult, 
                                   Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", true);
            bindingResult.getFieldErrors().forEach(error -> 
                model.addAttribute("err_" + error.getField(), error.getDefaultMessage())
            );
            // Also global errors (like the password mismatch)
            bindingResult.getGlobalErrors().forEach(error ->
                model.addAttribute("err_global", error.getDefaultMessage())
            );
            injectDynamicOptions(model);
            return "registro";
        }

        try {
            // Mapping DTO to Entity
            Empresa nuevaEmpresa = new Empresa();
            nuevaEmpresa.setNombreComercial(dto.getNombreComercial());
            nuevaEmpresa.setCif(dto.getCif());
            nuevaEmpresa.setDireccion(dto.getDireccion());
            nuevaEmpresa.setSectorIndustrial(dto.getSector());
            nuevaEmpresa.setEmailContacto(dto.getEmailContacto());

            byte[] logoBytes = null;
            if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
                logoBytes = dto.getLogoFile().getBytes();
            }

            empresaService.registrarNuevaEmpresa(nuevaEmpresa, dto.getPassword(), logoBytes);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            model.addAttribute("errorMsg", e.getMessage());
            return "registro";
        } catch (Exception e) {
            log.error("⚠️ Error general en el registro de empresa", e);
            model.addAttribute("error", true);
            return "registro";
        }

        return "redirect:/login?registrado";
    }
}
