package com.transit.ticketsales.application;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.domain.Money;
import com.transit.ticketsales.domain.Ticket;
import org.springframework.stereotype.Service;

/** Refunds a booking from the mobile app. */
@Service
public class BookingRefundService {

    private final RefundTicket refundTicket;

    public BookingRefundService(RefundTicket refundTicket) {
        this.refundTicket = refundTicket;
    }

    public Ticket refundBooking(String bookingId, long amountInCents) {
        return refundTicket.refund(new TicketId(bookingId), Money.ofCents(amountInCents));
    }
}
