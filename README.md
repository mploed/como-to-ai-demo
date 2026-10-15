# ticketing-ai-ready

Demo repository for the talk **From Collaborative Modeling to AI-Ready Domain Knowledge** (KanDDDinsky 2026).

One small example, followed all the way: a decision from a modeling session ends up as something a coding agent reads **and** something the build checks.

```
modeling session decision          contexts/ticket-sales/decisions/0007-ticket-not-booking.md
        │
        ├── context map (CML)       model/transit.cml
        ├── glossary + boundary     contexts/ticket-sales/glossary.yaml, boundary.yaml
        ├── agreed examples         contexts/ticket-sales/scenarios/refund.feature
        │
        ├── read by the agent       AGENTS.md, contexts/ticket-sales/AGENTS.md (generated)
        └── checked by the build    ContextMapRulesTest     (ArchUnit rules derived from CML)
                                    GlossaryLintTest        (avoided words from the glossary)
                                    TicketRefundProperties  (invariant, jqwik)
                                    RefundScenarioTest      (agreed examples)
                                    AgentInstructionsUpToDateTest
```

Context informs. Permissions prevent. Checks detect.

## Stack

Java 21, Spring Boot 3.5, jMolecules, ArchUnit, jqwik, SnakeYAML, Maven.

```bash
mvn verify                          # everything green on main
mvn -q test-compile exec:java       # regenerate contexts/*/AGENTS.md from the model
```

## Branches for the live demo

| Branch | What the "agent" did | What catches it |
|---|---|---|
| `main` | nothing, all green | |
| `demo/agent-refund-bug` | implemented partial refunds, checks each refund against the price but forgets earlier refunds. All agreed examples still pass. | `TicketRefundProperties`: jqwik shrinks to a tiny counterexample (typically price 0.01 EUR, refunds 0.01 and 0.01) |
| `demo/agent-says-booking` | added a `BookingRefundService` | `GlossaryLintTest`, with the reason and the decision record from the modeling session |
| `demo/agent-crosses-boundary` | let access-control read the `Ticket` aggregate directly instead of the published event | `ContextMapRulesTest`, the failing test names the CML line it violates |

## 90 second demo script

1. **Model** (15 s). Open `model/transit.cml`: `TicketSales [D,ACL] <- [U,OHS] PaymentProvider`. "This line came out of a modeling session."
2. **Agent reads it** (15 s). Open `contexts/ticket-sales/AGENTS.md`. "Generated from the same model. Nobody wrote this by hand."
3. **Build checks it** (20 s). `git checkout demo/agent-crosses-boundary && mvn -q test -Dtest=ContextMapRulesTest`. The failure quotes the CML line.
4. **Language** (15 s). `git checkout demo/agent-says-booking && mvn -q test -Dtest=GlossaryLintTest`. The failure says *why* we don't say Booking, and links the decision.
5. **Invariant** (25 s). `git checkout demo/agent-refund-bug && mvn -q test -Dtest='Refund*,TicketRefund*'`. The examples pass. The property fails with a shrunk counterexample. "Wrong code? Fix the code. Wrong understanding? Fix the spec."

Fallback if the network or laptop fails: screenshots of the three failures on the slides.

## What is prototype here

* `CmlModel` is a deliberately small regex reader for the parts of CML this demo needs. A real setup would use the Context Mapper parser.
* Context Mapper already ships an ArchUnit extension that checks **tactical** elements (aggregates, entities, value objects, domain events) against CML. The strategic rules in `ContextMapRulesTest` are the bridge sketched in the talk, not an existing feature. See https://github.com/ContextMapper/context-mapper-archunit-extension
* `boundary.yaml` holds what CML cannot express yet ("not responsible for"). That is the gap discussed in the talk: extend the DSL or keep a side file.
* Package convention: context `TicketSales` lives in `com.transit.ticketsales`, its knowledge in `contexts/ticket-sales`.
