package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.OfferStatus;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
import es.urjc.ecomostoles.backend.utils.FormOptionsHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.urjc.ecomostoles.backend.dto.SelectOption;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * Operational controller provisioning CRUD interfaces for Offer assets strictly tracked under the logged tenant.
 * 
 * Embeds zero-trust evaluation models explicitly blocking external data mutations by utilizing 
 * ownership checks prior to all state-changing endpoints. Abstracts file processing complexity 
 * safely away from the underlying persistence layers.
 */
@Controller
public class MyOffersController {

    private static final Logger log = LoggerFactory.getLogger(MyOffersController.class);

    private final CompanyService companyService;
    private final OfferService offerService;
    private final es.urjc.ecomostoles.backend.service.ConfigurationService configurationService;

    public MyOffersController(CompanyService companyService, OfferService offerService,
            es.urjc.ecomostoles.backend.service.ConfigurationService configurationService) {
        this.companyService = companyService;
        this.offerService = offerService;
        this.configurationService = configurationService;
    }

    /**
     * Defensive authorization gate. Resolves and validates asset alignment against the active security principle.
     * 
     * @param offerId unique constraint ID mapping the material asset.
     * @param principal current authorized connection object.
     * @return fully materialized Offer verified for logical accessibility.
     * @throws ResponseStatusException if bounded context assertions fail (HTTP 403/404).
     */
    private Offer verifyOwnership(Long offerId, Principal principal) {
        Offer offer = offerService.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Offer not found: " + offerId));

        Company loggedCompany = companyService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        boolean isAdmin = loggedCompany.getRoles() != null
                && loggedCompany.getRoles().contains("ADMIN");
        boolean isOwner = offer.getCompany() != null
                && offer.getCompany().getId().equals(loggedCompany.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this offer.");
        }

        return offer;
    }

    // -------------------------------------------------------------------------
    // GET /dashboard/mis-ofertas
    // -------------------------------------------------------------------------
    @GetMapping("/dashboard/mis-ofertas")
    public String showMyOffers(Model model, Principal principal,
            @PageableDefault(size = 6) Pageable pageable) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("activeOffers", true);
            model.addAttribute("isDashboard", true);

            Page<OfferSummary> offerPage = offerService.getByCompanyPaginated(company, pageable);
            model.addAttribute("offers", offerPage.getContent());
            model.addAttribute("hasOffers", !offerPage.isEmpty());

            // Pagination metadata
            model.addAttribute("currentPage", offerPage.getNumber() + 1);
            model.addAttribute("totalPages", offerPage.getTotalPages() == 0 ? 1 : offerPage.getTotalPages());
            model.addAttribute("hasNext", offerPage.hasNext());
            model.addAttribute("hasPrevious", offerPage.hasPrevious());
            model.addAttribute("prevPage", offerPage.getNumber() - 1);
            model.addAttribute("nextPage", offerPage.getNumber() + 1);
            model.addAttribute("totalItems", offerPage.getTotalElements());

            // Dynamic base URL for pagination partial
            model.addAttribute("paginationBaseUrl", "/dashboard/mis-ofertas");
            model.addAttribute("paginationQueryString", "");

            // KPI stats based on ALL user offers (for consistency)
            List<OfferSummary> allMyOffers = offerService.getByCompany(company);
            model.addAttribute("activeCount",
                    allMyOffers.stream().filter(o -> OfferStatus.ACTIVE.equals(o.getStatus())).count());
            model.addAttribute("pausedCount",
                    allMyOffers.stream().filter(o -> OfferStatus.PAUSED.equals(o.getStatus())).count());
            model.addAttribute("negotiationCount",
                    allMyOffers.stream().filter(o -> OfferStatus.IN_NEGOTIATION.equals(o.getStatus())).count());
            model.addAttribute("totalViews", allMyOffers.stream().mapToInt(OfferSummary::getVisits).sum());

