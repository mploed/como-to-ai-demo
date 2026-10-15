package com.transit.paymentprovider.api;

/** Published API of the external payment provider (upstream, Open Host Service). */
public interface PayoutClient {

    PayoutReceipt payout(String merchantReference, long amountInMinorUnits, String currencyIsoCode);
}
