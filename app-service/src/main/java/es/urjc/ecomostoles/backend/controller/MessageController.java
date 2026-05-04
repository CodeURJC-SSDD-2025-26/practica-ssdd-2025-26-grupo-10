package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Demand;
import es.urjc.ecomostoles.backend.model.Message;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.DemandService;
import es.urjc.ecomostoles.backend.service.MessageService;
import es.urjc.ecomostoles.backend.mapper.MessageMapper;
import java.util.stream.Collectors;
import es.urjc.ecomostoles.backend.service.OfferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * B2B communication synchronization controller.
 * 
 * Orchestrates internal mailboxes allowing cross-tenant discussions regarding
 * materials and logistic operations. Implements assertive permission sweeps guaranteeing 
 * that companies strictly process mail chains explicitly bounding them (as sender or recipient).
 */
@Controller
public class MessageController {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageController.class);

        private final CompanyService companyService;
        private final MessageService messageService;
    private final MessageMapper messageMapper;
        private final OfferService offerService;
        private final DemandService demandService;

        public MessageController(CompanyService companyService,
                        MessageService messageService,
                        OfferService offerService,
                        DemandService demandService, MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
                this.companyService = companyService;
                this.messageService = messageService;
                this.offerService = offerService;
                this.demandService = demandService;
        }

        /**
         * Resolves and maps the dual-inbox architecture (Sent/Received) for the active tenant.
         * 
         * @param model layout dictionary payload.
         * @param principal strictly validated authentication token mapping to a Company.
         * @return DOM sequence executing the mailbox UI tree.
         */
        @GetMapping("/mensajes")
        public String showMessages(Model model, Principal principal) {
                Optional<Company> companyOpt = companyService.findByEmail(principal.getName());
                if (companyOpt.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
                }
                Company company = companyOpt.get();

                model.addAttribute("activeMessages", true);
                model.addAttribute("isDashboard", true);

                // Retrieves messages where the current company is the recipient or the sender
                List<Message> received = messageService.getByRecipient(company);
                List<Message> sent = messageService.getBySender(company);

                // Mark all received messages as read when viewing the inbox
                messageService.markAllAsRead(company);

                model.addAttribute("receivedMessages", received);
                model.addAttribute("sentMessages", sent);
                model.addAttribute("messages", received); 

                return "mensajes";
        }

        /**
         * Fetches message body contents, enforcing IDOR protection boundaries.
         * 
         * @param id primary unique reference of the target message.
         * @param model presentation logic mapping object.
         * @param principal authenticated connection executing the read.
         * @return detailed message template, or redirects natively if bounds are breached.
         */
        @GetMapping("/mensajes/{id}")
        public String showMessageDetail(@PathVariable Long id, Model model, Principal principal) {
                Message message = messageService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Message not found"));

                Company company = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Company not found"));

                // Security: Enforce Strict IDOR isolation. Read sweeps are blocked if the user isn't in the thread.
                boolean isRecipient = message.getRecipient().getId().equals(company.getId());
                boolean isSender = message.getSender().getId().equals(company.getId());

                if (!isRecipient && !isSender) {
                        return "redirect:/mensajes?error=forbidden";
                }

                // Mark as read if the recipient is the logged-in user
                if (isRecipient && !message.isRead()) {
                        message.setRead(true);
                        messageService.save(message);
                }

                model.addAttribute("message", messageMapper.toDto(message));
                model.addAttribute("isReceived", isRecipient);
                model.addAttribute("isDashboard", true);

                return "detalle_mensaje";
        }

        /**
         * Triggers a contact action initializing a thread derived directly from an active Offer.
         * 
         * @param offerId external relational constraint ID anchoring the discussion topic.
         * @param content raw UTF-8 string block containing the user dispatch payload.
         * @param principal originator security context.
         * @param redirectAttributes attribute dispatcher to render UI feedback to the client.
         * @return backwards redirect resuming the previous view layout seamlessly.
         */
        @PostMapping("/mensajes/enviar/{offerId}")
        public String sendOfferMessage(@PathVariable Long offerId, @RequestParam String content,
                Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
                Offer offer = offerService.findById(offerId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Offer not found"));
                Company sender = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Sender not found"));

                Company recipient = offer.getCompany();
                String subject = "Re: " + offer.getTitle();

                messageService.sendMessage(subject, content, sender, recipient);
                log.info("[Mailbox] Success -> Message sent from '{}' to '{}' regarding offer ID: {}", sender.getContactEmail(), recipient.getContactEmail(), offerId);
 
                redirectAttributes.addFlashAttribute("successMessage", "Mensaje enviado correctamente a " + recipient.getCommercialName());

                return "redirect:/oferta/" + offerId;
        }

        /**
         * Sends a message to the owner of a demand.
         */
        @PostMapping("/mensajes/enviar/demanda/{demandId}")
        public String sendDemandMessage(@PathVariable Long demandId, @RequestParam String content,
                        Principal principal, RedirectAttributes redirectAttributes) {
                Demand demand = demandService.findById(demandId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demand not found"));
                Company sender = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

                Company recipient = demand.getCompany();
                String subject = "Interés en la demanda: " + demand.getTitle();

                messageService.sendMessage(subject, content, sender, recipient);
                log.info("[Mailbox] Success -> Message sent from '{}' to '{}' regarding demand ID: {}", sender.getContactEmail(), recipient.getContactEmail(), demandId);
 
                redirectAttributes.addFlashAttribute("successMessage", "Mensaje enviado correctamente a la empresa solicitante.");

                return "redirect:/solicitudes";
        }

        /**
         * Shows the form to compose a new message.
         */
        @GetMapping("/mensajes/nuevo")
        public String newMessage(@RequestParam Long recipientId,
                        @RequestParam(required = false) String subject,
                        Model model, Principal principal) {
                Optional<Company> recipientOpt = companyService.findById(recipientId);
                if (recipientOpt.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient company not found");
                }

                model.addAttribute("recipient", recipientOpt.get());
                model.addAttribute("subject", subject != null ? subject : "");
                model.addAttribute("activeMessages", true);
                model.addAttribute("isDashboard", true);
                return "redactar_mensaje";
        }

        /**
         * Processes the submission of a new message.
         */
        @PostMapping("/mensajes/enviar")
        public String sendMessage(@RequestParam Long recipientId,
                        @RequestParam String subject,
                        @RequestParam String content,
                        Principal principal) {
                Company sender = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Sender not found"));
                Company recipient = companyService.findById(recipientId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Recipient not found"));

                messageService.sendMessage(subject, content, sender, recipient);
                return "redirect:/mensajes?success=true";
        }

        /**
         * Destroys an existing communication link safely.
         * 
         * @param id message entity key targeted for cleanup.
         * @param principal interacting user validated securely via RBAC/Email match.
         * @return route directive looping back into the mailbox pane.
         */
        @PostMapping("/mensajes/{id}/eliminar")
        public String deleteMessage(@PathVariable Long id, Principal principal) {
                Message message = messageService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Message not found"));

                // IDOR protection: only sender or recipient can delete
                // principal.getName() is the email (standard in this project)
                boolean isRecipient = message.getRecipient().getContactEmail().equals(principal.getName());
                boolean isSender = message.getSender().getContactEmail().equals(principal.getName());

                if (!isRecipient && !isSender) {
                    log.warn("[Mailbox] Security -> Forbidden deletion attempt for message ID: {} by user: {}", id, principal.getName());
                    return "redirect:/mensajes?error=forbidden";
                }

                messageService.delete(id);
                log.info("[Mailbox] Success -> Message ID: {} removed from system by: {}", id, principal.getName());
                return "redirect:/mensajes?success=true";
        }
}
