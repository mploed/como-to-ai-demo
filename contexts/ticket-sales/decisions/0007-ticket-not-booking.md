# 0007: We say Ticket, not Booking

Status: agreed in the modeling session on refunds (mob modeling, fare product team, gate team, customer care)

## Context
Three words were used for the same thing in the workshop: ticket, booking, order.

## Decision
In ticket-sales we say **Ticket** for the fare product a customer has purchased and paid for.

## Rejected alternatives
- **Booking**: booking means a seat reservation in long distance rail. Our customers do not reserve seats.
- **Order**: the order is the checkout process, not the product the customer holds afterwards.
- **Cancellation** for money paid back: nothing gets cancelled. The Ticket stays, it gets the state Refunded.

## Consequences
The glossary lint fails the build when these words appear in ticket-sales code.
