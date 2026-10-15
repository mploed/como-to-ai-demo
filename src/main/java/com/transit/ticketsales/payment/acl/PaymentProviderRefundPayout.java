package com.transit.ticketsales.payment.acl;

import com.transit.paymentprovider.api.PayoutClient;
import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.application.RefundPayout;
import com.transit.ticketsales.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Anticorruption layer: the only place in ticket-sales that knows the payment provider's
 * language (payout, merchant reference, minor units).
 */
@Component
class PaymentProviderRefundPayout implements RefundPayout {

    private final PayoutClient client;

    PaymentProviderRefundPayout(PayoutClient client) {
        this.client = client;
    }

    @Override
    public void payOut(TicketId ticketId, Money amount) {
        client.payout("ticket-" + ticketId.value(), amount.cents(), "EUR");
    }
}
