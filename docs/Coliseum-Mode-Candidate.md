# Coliseum: recruited gangs versus recruited gangs

Owner request, 2026-11-16: choose gangs already owned/recruited after defeating them, pit them against each other, and watch exceptionally entertaining battles. This is a spectator-first mode, not another player combat gauntlet. The lineup preview below is a bounded implementation candidate, **not a playable coliseum**.

## Inspiration and original scope

Owner reference: [Followers vs Followers by SethSaidThat](https://modholdmods.com/g/skyrim-se/mods/203/followers-vs-followers), reviewed 2026-11-16, listing v1.0.1. It describes a spectator pit with staged follower teams, protected original companions and timed encounters. These are design references only: no code, art, arena, audio or other files were downloaded or copied; no reuse license was inferred. Its permadeath and wagering choices are not automatically JetSetCraft requirements.

JetSetCraft should preserve each gang's identity, musical style, distinctive moves, relationships and earned recruitment. The player chooses rival lineups, views understandable strengths and weaknesses, starts the bout and watches from a safe viewing area. The entertainment target includes readable openings, team tactics, counters, momentum shifts, knockout reactions, dramatic last-member moments and a concise replayable result summary. Use actual combat events for camera/announcer/music cues, never predeclared winners or invented combat logs. Reduced-motion/flash controls, optional commentary and an unobstructed free camera belong in acceptance.

## Existing architecture and honest boundary

`CrewProfile` is the durable owner-scoped virtual mission roster. It is not a collection of physical follower entities. `RushAlbum` records individually defeated actor/species and gang pairs. A positive card is **not proof that the entire gang was conquered**. Existing broader Gang Wars and terrain-aware arena plans remain in place; this mode must reuse shared identity, combat, music and recovery components rather than rebuild them.

The new read-only `ColiseumLineup.preview` derives roster and exact defeat keys from the requesting server player. It rejects blocked recovery records, wrong owner, stale revision, missing/duplicate members, busy recruits, mixed-gang sides, same-gang opposition, unrecorded actor/gang defeats and teams outside the provisional 1–6 per-side budget. Returned snapshots are immutable; no roster, mission, supplies, collection, entity or player state is changed. `PREVIEW_ELIGIBLE` means only this policy passed, never that an actor provider exists or a bout can start. The pure `evaluate` seam is for deterministic tests and cannot authorize spawning.

Whole-gang defeat/unlock semantics still need a production earned-victory receipt and agreement with the wider conquest design. Do not quietly replace that rule with album ownership. Named character profiles also need their actual licensed actor adapter and combat kit; a generic mob or clone is not acceptable proof. Current draft Honkistas mechanics are independent, not merged prerequisites or finished fighters.

## Delivery gates

1. **Preview policy candidate:** native tests for owner/revision/busy/duplicate/earned restrictions and read-only exact lookup. Fixture records are labeled, not genuine player wins.
2. **Earned access:** resolve authoritative whole-gang victory/recruitment receipts, migration and loss/recovery semantics. Revalidate on actual match start; reject stale previews. Reserve participants atomically against missions and concurrent bouts.
3. **Real actor admission:** provider-owned fighting representations with provenance, gear/combat abilities and bounds. Do not mutate originals' health, inventory, ownership, tame state, quest state or durable roster. Establish copy/representation policy explicitly; fail closed if unsupported.
4. **Bounded battle lifecycle:** separate arena allocation; fixed maximum duration; real team targeting/damage rules; knockout accounting; timeout/draw/cancel; disconnect, dimension change, restart and crash cleanup. Start with a native 1-v-1, then 2-v-2 before raising budgets. No fake winner, forced kill or disposable stand-in accepted as character combat.
5. **Spectator experience:** real client lineup UI, safe entry/return, live health/team status, agency-preserving camera, evidence-driven highlights and music. Preserve normal settings, inventory and location. No real-money betting or irreversible follower death by default.
6. **Native acceptance:** both teams actually engage, target only admitted opponents, finish naturally or time out, leave no actors/tickets/terrain damage, retain original roster and saved state. Test unknown provider, partial spawn failure, concurrent reservation, empty roster, stale state, unload/reload, cancellation, repeated matches and maximum stress. Inspect actual video/captures before claiming fun, visual quality or playability.

This draft contains no battle director, arena, UI, rewards, betting, model imports or deployment. Existing source repositories and active drafts remain unchanged.
