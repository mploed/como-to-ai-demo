# Agent instructions: transit ticketing

This repository is split along Bounded Contexts, not technical layers.
Before you change anything, find out which context the request belongs to.

| Context | Package | Instructions |
|---|---|---|
| TicketSales | `com.transit.ticketsales` | `contexts/ticket-sales/AGENTS.md` |
| AccessControl | `com.transit.accesscontrol` | none yet: ask the owning team |
| CustomerCare | `com.transit.customercare` | none yet: ask the owning team |
| PaymentProvider | `com.transit.paymentprovider` | external system, never change |

The word "ticket" means different things here: a fare product (TicketSales), a validation at a gate (AccessControl), a support case (CustomerCare).
If a request says "ticket" and the context is not obvious, ask first.

Source of truth for relationships between contexts: `model/transit.cml`.
Context instructions are generated from it. Never edit a generated `AGENTS.md` by hand, change the model and regenerate.

Run `mvn verify` before you call a task done. It checks the context map, the language and the invariants.
