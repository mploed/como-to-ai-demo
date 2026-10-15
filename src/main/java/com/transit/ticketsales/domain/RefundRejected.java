package com.transit.ticketsales.domain;

public class RefundRejected extends RuntimeException {

    public RefundRejected(String message) {
        super(message);
    }
}
