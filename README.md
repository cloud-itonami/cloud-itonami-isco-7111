# cloud-itonami-isco-7111

Open Occupation Blueprint for **ISCO-08 7111**: House Builders.

This repository designs a forkable OSS business for a house-building job-site scheduling and logistics coordination practice: a job-site scheduling and supply-coordination robot manages crew/task records under a governor-gated actor, so a house-building crew keeps its own operating records instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/housebuilder/` implements the
`HouseBuilderActor` as a `langgraph.graph/state-graph`
(`housebuilder.actor`) wired to a `House Builder Advisor`
(`housebuilder.advisor`) and an independent `HouseBuilderGovernor`
(`housebuilder.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 21 tests / 45 assertions green (`kbb -M:test`).
HARD invariants (always hold, never overridable): builder provenance,
site provenance, no-actuation (`:effect` must be `:propose`), a closed
op-allowlist (`:log-work-record`, `:schedule-crew-operation`,
`:flag-safety-concern`, `:coordinate-supply-order` — nothing else may
ever be proposed), and a permanent, unconditional block on any
proposal that would directly finalize a structural-work-execution
decision (e.g. deciding to proceed with a specific framing or
foundation step) or override a site safety officer's or foreman's
judgment. Always-escalate paths (human sign-off regardless of
confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a job-site scheduling/logistics coordination robot performs crew scheduling, task/materials-usage/progress-record logging and building-materials supply-order coordination for a house-building crew, under an actor that proposes actions and an independent **House Builder Governor** that gates them. The governor never
dispatches hardware itself, never performs construction work on the job site, and never finalizes a structural-work-execution decision or overrides a site safety officer's/foreman's judgment; `:high`/`:safety-critical` actions (such as a flagged structural-integrity/site-hazard/crew-fatigue concern, or an above-threshold supply order) require human sign-off. **This actor coordinates job-site scheduling/logistics only — it never performs construction work itself.**

## Core Contract

```text
crew roster + job-site registration + safety-reporting policy
        |
        v
House Builder Advisor -> House Builder Governor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
a structural-work-execution decision, override a site safety officer's or
foreman's judgment, suppress an operating record, or disclose sensitive data
without governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7111`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
