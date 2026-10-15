package com.transit.ticketsales.application;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.domain.Ticket;

import java.util.Optional;

public interface TicketRepository {

    Optional<Ticket> findById(TicketId id);

    void save(Ticket ticket);
}
