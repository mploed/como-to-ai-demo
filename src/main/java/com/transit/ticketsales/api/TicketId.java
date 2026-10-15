package com.transit.ticketsales.api;

import org.jmolecules.ddd.annotation.ValueObject;

/** Part of the published language of ticket-sales. */
@ValueObject
public record TicketId(String value) {

    public TicketId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A TicketId needs a value");
        }
    }
}
