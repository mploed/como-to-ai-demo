package com.transit.ticketsales.domain;

import com.transit.ticketsales.api.TicketId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The agreed examples from contexts/ticket-sales/scenarios/refund.feature. Read only for agents. */
class RefundScenarioTest {

    private Ticket ticketFor(int cents) {
        return Ticket.purchase(new TicketId("T-4711"), "AB", Money.ofCents(cents), LocalDate.of(2026, 3, 3));
    }

    @Test
    @DisplayName("A refund never deletes a Ticket")
    void a_refund_never_deletes_a_ticket() {
        Ticket ticket = ticketFor(400);
        ticket.refund(Money.ofCents(400));
        assertEquals(TicketStatus.REFUNDED, ticket.status());
        assertTrue(ticket.pullEvents().get(0).fullyRefunded());
    }

    @Test
    @DisplayName("Partial refunds add up")
    void partial_refunds_add_up() {
        Ticket ticket = ticketFor(400);
        ticket.refund(Money.ofCents(150));
        ticket.refund(Money.ofCents(150));
        assertEquals(TicketStatus.PARTIALLY_REFUNDED, ticket.status());
        assertEquals(Money.ofCents(300), ticket.refunded());
    }

    @Test
    @DisplayName("A validated Ticket cannot be refunded")
    void a_validated_ticket_cannot_be_refunded() {
        Ticket ticket = ticketFor(400);
        ticket.markValidated();
        assertThrows(RefundRejected.class, () -> ticket.refund(Money.ofCents(100)));
    }
}
