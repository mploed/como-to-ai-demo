package com.transit.ticketsales.domain;

import com.transit.ticketsales.api.TicketId;
import com.transit.ticketsales.api.events.TicketRefunded;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions, owned by the engineers. They translate the expert's sentences into calls
 * on the Ticket aggregate and nothing else: no logic of their own.
 */
public class RefundSteps {

    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

    private Ticket ticket;
    private RefundRejected rejected;
    private final List<TicketRefunded> published = new ArrayList<>();

    @Given("^a Ticket for zone (\\w+), bought on (\\d{1,2} \\w+) for (\\d+\\.\\d{2}) EUR, (not validated|validated at a gate)$")
    public void a_ticket(String zone, String dayAndMonth, String price, String validation) {
        LocalDate purchasedOn = MonthDay.parse(dayAndMonth, DAY_MONTH).atYear(2026);
        ticket = Ticket.purchase(new TicketId("T-4711"), zone, euros(price), purchasedOn);
        if (validation.equals("validated at a gate")) {
            ticket.markValidated();
        }
    }

    @When("^the customer requests (?:a|another) refund of (\\d+\\.\\d{2}) EUR$")
    public void the_customer_requests_a_refund(String amount) {
        try {
            ticket.refund(euros(amount));
            published.addAll(ticket.pullEvents());
        } catch (RefundRejected e) {
            rejected = e;
        }
    }

    @Then("^the Ticket is marked (Active|Partially Refunded|Refunded)$")
    public void the_ticket_is_marked(String status) {
        assertEquals(TicketStatus.valueOf(status.toUpperCase(Locale.ROOT).replace(' ', '_')), ticket.status());
    }

    @Then("^TicketRefunded is published$")
    public void ticket_refunded_is_published() {
        assertFalse(published.isEmpty(), "expected a TicketRefunded event");
        assertTrue(published.get(published.size() - 1).fullyRefunded());
    }

    @Then("^the total refunded amount is (\\d+\\.\\d{2}) EUR$")
    public void the_total_refunded_amount_is(String amount) {
        assertEquals(euros(amount), ticket.refunded());
    }

    @Then("^the refund is rejected$")
    public void the_refund_is_rejected() {
        assertNotNull(rejected, "expected the refund to be rejected");
        assertEquals(Money.ZERO, ticket.refunded());
    }

    private static Money euros(String amount) {
        return Money.ofCents(new BigDecimal(amount).movePointRight(2).longValueExact());
    }
}
