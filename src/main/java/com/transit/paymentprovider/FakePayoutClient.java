package com.transit.paymentprovider;

import com.transit.paymentprovider.api.PayoutClient;
import com.transit.paymentprovider.api.PayoutReceipt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Stand-in for the external provider. Internals that nobody outside may touch. */
@Component
class FakePayoutClient implements PayoutClient {

    @Override
    public PayoutReceipt payout(String merchantReference, long amountInMinorUnits, String currencyIsoCode) {
        return new PayoutReceipt(UUID.randomUUID().toString(), merchantReference);
    }
}
