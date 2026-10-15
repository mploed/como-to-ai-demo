package com.transit.ticketsales.application;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.domain.Money;
import com.transit.ticketsales.domain.Ticket;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
class InMemoryTicketRepository implements TicketRepository {

    private final Map<TicketId, Ticket> tickets = new ConcurrentHashMap<>();

    InMemoryTicketRepository() {
        save(Ticket.purchase(new TicketId("T-4711"), "AB", Money.ofCents(400), LocalDate.of(2026, 3, 3)));
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return Optional.ofNullable(tickets.get(id));
    }

    @Override
    public void save(Ticket ticket) {
        tickets.put(ticket.id(), ticket);
    }
}
