package com.transit.accesscontrol;

import com.transit.ticketsales.api.events.TicketRefunded;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Policy across contexts: a refunded Ticket no longer grants access.
 * Uses only the published language of ticket-sales.
 */
@Component
class TicketRefundedListener {

    private final Gate gate;

    TicketRefundedListener(Gate gate) {
        this.gate = gate;
    }

    @EventListener
    void on(TicketRefunded event) {
        if (event.fullyRefunded()) {
            gate.revoke(event.ticketId());
        }
    }
}
