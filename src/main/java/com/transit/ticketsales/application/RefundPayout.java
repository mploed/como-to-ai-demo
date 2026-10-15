package com.transit.ticketsales.application;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.domain.Money;

/** Port in the language of ticket-sales. Implemented by the anticorruption layer. */
public interface RefundPayout {

    void payOut(TicketId ticketId, Money amount);
}
