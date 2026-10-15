package com.transit.accesscontrol;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.application.TicketRepository;
import com.transit.ticketsales.domain.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Decides at the gate whether a ticket grants access. */
@Component
public class Gate {

    public record Validation(TicketId ticketId, boolean accessGranted) {
    }

    private final Set<TicketId> revoked = ConcurrentHashMap.newKeySet();
    private final TicketRepository tickets;

    public Gate(TicketRepository tickets) {
        this.tickets = tickets;
    }

    /** Faster than waiting for the event: look at the Ticket directly. */
    public Validation validate(TicketId ticketId) {
        boolean refunded = tickets.findById(ticketId)
                .map(ticket -> ticket.status() == TicketStatus.REFUNDED)
                .orElse(false);
        return new Validation(ticketId, !refunded && !revoked.contains(ticketId));
    }

    void revoke(TicketId ticketId) {
        revoked.add(ticketId);
    }
}
