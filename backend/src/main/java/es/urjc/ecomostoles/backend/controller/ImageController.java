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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller that serves images stored as BLOB in the database.
 * Uses the service layer for data access (Strict MVC Architecture).
 */
@RestController
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final EmpresaService empresaService;
    private final OfertaService  ofertaService;

    public ImageController(EmpresaService empresaService,
                           OfertaService  ofertaService) {
        this.empresaService = empresaService;
        this.ofertaService = ofertaService;
    }

    // ── Logo de Empresa ──────────────────────────────────────────────────────

    /**
     * Returns a company logo as binary response.
     */
    @GetMapping("/images/empresa/{id}")
    public ResponseEntity<byte[]> servirLogoEmpresa(@PathVariable Long id) {
        Optional<Empresa> empresaOpt = empresaService.buscarPorId(id);

        if (empresaOpt.isPresent() && empresaOpt.get().getLogo() != null && empresaOpt.get().getLogo().length > 0) {
            byte[] bytes = empresaOpt.get().getLogo();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).mustRevalidate())
                    .contentType(detectarMediaType(bytes))
                    .body(bytes);
        }

        // If the logo or company does not exist, return a default image (Robustness)
        return servirImagenPorDefecto();
    }

    // ── Imagen de Oferta ─────────────────────────────────────────────────────

    /**
     * Returns an offer image as binary response.
     */
    @GetMapping("/images/oferta/{id}")
    public ResponseEntity<byte[]> servirImagenOferta(@PathVariable Long id) {
        Optional<Oferta> ofertaOpt = ofertaService.buscarPorId(id);

        if (ofertaOpt.isPresent() && ofertaOpt.get().getImagen() != null && ofertaOpt.get().getImagen().length > 0) {
            byte[] bytes = ofertaOpt.get().getImagen();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).mustRevalidate())
                    .contentType(detectarMediaType(bytes))
                    .body(bytes);
        }

        // If the image or offer does not exist, return a default image (Robustness)
        return servirImagenPorDefecto();
    }

    /**
     * Loads and serves the default corporate image from the classpath.
     * 'Bulletproof' Architecture: prevents uncontrolled exceptions even if the file system fails.
     */
    private ResponseEntity<byte[]> servirImagenPorDefecto() {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/img/logo.webp");
            if (imgFile.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
                return ResponseEntity.ok()
                        .contentType(detectarMediaType(bytes))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("Error sirviendo la imagen", e);
        }
        
        // Final line of defense: return 404 so the browser/HTML handles the error (e.g., with onerror)
        return ResponseEntity.notFound().build();
    }

    private MediaType detectarMediaType(byte[] imagenBytes) {
        String mimeType = null;
        try {
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(imagenBytes));
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (Exception e) {
            log.warn("MIME type could not be detected dynamically");
        }
        return (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;
    }
}
