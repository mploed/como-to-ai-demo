package com.transit.customercare;

import com.transit.ticketsales.api.TicketId;

/** A passenger's support case. Conforms to the published language of ticket-sales. */
public record SupportCase(String caseNumber, TicketId ticketId, String description) {
}
