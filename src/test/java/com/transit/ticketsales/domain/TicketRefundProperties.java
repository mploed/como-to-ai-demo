package com.transit.ticketsales.domain;

import com.transit.ticketsales.api.TicketId;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * The invariant from the modeling session, for ALL inputs, not just the agreed examples:
 * whatever sequence of refunds is requested, the sum of refunds never exceeds the price paid.
 */
class TicketRefundProperties {

    @Property(tries = 2000)
    void refunds_never_exceed_the_price_paid(
            @ForAll @IntRange(min = 1, max = 10_000) int priceCents,
            @ForAll @Size(max = 8) List<@IntRange(min = 1, max = 10_000) Integer> requestedRefunds) {

        Ticket ticket = Ticket.purchase(new TicketId("T-1"), "AB", Money.ofCents(priceCents), LocalDate.of(2026, 3, 3));

        for (int cents : requestedRefunds) {
            try {
                ticket.refund(Money.ofCents(cents));
            } catch (RefundRejected expected) {
                // Rejecting is fine. Breaking the invariant is not.
            }
            if (ticket.refunded().isGreaterThan(ticket.price())) {
                throw new AssertionError("Invariant broken: refunded " + ticket.refunded()
                        + " for a Ticket priced " + ticket.price() + " after requests " + requestedRefunds);
            }
        }
    }
}
