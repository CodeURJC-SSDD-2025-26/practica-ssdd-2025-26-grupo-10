package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import es.urjc.ecomostoles.backend.service.DemandaService;
import es.urjc.ecomostoles.backend.service.AcuerdoService;
import es.urjc.ecomostoles.backend.model.EstadoAcuerdo;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import es.urjc.ecomostoles.backend.dto.EmpresaDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.service.ConfiguracionService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import java.awt.Color;

/**
 * Administration panel controller.
 *
 * All paths are under /admin/** and protected by @PreAuthorize("hasRole('ADMIN')")
 * (additional reinforcement over the SecurityConfig rule).
 *
 * Used views (existing):
 *   admin_panel, admin_usuarios, admin_ofertas, admin_reportes, admin_configuracion
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmpresaService       empresaService;
    private final OfertaService        ofertaService;
    private final DemandaService       demandaService;
    private final AcuerdoService       acuerdoService;
    private final ConfiguracionService configuracionService;

    public AdminController(EmpresaService empresaService,
                           OfertaService ofertaService,
                           DemandaService demandaService,
                           AcuerdoService acuerdoService,
                           ConfiguracionService configuracionService) {
        this.empresaService = empresaService;
        this.ofertaService  = ofertaService;
        this.demandaService = demandaService;
        this.acuerdoService = acuerdoService;
        this.configuracionService = configuracionService;
    }

    // ... (keep private methods)

    // ── Helper: puts company + global KPIs in the model ─────────────────────
    private void addCommonAttributes(Model model, Principal principal) {
        addCommonAttributes(model, principal, null);
    }

    private void addCommonAttributes(Model model, Principal principal, String filtro) {
        if (principal != null) {
            Optional<Empresa> opt = empresaService.buscarPorEmail(principal.getName());
            opt.ifPresent(e -> model.addAttribute("empresa", e));
        }
        // Global platform KPIs
        model.addAttribute("totalUsuarios",  empresaService.contarTodas());
        model.addAttribute("totalOfertas",   ofertaService.contarTodas());
        model.addAttribute("totalDemandas",  demandaService.contarTodas());
        model.addAttribute("totalAcuerdos",  acuerdoService.contarTodos(filtro));
 
        // Admin-Specific Stats (Real DB counts)
        model.addAttribute("totalPendientes",  acuerdoService.contarPorEstado(EstadoAcuerdo.PENDIENTE, filtro));
        model.addAttribute("totalDenunciadas", ofertaService.contarPorEstado(EstadoOferta.DENUNCIADA));
        model.addAttribute("totalCompletadas", acuerdoService.contarPorEstado(EstadoAcuerdo.COMPLETADO, filtro));
        model.addAttribute("toneladasCO2", acuerdoService.calcularCO2Ahorrado());
    }

    // ── GET /admin  →  redirects to /admin/panel ───────────────────────────
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/panel";
    }

    // ── GET /admin/panel ───────────────────────────────────────────────────────
    @GetMapping("/panel")
    public String panel(Model model, Principal principal, @RequestParam(required = false) String filtro) {
        addCommonAttributes(model, principal, filtro);
        model.addAttribute("navPanel", true);
        model.addAttribute("filtroActual", filtro);
        return "admin_panel";
    }

    // ── GET /admin/usuarios ────────────────────────────────────────────────────
    @GetMapping("/usuarios")
    public String usuarios(Model model, Principal principal, @RequestParam(required = false) String search) {
        addCommonAttributes(model, principal);
        model.addAttribute("navUsuarios", true);
        
        if (search != null && !search.isEmpty()) {
            model.addAttribute("empresas", empresaService.filtrarEmpresas(search));
        } else {
            // Limited list of registered companies for the table (Top 50)
            model.addAttribute("empresas", empresaService.obtenerTodas());
        }
        
        return "admin_usuarios";
    }

    // ── GET /admin/ofertas ─────────────────────────────────────────────────────
    @GetMapping("/ofertas")
    public String ofertas(Model model, Principal principal, @RequestParam(required = false) String estado) {
        addCommonAttributes(model, principal);
        model.addAttribute("navOfertas", true);
        
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoOferta enumEstado = EstadoOferta.valueOf(estado.toUpperCase());
                model.addAttribute("todasOfertas", ofertaService.obtenerPorEstado(enumEstado));
                model.addAttribute("filtroSeleccionado", estado);
            } catch (IllegalArgumentException e) {
                model.addAttribute("todasOfertas", ofertaService.obtenerTodas());
            }
        } else {
            model.addAttribute("todasOfertas", ofertaService.obtenerTodas());
        }
        
        return "admin_ofertas";
    }

    // ── GET /admin/reportes ────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navReportes", true);

        // Fetch companies and enrich with CO2 stats
        List<EmpresaDTO> empresas = empresaService.obtenerTodas().stream().map(e -> {
            EmpresaDTO dto = new EmpresaDTO(e);
            dto.setCo2Ahorrado(acuerdoService.calcularCO2AhorradoPorEmpresa(e.getId()));
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("empresas", empresas);
        return "admin_reportes";
    }

    // ── GET /admin/configuracion ───────────────────────────────────────────────
    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navConfiguracion", true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("modoMantenimiento", "true".equals(configuracionService.obtenerValorConfiguracion("modoMantenimiento", "false")));
        configMap.put("emailContacto", configuracionService.obtenerValorConfiguracion("emailContacto", "info@ecomostoles.com"));
        configMap.put("comisionPlataforma", configuracionService.obtenerValorConfiguracion("comisionPlataforma", "2.5"));
        configMap.put("listaCategorias", configuracionService.obtenerValorConfiguracion("listaCategorias", ""));

        model.addAttribute("config", configMap);
        return "admin_configuracion";
    }
 
    /**
     * Deletes an offer from the administrative panel.
     */
    @PostMapping("/ofertas/{id}/eliminar")
    public String eliminarOferta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ofertaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "La oferta ha sido eliminada correctamente por el administrador.");
        return "redirect:/admin/ofertas";
    }
 
    /**
     * Saves global platform configuration.
     */
    @PostMapping("/configuracion")
    public String guardarConfiguracion(@RequestParam(required = false) String modoMantenimiento,
                                       @RequestParam(required = false) String emailContacto,
                                       @RequestParam(required = false) Double comisionPlataforma,
                                       @RequestParam(required = false) String listaCategorias,
                                       RedirectAttributes redirectAttributes) {
        
        configuracionService.guardarOActualizarConfiguracion("modoMantenimiento", modoMantenimiento != null ? "true" : "false");
        configuracionService.guardarOActualizarConfiguracion("emailContacto", emailContacto);
        configuracionService.guardarOActualizarConfiguracion("comisionPlataforma", String.valueOf(comisionPlataforma));
        configuracionService.guardarOActualizarConfiguracion("listaCategorias", listaCategorias);

        redirectAttributes.addFlashAttribute("mensaje", "La configuración de la plataforma se ha actualizado correctamente en la base de datos.");
        return "redirect:/admin/configuracion";
    }

    /**
     * Exports a PDF report (Mock generation for REST flow compliance).
     */
    @GetMapping("/exportar/pdf")
    public void exportarPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ecomostoles.pdf\"");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Title
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(new Color(25, 135, 84)); // EcoMostoles Green

        Paragraph p = new Paragraph("Reporte Administrativo: EcoMostoles", fontTitle);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        document.add(new Paragraph(" ")); // Spacer

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.5f, 3.5f, 2.0f});
        table.setSpacingBefore(10);

        // Header Cell Helper
        writeTableHeader(table);

        // Data
        List<Empresa> empresas = empresaService.obtenerTodas();
        for (Empresa emp : empresas) {
            table.addCell(String.valueOf(emp.getId()));
            table.addCell(emp.getNombreComercial());
            table.addCell(emp.getEmailContacto());
            table.addCell(emp.getRol());
        }

        document.add(table);
        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(25, 135, 84));
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new com.lowagie.text.Phrase("ID", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Nombre", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Email", font));
        table.addCell(cell);
        cell.setPhrase(new com.lowagie.text.Phrase("Rol", font));
        table.addCell(cell);
    }

    /**
     * Exports all offers as a CSV file.
     */
    @GetMapping("/exportar/csv")
    public void exportarCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"ofertas_reporte.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID;Titulo;Empresa;Cantidad;Estado");

        ofertaService.obtenerTodas().forEach(o -> {
            writer.println(String.format("%d;%s;%s;%s;%s",
                    o.getId(),
                    o.getTitulo(),
                    o.getEmpresa() != null ? o.getEmpresa().getNombreComercial() : "N/A",
                    o.getCantidad() != null ? o.getCantidad().toString() : "0",
                    o.getEstado() != null ? o.getEstado().toString() : "N/A"
            ));
        });

        writer.flush();
        writer.close();
    }

    @GetMapping("/ajustes")
    public String ajustes(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        model.addAttribute("navAjustes", true);
        model.addAttribute("emailActual", principal.getName());
        return "admin_ajustes";
    }

    @PostMapping("/ajustes")
    public String guardarAjustes(@RequestParam String emailAdmin, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Empresa> adminOpt = empresaService.buscarPorEmail(principal.getName());
        if (adminOpt.isPresent()) {
            Empresa admin = adminOpt.get();
            admin.setEmailContacto(emailAdmin);
            empresaService.guardar(admin);
            redirectAttributes.addFlashAttribute("mensaje", "Email de administrador actualizado correctamente.");
        }
        return "redirect:/admin/ajustes?exito=true";
    }
}