            return "mis_activos";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // GET /oferta/nueva — Show new offer form
    // -------------------------------------------------------------------------
    @GetMapping("/oferta/nueva")
    public String showNewOfferForm(Model model, Principal principal) {
        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            model.addAttribute("activeNewOffer", true);
            model.addAttribute("isDashboard", true);
            model.addAttribute("offer", new Offer());
            injectDynamicOptions(model);
            return "crear_activo";
        }
        return "redirect:/";
    }

    private void injectDynamicOptions(Model model) {
        model.addAttribute("wasteCategories",
                FormOptionsHelper.getCategoryOptions(configurationService, null));
        model.addAttribute("unitOptions",
                FormOptionsHelper.getUnitOptions(configurationService, null));
        model.addAttribute("availabilityOptions",
                FormOptionsHelper.getUrgencyOptions(configurationService, null));
    }

    /**
     * Marshals complex multipart form payloads handling both textual metadata and BLOB image buffers.
     * 
     * @param offer DTO automatically hydrated via Spring's internal binder.
     * @param result JSR 380 constraint validation state holder.
     * @param imageFile buffered input stream intercepting multipart network boundaries.
     * @param model MVC rendering dictionary.
     * @param principal authentication wrapping context.
     * @param redirectAttributes flash storage container ensuring ephemeral messages persist over redirect jumps.
     * @return logical redirect URI enforcing PRG interaction loops.
     */
    @PostMapping("/oferta/nueva")
    public String saveNewOffer(@Valid @ModelAttribute("offer") Offer offer,
            BindingResult result,
            @RequestParam(required = false) MultipartFile imageFile,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Check image errors separately but let field validation run
        boolean imageError = false;
        if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("errorMessage", "Error: Es obligatorio subir una imagen para publicar la oferta.");
            imageError = true;
        } else {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("errorMessage",
                        "Error: El archivo seleccionado no es una imagen válida (JPG, PNG, WEBP).");
                imageError = true;
            }
        }

        if (result.hasErrors() || imageError) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("offer", offer); // Ensure attribute name matches template
            loadSelectOptions(model, offer); // Fix: use loadSelectOptions instead of injectDynamicOptions
            
            // SECURITY: Ensure sidebar knows user role on validation fail
            Company loggedUser = companyService.findByEmail(principal.getName()).orElse(null);
            if (loggedUser != null && loggedUser.getRoles().contains("ADMIN")) {
                model.addAttribute("isAdmin", true);
            }
            
            return "crear_activo";
        }

        Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
        if (companyOpt.isPresent()) {
            offer.setCompany(companyOpt.get());
            offer.setPublicationDate(LocalDateTime.now());
            offer.setStatus(OfferStatus.ACTIVE);

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    offer.setImage(imageFile.getBytes());
                } catch (Exception e) {
                    log.error("Error reading image", e);
                }
            }

            offerService.save(offer);
            redirectAttributes.addFlashAttribute("successMessage", "¡Oferta publicada con éxito!");
            return "redirect:/dashboard/mis-ofertas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /ofertas/{id}/eliminar — Delete offer (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/ofertas/{id}/eliminar")
    public String deleteOffer(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Offer offer = verifyOwnership(id, principal);

        if (offer.isClosed()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: No se puede eliminar una oferta que ya ha sido finalizada.");
            return "redirect:/dashboard/mis-ofertas";
        }

        offerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Oferta eliminada correctamente.");

        // FIX: Redirect based on role to avoid 403
        Company loggedUser = companyService.findByEmail(principal.getName()).orElseThrow();
        if (loggedUser.getRoles().contains("ADMIN")) {
            return "redirect:/admin/ofertas";
        }
        return "redirect:/dashboard/mis-ofertas";
    }

    // -------------------------------------------------------------------------
    // GET /oferta/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    private void loadSelectOptions(Model model, Offer offer) {
        // Status with selection logic
        List<SelectOption> statusOptions = new ArrayList<>();
        for (OfferStatus status : OfferStatus.values()) {
            statusOptions.add(new SelectOption(status.name(), status.getDisplayName(), status.equals(offer.getStatus())));
        }
        model.addAttribute("statusOptions", statusOptions);

        model.addAttribute("wasteCategories",
                FormOptionsHelper.getCategoryOptions(configurationService, offer.getWasteCategory()));
        model.addAttribute("unitOptions", FormOptionsHelper.getUnitOptions(configurationService, offer.getUnit()));
        model.addAttribute("availabilityOptions",
                FormOptionsHelper.getUrgencyOptions(configurationService, offer.getAvailability()));
    }

    // -------------------------------------------------------------------------
    // GET /oferta/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/ofertas/{id}/editar")
    public String showEditOfferForm(@PathVariable Long id,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Offer offer = verifyOwnership(id, principal);

        if (offer.isClosed()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: No es posible editar ofertas que ya han sido finalizadas.");
            return "redirect:/dashboard/mis-ofertas";
        }

        model.addAttribute("offer", offer);
        model.addAttribute("isDashboard", true);

        loadSelectOptions(model, offer);

        return "editar_activo";
    }

    // -------------------------------------------------------------------------
    // POST /oferta/editar/{id} — Save changes with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/ofertas/{id}/editar")
    public String saveOfferChanges(@PathVariable Long id,
            @Valid @ModelAttribute("offer") Offer offerForm,
            BindingResult result,
            @RequestParam(required = false) MultipartFile imageFile,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Company loggedUser = companyService.findByEmail(principal.getName()).orElseThrow();
        Offer offer = verifyOwnership(id, principal);

        if (offer.isClosed()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error de seguridad: Intento de edición sobre una oferta finalizada.");
            return "redirect:/dashboard/mis-ofertas";
        }

        // Validate image content type (Security check)
        boolean imageError = false;
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("errorMessage",
                        "Error: El archivo seleccionado no es una imagen válida (JPG, PNG, WEBP).");
                imageError = true;
            }
        }

        if (result.hasErrors() || imageError) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            loadSelectOptions(model, offerForm);
            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("offer", offerForm); // Ensure attribute name matches template 'offer'
            offerForm.setId(id);
            
            // SECURITY: Ensure sidebar knows user role on validation fail
            if (loggedUser.getRoles().contains("ADMIN")) {
                model.addAttribute("isAdmin", true);
            }
            
            return "editar_activo";
        }

        offer.setTitle(offerForm.getTitle());
        offer.setDescription(offerForm.getDescription());
        offer.setQuantity(offerForm.getQuantity());
        offer.setPrice(offerForm.getPrice());
        offer.setWasteCategory(offerForm.getWasteCategory());
        offer.setUnit(offerForm.getUnit());
        offer.setAvailability(offerForm.getAvailability());
        offer.setStatus(offerForm.getStatus());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                offer.setImage(imageFile.getBytes());
            } catch (Exception e) {
                log.error("Error reading updated image", e);
            }
        }

        offerService.save(offer);
        redirectAttributes.addFlashAttribute("successMessage", "Cambios guardados correctamente.");

        // FIX: Redirect based on role to avoid 403
        if (loggedUser.getRoles().contains("ADMIN")) {
            return "redirect:/admin/ofertas";
        }
        return "redirect:/dashboard/mis-ofertas";
    }
}
