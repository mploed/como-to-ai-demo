package com.transit.accesscontrol;

import com.transit.ticketsales.api.TicketId;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Decides at the gate whether a ticket grants access. */
@Component
public class Gate {

    public record Validation(TicketId ticketId, boolean accessGranted) {
    }

    private final Set<TicketId> revoked = ConcurrentHashMap.newKeySet();

    public Validation validate(TicketId ticketId) {
        return new Validation(ticketId, !revoked.contains(ticketId));
    }

    void revoke(TicketId ticketId) {
        revoked.add(ticketId);
    }
}
