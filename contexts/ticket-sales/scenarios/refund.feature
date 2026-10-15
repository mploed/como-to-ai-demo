# Agreed examples. Read only for agents: propose new scenarios, never edit agreed ones.
# This file IS the test: RefundScenarioTest runs it through Cucumber, step definitions in RefundSteps.
# Change a number here and the build checks the new number.

Feature: Refunds

  Scenario: A refund never deletes a Ticket
    Given a Ticket for zone AB, bought on 3 March for 4.00 EUR, not validated
    When the customer requests a refund of 4.00 EUR
    Then the Ticket is marked Refunded
    And TicketRefunded is published

  Scenario: Partial refunds add up
    Given a Ticket for zone AB, bought on 3 March for 4.00 EUR, not validated
    When the customer requests a refund of 1.50 EUR
    And the customer requests another refund of 1.50 EUR
    Then the Ticket is marked Partially Refunded
    And the total refunded amount is 3.00 EUR

  Scenario: A validated Ticket cannot be refunded
    Given a Ticket for zone AB, bought on 3 March for 4.00 EUR, validated at a gate
    When the customer requests a refund of 1.00 EUR
    Then the refund is rejected

  # Invariant (Ticket aggregate): the sum of refunds never exceeds the price paid.
  # Explored across many generated cases by TicketRefundProperties (owned by the engineers).

  # Policy across contexts (not an invariant of Ticket):
  # A fully refunded Ticket no longer grants access. -> TicketRefunded event, consumed by access-control.
  # Open questions for the experts: how long may the delay be? What happens at an offline gate?
