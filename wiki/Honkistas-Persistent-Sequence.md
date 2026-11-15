# Honkistas persistent event sequence

The owner-required sequence remains **completed gang event → admitted random rival challenge → rival victory → possible Honkistas event**. Grinions are a rare higher-ranked Honkistas variant; Lucy remains an AMYGDALA guest rather than being reassigned to Honkistas. No ordinary Boombox victory is silently substituted for the preceding rival challenge.

## Journal implementation candidate

`HonkistasEncounterData` is server-owned SavedData, separate from reward balances, return inventories and actor state. Successful street activities record a source event only **after** the existing shared reward settlement accepts it. Failed/cancelled rounds and rejected reward replays do not reach that hook. The first unresolved source is retained rather than replaced by subsequent activity wins.

The journal enforces source identity, owner identity and ordered transitions:

1. SOURCE_RECORDED
2. RIVAL_ADMITTED, with distinct rival event UUID
3. RIVAL_WON, accepting only that rival UUID
4. HONKISTAS_ADMITTED, with a distinct event UUID and explicit eligibility
5. CLOSED, retaining the last chain rather than deleting its evidence

Duplicate and out-of-order transitions return false. Reload retains chosen UUIDs; callers must not roll or spawn on a rejected transition. Unknown save schemas are preserved read-only and malformed rows are retained/quarantined, blocking changes to their owner. Closing a chain does not grant rewards or spawn another chain. Source ingestion is trusted-server-only and must be called after the existing once-only reward receipt accepts a new event, not from client packets or arbitrary historical IDs.

`saveAcknowledged` reuses the project's actual saved-data checkpoint/readback contract. Future actor admission must record its selected event and await acknowledgement before world effects. A normal `setDirty` is not a synchronous disk durability guarantee.

## What is deliberately not implemented

No random rival director, parent Honkistas encounter, actor spawn, model, music handoff or reward is added by this journal. The current first recorded source stays unresolved until an authoritative director decides to admit a rival or closes/declines the chain. No automatic surprise is claimed. Probability, cooldown, progression/defeat eligibility, terrain admission, decline/cancel UI and owned actor cleanup remain required integration work; they are not inferred from a boolean or stored stage.

No player settings, saves or worlds are migrated by the development tools. The code affects the new journal only when a build containing it is later run. Existing progression and reward settlement remain owned by their original managers.

## Verification

Two new native Forge tests cover ordered progression, duplicate/out-of-order/other-owner calls, exact NBT round trips, future schema preservation and malformed-owner quarantine. The existing real hill activity completion test now verifies the source-only hook and acknowledged disk checkpoint after reward settlement. These are added tests pending final CI, not a claim that full events are playable.


## Native actor admission boundary (new candidate)

An explicit trusted-server provider can prepare new actors, then the admission boundary verifies identity uniqueness, count≤16, native level, entity-ticking terrain, world border, collision and bounded distance. It rejects pre-existing entities rather than adopting unrelated mobs. Only after reserving the distinct rival event and acknowledging its saved data does it insert actors. Every accepted actor must be the same object returned by the native world UUID registry. Partial insertion failures clean only the new prepared bodies and close the chain without rewards or rerolls.

This is not an installed Honkistas actor provider: no production provider or automatic launch is configured. Native tests use clearly labelled ordinary pig fixtures solely to exercise insertion/checkpoint/replay behavior. They do not portray pigs as Honkistas, establish named-character asset acceptance, or implement encounter scoring.

Actual production integration still needs authored rival and Honkistas actors, source-control/defeat eligibility, invitation/decline UX, selected-provider persistence, actor ownership/reload and interruption cleanup, scoring and victory hooks, plus accessible audiovisual acceptance. A returned actor list is not a complete director.
