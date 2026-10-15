package com.transit.ticketsales.application;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.domain.Money;
import com.transit.ticketsales.domain.Ticket;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class RefundTicket {

    private final TicketRepository tickets;
    private final RefundPayout payout;
    private final ApplicationEventPublisher events;

    public RefundTicket(TicketRepository tickets, RefundPayout payout, ApplicationEventPublisher events) {
        this.tickets = tickets;
        this.payout = payout;
        this.events = events;
    }

    public Ticket refund(TicketId ticketId, Money amount) {
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Ticket " + ticketId.value()));
        ticket.refund(amount);
        payout.payOut(ticketId, amount);
        tickets.save(ticket);
        ticket.pullEvents().forEach(events::publishEvent);
        return ticket;
    }
}
