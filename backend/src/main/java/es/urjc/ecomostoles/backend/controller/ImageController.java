package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.service.EmpresaService;
import es.urjc.ecomostoles.backend.service.OfertaService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

        if (empresaOpt.isEmpty() || empresaOpt.get().getLogo() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] logo = empresaOpt.get().getLogo();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .contentType(MediaType.IMAGE_JPEG)
                .body(logo);
    }

    // ── Imagen de Oferta ─────────────────────────────────────────────────────

    /**
     * Devuelve la imagen de una oferta como respuesta binaria.
     */
    @GetMapping("/images/oferta/{id}")
    public ResponseEntity<byte[]> servirImagenOferta(@PathVariable Long id) {
        Optional<Oferta> ofertaOpt = ofertaService.buscarPorId(id);

        if (ofertaOpt.isEmpty() || ofertaOpt.get().getImagen() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imagen = ofertaOpt.get().getImagen();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .contentType(MediaType.IMAGE_JPEG)
                .body(imagen);
    }
}
