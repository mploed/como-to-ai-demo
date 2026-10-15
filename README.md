# ticketing-ai-ready

Demo repository for the talk **From Collaborative Modeling to AI-Ready Domain Knowledge** (KanDDDinsky 2026).

One small example, followed all the way: a decision from a modeling session ends up as something a coding agent reads, something that limits what the agent may touch, and something the build checks.

```
modeling session decision          contexts/ticket-sales/decisions/0007-ticket-not-booking.md
        │
        ├── context map (CML)       model/transit.cml
        ├── glossary + boundary     contexts/ticket-sales/glossary.yaml, boundary.yaml
        ├── agreed examples         contexts/ticket-sales/scenarios/refund.feature
        │
        ├── INFORM     the agent reads      AGENTS.md, contexts/ticket-sales/AGENTS.md (generated)
        │                                   PromptLint: asks back before anything gets built
        ├── CONSTRAIN  the agent may not    .claude/settings.json (generated), .github/CODEOWNERS
        └── VERIFY     the build checks     RefundScenarioTest      runs refund.feature itself (Cucumber)
                                            TicketRefundProperties  the invariant (jqwik)
                                            ContextMapRulesTest     ArchUnit rules derived from CML
                                            GlossaryLintTest        avoided words from the glossary
                                            AgentInstructionsUpToDateTest, PromptLintTest
```

Context informs. Permissions prevent. Checks detect.

## Stack

Java 21, Spring Boot 3.5, jMolecules, ArchUnit, jqwik, Cucumber, SnakeYAML, Maven.

```bash
mvn verify                                        # everything green on main
mvn -q test-compile exec:java@agents              # regenerate AGENTS.md and .claude/settings.json from the model
mvn -q test-compile exec:java@prompt-lint -Dexec.args="Implement ticket cancellation for the mobile app"
```

The prompt lint on the opening prompt of the talk prints:

```
> Implement ticket cancellation for the mobile app
? 'Ticket' in which context? Ticket (ticket-sales), Validation (access-control), Support Case (customer-care).
? 'Cancellation' is not a term in ticket-sales. Do you mean Refund? (nothing gets cancelled, ...)
```

## Branches for the live demo

These are **prepared regressions of the kind coding agents produce**, written by hand for a reliable demo. They are not recordings of an agent run.

| Branch | The agent like change | What catches it |
|---|---|---|
| `main` | nothing, all green | |
| `demo/agent-refund-bug` | partial refunds, each refund checked against the price, earlier refunds forgotten. **All agreed examples in `refund.feature` still pass.** | `TicketRefundProperties`: jqwik shrinks to a tiny counterexample, typically price 0.01 EUR, refunds 0.01 and 0.01 |
| `demo/agent-says-booking` | adds a `BookingRefundService` | `GlossaryLintTest`, with the reason and the decision record from the modeling session |
| `demo/agent-crosses-boundary` | access-control reads the `Ticket` aggregate directly instead of the published event | `ContextMapRulesTest`, the failing test names the CML line it violates. With the generated permissions, a ticket-sales agent could not even write into access-control: prevent first, detect when it comes from elsewhere |

## 90 second demo script

1. **Model** (15 s). `model/transit.cml`: `TicketSales [D,ACL] <- [U,OHS] PaymentProvider`. "This line came out of a modeling session."
2. **Agent reads it** (15 s). `contexts/ticket-sales/AGENTS.md` and `.claude/settings.json`. "Generated from the same model. Nobody wrote this by hand."
3. **Build checks it** (20 s). `git checkout demo/agent-crosses-boundary && mvn -q test -Dtest=ContextMapRulesTest`. The failure quotes the CML line.
4. **Language** (15 s). `git checkout demo/agent-says-booking && mvn -q test -Dtest=GlossaryLintTest`. The failure says *why* we don't say Booking, and links the decision.
5. **Invariant** (25 s). `git checkout demo/agent-refund-bug && mvn -q test -Dtest='RefundScenarioTest,TicketRefundProperties'`. The expert's examples pass. The property fails with a shrunk counterexample. "Wrong code? Fix the code. Wrong understanding? Fix the spec."

Fallback if the network or laptop fails: screenshots of the three failures on the slides.

## What is prototype here, and what is out of scope

* **CML reader.** `CmlModel` is a deliberately small regex reader for the parts of CML this demo needs. It is not validated against the Context Mapper parser. A real setup would use the Context Mapper tooling.
* **Strategic rules.** Context Mapper already ships an ArchUnit extension that checks **tactical** elements (aggregates, entities, value objects, domain events) against CML: https://github.com/ContextMapper/context-mapper-archunit-extension. The strategic rules in `ContextMapRulesTest` are the bridge sketched in the talk, not an existing feature.
* **Not on the map.** Contexts without a relationship on the map may not depend on each other. That is a closed world convention of this repository, a sensible default for agents. It is not the DDD meaning of Separate Ways: Separate Ways is a deliberate decision and deserves its own decision record.
* **Permissions.** `.claude/settings.json` is an example in Claude Code's format, generated for the ticket-sales team's agents. Other agents have similar mechanisms. These rules cover the agent's own file tools; for OS level enforcement use a sandbox. `CODEOWNERS` covers what gets merged, with placeholder team handles.
* **Invariant.** The invariant is written as a sentence next to the scenarios and implemented as a property test by the engineers. The sentence is not parsed.
* **Linting.** `GlossaryLint` and `PromptLint` are token based, not semantic and not AST based. Expect the occasional false positive.
* **Out of scope.** The refund flow uses an in memory repository and an in process event. Idempotency, transactions, reliable event delivery, retries and the gate to ticket-sales validation feed are not modeled.
* **Gap in CML.** `boundary.yaml` holds what CML cannot express yet ("not responsible for"). That is the gap discussed in the talk: extend the DSL or keep a side file.
* **Package convention.** Context `TicketSales` lives in `com.transit.ticketsales`, its knowledge in `contexts/ticket-sales`.
