package es.urjc.ecomostoles.backend.controller;

import es.urjc.ecomostoles.backend.model.Company;
import es.urjc.ecomostoles.backend.model.Message;
import es.urjc.ecomostoles.backend.model.Offer;
import es.urjc.ecomostoles.backend.service.CompanyService;
import es.urjc.ecomostoles.backend.service.MessageService;
import es.urjc.ecomostoles.backend.service.OfferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller to handle reading and listing messages.
 * Uses Controller > Service > Repository architecture.
 */
@Controller
public class MessageController {

        private final CompanyService companyService;
        private final MessageService messageService;
        private final OfferService offerService;

        public MessageController(CompanyService companyService,
                        MessageService messageService,
                        OfferService offerService) {
                this.companyService = companyService;
                this.messageService = messageService;
                this.offerService = offerService;
        }

        /**
         * Shows the inbox messages for the active company.
         *
         * @param model Spring UI model
         * @return view template "mensajes"
         */
        @GetMapping("/mensajes")
        public String showMessages(Model model, Principal principal) {
                Company company = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Recurso no encontrado"));

                model.addAttribute("activeMensajes", true);
                model.addAttribute("isDashboard", true);

                // Retrieves messages where the current company is the recipient or the sender
                List<Message> received = messageService.getByRecipient(company);
                List<Message> sent = messageService.getBySender(company);

                model.addAttribute("mensajesRecibidos", received);
                model.addAttribute("mensajesEnviados", sent);
                model.addAttribute("mensajes", received); // for backward compatibility in templates if needed

                return "mensajes";
        }

        /**
         * Shows the detail of a specific message.
         */
        @GetMapping("/mensajes/{id}")
        public String showMessageDetail(@PathVariable Long id, Model model, Principal principal) {
                Message message = messageService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Mensaje no encontrado"));

                Company company = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Empresa no encontrada"));

                // Security: Only the sender or the recipient can view the message
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

                model.addAttribute("mensaje", message);
                model.addAttribute("esRecibido", isRecipient);
                model.addAttribute("isDashboard", true);

                return "detalle_mensaje";
        }

        /**
         * Sends a message to the owner of an offer.
         */
        @PostMapping("/mensajes/enviar/{ofertaId}")
        public String sendOfferMessage(@PathVariable Long offerId, @RequestParam String content,
                        Principal principal) {
                Offer offer = offerService.findById(offerId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Oferta no encontrada"));
                Company sender = companyService.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Remitente no encontrado"));

                Company recipient = offer.getCompany();
                String subject = "Re: " + offer.getTitle();

                messageService.sendMessage(subject, content, sender, recipient);

                return "redirect:/mensajes";
        }

        /**
         * Shows the form to compose a new message.
         */
        @GetMapping("/mensajes/nuevo")
        public String newMessage(@RequestParam Long recipientId,
                        @RequestParam(required = false) String subject,
                        Model model, Principal principal) {
                Company recipient = companyService.findById(recipientId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Empresa receptora no encontrada"));

                model.addAttribute("receptor", recipient);
                model.addAttribute("asunto", subject != null ? subject : "");
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
                                                "Remitente no encontrado"));
                Company recipient = companyService.findById(recipientId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Destinatario no encontrado"));

                messageService.sendMessage(subject, content, sender, recipient);
                return "redirect:/mensajes?exito=true";
        }

        /**
         * Deletes a message (Security: ownership check).
         */
        @PostMapping("/mensajes/{id}/eliminar")
        public String deleteMessage(@PathVariable Long id, Principal principal) {
                Message message = messageService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Mensaje no encontrado"));

                // IDOR protection: only sender or recipient can delete
                // principal.getName() is the email (standard in this project)
                boolean isRecipient = message.getRecipient().getContactEmail().equals(principal.getName());
                boolean isSender = message.getSender().getContactEmail().equals(principal.getName());

                if (!isRecipient && !isSender) {
                        return "redirect:/mensajes?error=forbidden";
                }

                messageService.delete(id);
                return "redirect:/mensajes?exito=true";
        }
}
