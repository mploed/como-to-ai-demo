package com.transit.ticketsales.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Money(long cents) {

    public static final Money ZERO = new Money(0);

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    public boolean isGreaterThan(Money other) {
        return cents > other.cents;
    }

    public boolean isPositive() {
        return cents > 0;
    }

    @Override
    public String toString() {
        return String.format("%d.%02d EUR", cents / 100, Math.abs(cents % 100));
    }
}
