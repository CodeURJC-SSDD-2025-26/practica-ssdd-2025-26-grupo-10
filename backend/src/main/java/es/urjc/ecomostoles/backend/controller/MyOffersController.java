package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.OfferStatus;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.dto.OfferSummary;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.OfferService;
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
 * Manages offers for the authenticated user (CRUD + ownership logic).
 *
 * Follows Controller > Service > Repository architecture:
 * delegates all data access to OfferService and CompanyService.
 *
 * Ownership rule:
 * Before editing or deleting an offer, we verify that the logged-in user
 * is the author company OR has ADMIN role. Returns 403 Forbidden otherwise.
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

    // -------------------------------------------------------------------------
    // Ownership helper: returns the offer if the user is the author or ADMIN.
    // -------------------------------------------------------------------------
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
            @PageableDefault(size = 5) Pageable pageable) {
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
            model.addAttribute("totalPages", offerPage.getTotalPages());
            model.addAttribute("hasNext", offerPage.hasNext());
            model.addAttribute("hasPrev", offerPage.hasPrevious());
            model.addAttribute("prevPage", offerPage.getNumber() - 1);
            model.addAttribute("nextPage", offerPage.getNumber() + 1);
            model.addAttribute("totalItems", offerPage.getTotalElements());

            // Dynamic base URL for pagination partial
            model.addAttribute("paginationBaseUrl", "/dashboard/mis-offers");
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
        model.addAttribute("categoryList", configurationService.getSanitizedList("categoryList"));
        model.addAttribute("unitList", configurationService.getSanitizedList("unitList"));
        model.addAttribute("availabilityList",
                configurationService.getSanitizedList("availabilityList"));
    }

    // -------------------------------------------------------------------------
    // POST /oferta/nueva — Create new offer with Bean Validation
    // -------------------------------------------------------------------------
    @PostMapping("/oferta/nueva")
    public String saveNewOffer(@Valid @ModelAttribute("offer") Offer offer,
            BindingResult result,
            @RequestParam(required = false) MultipartFile imageFile,
            Model model,
            Principal principal) {

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("offer", offer);
            injectDynamicOptions(model);
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
            return "redirect:/dashboard/mis-ofertas";
        }
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // POST /ofertas/{id}/eliminar — Delete offer (ownership check)
    // -------------------------------------------------------------------------
    @PostMapping("/ofertas/{id}/eliminar")
    public String deleteOffer(@PathVariable Long id, Principal principal) {
        verifyOwnership(id, principal);
        offerService.delete(id);
        return "redirect:/dashboard/mis-ofertas";
    }

    // -------------------------------------------------------------------------
    // GET /oferta/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    private void loadSelectOptions(Model model, Offer offer) {
        // Dynamic Select Options for status
        List<SelectOption> statusOptions = new ArrayList<>();
        statusOptions.add(new SelectOption("ACTIVE", "ACTIVA",
                "ACTIVE".equals(offer.getStatus() != null ? offer.getStatus().name() : "")));
        statusOptions.add(new SelectOption("PAUSED", "PAUSADA",
                "PAUSED".equals(offer.getStatus() != null ? offer.getStatus().name() : "")));
        model.addAttribute("statusOptions", statusOptions);

        // Dynamic Categories
        List<String> categories = configurationService.getSanitizedList("categoryList");
        List<SelectOption> categoryOptions = new ArrayList<>();
        for (String cat : categories) {
            categoryOptions.add(new SelectOption(cat, cat, cat.equals(offer.getWasteType())));
        }
        model.addAttribute("categoryOptions", categoryOptions);

        // Dynamic Units
        List<String> unitsList = configurationService.getSanitizedList("unitList");
        List<SelectOption> unitOptions = new ArrayList<>();
        for (String u : unitsList) {
            unitOptions.add(new SelectOption(u, u, u.equals(offer.getUnit())));
        }
        model.addAttribute("unitOptions", unitOptions);

        // Dynamic Availability
        List<String> availabilityList = configurationService.getSanitizedList("availabilityList");
        List<SelectOption> availabilityOptions = new ArrayList<>();
        for (String d : availabilityList) {
            availabilityOptions.add(new SelectOption(d, d, d.equals(offer.getAvailability())));
        }
        model.addAttribute("availabilityOptions", availabilityOptions);
    }

    // -------------------------------------------------------------------------
    // GET /oferta/editar/{id} — Show edit form (ownership check)
    // -------------------------------------------------------------------------
    @GetMapping("/ofertas/{id}/editar")
    public String showEditOfferForm(@PathVariable Long id,
            Model model,
            Principal principal) {
        Offer offer = verifyOwnership(id, principal);
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
            Principal principal) {

        // SECURITY: Verify ownership BEFORE validation to prevent Data Leak
        Offer offer = verifyOwnership(id, principal);

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(err -> model.addAttribute("error_" + err.getField(), true));
            loadSelectOptions(model, offerForm);

            model.addAttribute("errors", result.getAllErrors());
            model.addAttribute("offer", offerForm);
            offerForm.setId(id);
            return "editar_activo";
        }

        offer.setTitle(offerForm.getTitle());
        offer.setDescription(offerForm.getDescription());
        offer.setQuantity(offerForm.getQuantity());
        offer.setPrice(offerForm.getPrice());
        offer.setWasteType(offerForm.getWasteType());
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
        return "redirect:/dashboard/mis-ofertas";
    }
}
