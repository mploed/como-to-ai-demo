package com.transit.ticketsales.domain;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.api.events.TicketRefunded;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A fare product a customer has purchased and paid for.
 *
 * Invariant: the sum of refunds never exceeds the price paid.
 * (Whether a fully refunded Ticket still grants access is NOT an invariant of this aggregate,
 * it is a policy across contexts, see TicketRefunded and access-control.)
 */
@AggregateRoot
public class Ticket {

    @Identity
    private final TicketId id;
    private final String zone;
    private final Money price;
    private final LocalDate purchasedOn;
    private boolean validated;
    private final List<Money> refunds = new ArrayList<>();
    private final List<TicketRefunded> events = new ArrayList<>();

    private Ticket(TicketId id, String zone, Money price, LocalDate purchasedOn) {
        if (!price.isPositive()) {
            throw new IllegalArgumentException("A Ticket has a positive price");
        }
        this.id = id;
        this.zone = zone;
        this.price = price;
        this.purchasedOn = purchasedOn;
    }

    public static Ticket purchase(TicketId id, String zone, Money price, LocalDate purchasedOn) {
        return new Ticket(id, zone, price, purchasedOn);
    }

    /** Fact reported by access-control. */
    public void markValidated() {
        this.validated = true;
    }

    public void refund(Money amount) {
        if (validated) {
            throw new RefundRejected("A validated Ticket cannot be refunded");
        }
        if (!amount.isPositive()) {
            throw new RefundRejected("A refund must be positive, was " + amount);
        }
        if (refunded().plus(amount).isGreaterThan(price)) {
            throw new RefundRejected("Refunds would exceed the price paid: already refunded "
                    + refunded() + ", requested " + amount + ", price " + price);
        }
        refunds.add(amount);
        events.add(new TicketRefunded(id, amount.cents(), isRefunded()));
    }

    public Money refunded() {
        return refunds.stream().reduce(Money.ZERO, Money::plus);
    }

    public boolean isRefunded() {
        return refunded().equals(price);
    }

    public TicketStatus status() {
        if (isRefunded()) {
            return TicketStatus.REFUNDED;
        }
        return refunds.isEmpty() ? TicketStatus.ACTIVE : TicketStatus.PARTIALLY_REFUNDED;
    }

    public List<TicketRefunded> pullEvents() {
        List<TicketRefunded> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public TicketId id() {
        return id;
    }

    public String zone() {
        return zone;
    }

    public Money price() {
        return price;
    }

    public LocalDate purchasedOn() {
        return purchasedOn;
    }
}
