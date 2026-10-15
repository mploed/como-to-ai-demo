package com.transit.ticketsales.web;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.application.RefundTicket;
import com.transit.ticketsales.domain.Money;
import com.transit.ticketsales.domain.RefundRejected;
import com.transit.ticketsales.domain.Ticket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Used by the mobile app. */
@RestController
class RefundController {

    record RefundRequest(long amountInCents) {
    }

    record RefundResponse(String ticketId, String status, String refunded) {
    }

    private final RefundTicket refundTicket;

    RefundController(RefundTicket refundTicket) {
        this.refundTicket = refundTicket;
    }

    @PostMapping("/tickets/{ticketId}/refunds")
    ResponseEntity<?> refund(@PathVariable String ticketId, @RequestBody RefundRequest request) {
        try {
            Ticket ticket = refundTicket.refund(new TicketId(ticketId), Money.ofCents(request.amountInCents()));
            return ResponseEntity.ok(new RefundResponse(ticketId, ticket.status().name(), ticket.refunded().toString()));
        } catch (RefundRejected rejected) {
            return ResponseEntity.unprocessableEntity().body(rejected.getMessage());
        }
    }
}
