package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
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

    private final CompanyService companyService;
    private final OfferService offerService;

    public ImageController(CompanyService companyService,
            OfferService offerService) {
        this.companyService = companyService;
        this.offerService = offerService;
    }

    // ── Company Logo ──────────────────────────────────────────────────────

    /**
     * Returns a company logo as binary response.
     */
    @GetMapping("/images/empresa/{id}")
    public ResponseEntity<byte[]> serveCompanyLogo(@PathVariable Long id) {
        Optional<Company> companyOpt = companyService.findById(id);

        if (companyOpt.isPresent() && companyOpt.get().getLogo() != null && companyOpt.get().getLogo().length > 0) {
            byte[] bytes = companyOpt.get().getLogo();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).mustRevalidate())
                    .contentType(detectMediaType(bytes))
                    .body(bytes);
        }

        // If the logo or company does not exist, return a default image (Robustness)
        return serveDefaultImage();
    }

    // ── Offer Image ─────────────────────────────────────────────────────

    /**
     * Returns an offer image as binary response.
     */
    @GetMapping("/images/oferta/{id}")
    public ResponseEntity<byte[]> serveOfferImage(@PathVariable Long id) {
        Optional<Offer> offerOpt = offerService.findById(id);

        if (offerOpt.isPresent() && offerOpt.get().getImage() != null && offerOpt.get().getImage().length > 0) {
            byte[] bytes = offerOpt.get().getImage();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).mustRevalidate())
                    .contentType(detectMediaType(bytes))
                    .body(bytes);
        }

        // If the image or offer does not exist, return a default image (Robustness)
        return serveDefaultImage();
    }

    /**
     * Loads and serves the default corporate image from the classpath.
     * 'Bulletproof' Architecture: prevents uncontrolled exceptions even if the file
     * system fails.
     */
    private ResponseEntity<byte[]> serveDefaultImage() {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/img/logo.webp");
            if (imgFile.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
                return ResponseEntity.ok()
                        .contentType(detectMediaType(bytes))
                        .body(bytes);
            }
        } catch (Exception e) {
            log.error("Error serving the image", e);
        }

        // Final line of defense: return 404 so the browser/HTML handles the error
        // (e.g., with onerror)
        return ResponseEntity.notFound().build();
    }

    private MediaType detectMediaType(byte[] imageBytes) {
        String mimeType = null;
        try {
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(imageBytes));
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (Exception e) {
            log.warn("MIME type could not be detected dynamically");
        }
        return (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;
    }
}
