package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador que sirve imágenes almacenadas como BLOB en la base de datos.
 * Utiliza la capa de servicio para el acceso a datos (Arquitectura MVC Estricta).
 */
@RestController
public class ImageController {

    private final EmpresaService empresaService;
    private final OfertaService  ofertaService;

    public ImageController(EmpresaService empresaService,
                           OfertaService  ofertaService) {
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
    }

    // ── Logo de Empresa ──────────────────────────────────────────────────────

    /**
     * Devuelve el logo de una empresa como respuesta binaria.
     */
    @GetMapping("/images/empresa/{id}")
    public ResponseEntity<byte[]> servirLogoEmpresa(@PathVariable Long id) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorId(id);

        if (empresaOpt.isPresent() && empresaOpt.get().getLogo() != null && empresaOpt.get().getLogo().length > 0) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(empresaOpt.get().getLogo());
        }

        // Si no existe el logo o la empresa, devolvemos imagen por defecto (Robustez)
        return servirImagenPorDefecto();
    }

    // ── Imagen de Oferta ─────────────────────────────────────────────────────

    /**
     * Devuelve la imagen de una oferta como respuesta binaria.
     */
    @GetMapping("/images/oferta/{id}")
    public ResponseEntity<byte[]> servirImagenOferta(@PathVariable Long id) {
        Optional<Oferta> ofertaOpt = ofertaService.buscarPorId(id);

        if (ofertaOpt.isPresent() && ofertaOpt.get().getImagen() != null && ofertaOpt.get().getImagen().length > 0) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(ofertaOpt.get().getImagen());
        }

        // Si no existe la imagen o la oferta, devolvemos imagen por defecto (Robustez)
        return servirImagenPorDefecto();
    }

    /**
     * Carga y sirve la imagen corporativa por defecto desde el classpath.
     * Arquitectura 'Bulletproof': previene excepciones no controladas incluso si falla el sistema de archivos.
     */
    private ResponseEntity<byte[]> servirImagenPorDefecto() {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/img/logo.webp");
            if (imgFile.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(bytes);
            }
        } catch (Exception e) {
            // Silenciamos la excepción para evitar un Error 500 y permitimos que el flujo continúe al 404
        }
        
        // Última línea de defensa: devolvemos 404 para que el navegador/HTML gestione el error (p.ej. con onerror)
        return ResponseEntity.notFound().build();
    }
}
