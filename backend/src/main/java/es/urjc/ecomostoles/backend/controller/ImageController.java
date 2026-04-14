package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.Oferta;
import es.urjc.ecomostoles.backend.repository.EmpresaRepository;
import es.urjc.ecomostoles.backend.repository.OfertaRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Controlador que sirve imágenes almacenadas como BLOB en la base de datos.
 *
 * Rutas disponibles:
 * GET /images/empresa/{id} → logo de la Empresa con ese ID
 * GET /images/oferta/{id} → imagen de la Oferta con ese ID
 */
@RestController
public class ImageController {

    private final EmpresaRepository empresaRepository;
    private final OfertaRepository ofertaRepository;

    public ImageController(EmpresaRepository empresaRepository,
            OfertaRepository ofertaRepository) {
        this.empresaRepository = empresaRepository;
        this.ofertaRepository = ofertaRepository;
    }

    // ── Logo de Empresa ──────────────────────────────────────────────────────

    /**
     * Devuelve el logo de una empresa como respuesta binaria.
     * Uso en HTML: <img th:src="@{/images/empresa/{id}(id=${empresa.id})}">
     * o con Mustache: <img src="/images/empresa/{{empresa.id}}">
     */
    @GetMapping("/images/empresa/{id}")
    public ResponseEntity<byte[]> servirLogoEmpresa(@PathVariable Long id) {
        Optional<Empresa> empresaOpt = empresaRepository.findById(id);

        if (empresaOpt.isEmpty() || empresaOpt.get().getLogo() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] logo = empresaOpt.get().getLogo();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // JPEG por defecto; el navegador auto-detecta
        headers.setContentLength(logo.length);

        return new ResponseEntity<>(logo, headers, HttpStatus.OK);
    }

    // ── Imagen de Oferta ─────────────────────────────────────────────────────

    /**
     * Devuelve la imagen de una oferta como respuesta binaria.
     * Uso en HTML: <img src="/images/oferta/{{oferta.id}}">
     */
    @GetMapping("/images/oferta/{id}")
    public ResponseEntity<byte[]> servirImagenOferta(@PathVariable Long id) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(id);

        if (ofertaOpt.isEmpty() || ofertaOpt.get().getImagen() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imagen = ofertaOpt.get().getImagen();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(imagen.length);

        return new ResponseEntity<>(imagen, headers, HttpStatus.OK);
    }
}
