package com.transit.ticketsales.api.events;

import com.transit.ticketsales.api.TicketId;
import org.jmolecules.event.annotation.DomainEvent;

/**
 * Published language of ticket-sales (Open Host Service).
 * Changing this type needs the owners of access-control and customer-care.
 */
@DomainEvent
public record TicketRefunded(TicketId ticketId, long amountInCents, boolean fullyRefunded) {
}
