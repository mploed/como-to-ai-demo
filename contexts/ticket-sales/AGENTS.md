<!-- GENERATED from model/transit.cml, contexts/ticket-sales/glossary.yaml and boundary.yaml.
     Do not edit by hand. Regenerate with: mvn -q test-compile exec:java -->

# Agent instructions: TicketSales

Sells fare products, handles refunds

- Subdomain: FareSales (CORE_DOMAIN)
- Code: `com.transit.ticketsales`
- Owner of the language: Fare Product Team

## Responsible for

- Selling fare products
- Refunds

## Not responsible for

- Gate validation (access-control)
- Support cases (customer-care)
- Payment processing (payment-provider, only via the anticorruption layer)

## Language

- **Ticket**: A fare product a customer has purchased and paid for. Not to be confused with: Validation (access-control), Support Case (customer-care). Never say: Booking, Order, Voucher.
- **Refund**: Money paid back to the customer for a Ticket. A Ticket can be refunded partially, several times. Never say: Cancellation.

## Policies from the context map

- `TicketSales [U,OHS,PL] -> [D] AccessControl`: AccessControl depends on you. Your published language is `com.transit.ticketsales.api`. Changing it needs the AccessControl owner. Never depend on AccessControl. [permission, build]
- `TicketSales [U,OHS,PL] -> [D,CF] CustomerCare`: CustomerCare depends on you. Your published language is `com.transit.ticketsales.api`. Changing it needs the CustomerCare owner. Never depend on CustomerCare. [permission, build]
- `TicketSales [D,ACL] <- [U,OHS] PaymentProvider`: You depend on PaymentProvider. Only `com.transit.ticketsales..acl..` may use PaymentProvider. Translate into your own language there, nothing behind the anticorruption layer sees PaymentProvider types. [build] Use only its published API `com.transit.paymentprovider.api`. [build]

## How to work here

- First name the Bounded Context of the request. If the request is ambiguous ("ticket"), ask before you do anything else.
- Examples first: draft scenarios and invariants for one slice and wait until the domain expert agrees.
- Agreed scenarios in `contexts/ticket-sales/scenarios` are read only. Propose new ones, never edit agreed ones.
- Unknown term or uncovered case: do not invent. Add a question to `contexts/ticket-sales/open-questions.md` and continue with the rest.
- The build checks these rules: `GlossaryLintTest`, `ContextMapRulesTest`, `TicketRefundProperties`, `RefundScenarioTest`.
