# Phonkistas: Phonk Rival Gang

> **Status: owner-approved name and requested gang; design / implementation backlog only. Not implemented or runtime-verified.** The owner selected **Phonkistas** on October 11, 2026 (UTC-06:00). Earlier naming suggestions are not alternate canon.

Tracking: [Phonkistas issue #29](https://github.com/Herbertofury/JetSetCraft/issues/29).

## Major requirement: music-driven dances and themed bosses

High-priority owner requirement: every mob needs amazing music-generated/driven dances with meaningful, fun variety and goosebump-level presentation. Cumbias, Phonkistas, Gabberina's and Hardtekkas each need exceptional signature dances. When a major faction owns a mob gang, its boss becomes a music-driven, mob-themed human leader tied to the controller; the explicit example is **Hakken Horse**, a human dressed like a horse for a Gabberina-controlled horse gang. Preserve source mobs and existing takeover/power-removal, surprise-challenge and independent-defeat rules.

See the [canonical requirement and extendable reference register](https://github.com/Herbertofury/JetSetCraft/wiki/Music%E2%80%90Driven-Dances-and-Mob%E2%80%90Themed-Bosses) and [shared high-priority issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30). Generation technology, detailed timing/rig criteria and final boss roster remain design work. This is a required quality target and backlog, not an implementation claim.

## Identity
The **Phonkistas** are the phonk-themed major competing gang in JetSetCraft, alongside Gabberina's and the Cumbias. Give them an exceptional, distinctive skating and dance identity, memorable original characters and styling, intense fair challenges, and entrances/music intros that aim for goosebumps.

Phonk is the approved genre direction. Exact substyle, lineup, character genders, visual palette, costumes, choreography and final music are still design decisions; do not impose those as owner-approved facts.

## Owner reference
[On The Dance - Slowed](https://open.spotify.com/track/4mD0L3mSySgs0EYaCKXhHn?si=a8647d56ec8e46f9), listed by Spotify under DAYVØNE1, Human Resource 404, YONCE and Rozer_rev.

Public title/artist metadata was checked; the actual audio has not been independently analyzed here. Review the reference directly before deriving tempo, choreography, instrumentation or detailed visual timing. Use it as inspiration, not as permission to extract, sample or redistribute the recording. Ship original or appropriately licensed music/assets with documented rights.

## Proposed creative direction to explore
- A confident, tightly coordinated phonk skate/dance crew whose signature movement phrases remain recognizable during play, not just in cinematics.
- Explore contrast between a restrained, ominous approach and a sharp synchronized reveal, then carry that energy into responsive, playable challenge sequences. Final pacing must follow direct music research.
- Give every member a readable role, silhouette and ability vocabulary. Use attitude, timing, footwork, line choice and group coordination rather than simply increasing damage/speed.
- Build distinct original fashion and graphic language; avoid copying performers or using the other rival gangs' animations and music with a cosmetic swap.
- Prototype a signature challenge around precision, rhythmic line changes and escalating crew call-and-response, then playtest rather than assuming one phonk substyle fits all of it.

These are proposed design explorations, not completed mechanics or audio observations.

## Shared gang-war campaign integration
Apply the established rival framework consistently while preserving each crew's identity:

1. Player, Gabberina's, Cumbias, Phonkistas and Hardtekkas compete for influence/control of normal mob gangs.
2. Source-gang control feeds readable, mixed mob-inspired ability kits. Finalize the exact mappings and compatibility before implementation.
3. **Full player takeover removes the corresponding borrowed ability from the affected rival**, giving strategic choices that break dangerous combinations. Track each rival independently; partial/contested influence and recapture restoration need explicit rules.
4. After a normal mob event is defeated/completed, an eligible rival retaining some control of that source gang may make a dramatic surprise entrance and begin a separate challenge. Preserve the normal result/rewards first.
5. When several rivals qualify, use a designed arbitration/staging rule rather than spawning overlapping events or conflicting music. Bound frequency and prevent recursive/duplicate follow-on events.
6. Give rivals persistent defeat states and define the overall campaign conclusion. Optional rematches must not silently erase wins or revive completed hostile campaigns.

### Technical and fairness constraints
Use stable gang IDs and server-authoritative control/ability/event state. Preserve source mobs' identities, inventories, ownership, provider state and normal behavior; do not replace them to obtain powers. Keep safe optional-mod fallbacks.

Telegraph hazards and combos, provide genuine counterplay and recovery windows, and avoid stun-locks or unavoidable reveal damage. Honor stripped powers even in surprise challenges. Resolve in-flight abilities and simultaneous ownership changes deterministically.

## Presentation and accessibility
Plan the full anticipation → reveal → music handoff → challenge → escalation → finale arc. Verify actual skating/dance contact, momentum, transitions and group synchronization in the game. Include short/skip options, separate audio levels, muted-audio rhythm cues and reduced motion/flashing. Emotional impact is a playtest goal, not a guaranteed reaction.

## Implementation to-do and acceptance
- [ ] Review the owner track directly and finalize phonk substyle, music direction and original visual identity.
- [ ] Finalize Phonkistas roster, names, wardrobe, silhouettes, dance/skate animation and signature challenges.
- [ ] Reconcile with existing registry, territory, event, movement, audio and compatibility architecture.
- [ ] Design multi-rival partial influence, full control, contested states, recapture and independent defeat conditions.
- [ ] Map supported mob gangs to distinct borrowed powers, combinations, tells and counters.
- [ ] Implement full takeover → affected rival ability removed, with clear player-facing feedback and preserved strategic reward.
- [ ] Build a flagship entrance/music intro and coherent escalating playable challenge/finale.
- [ ] Implement control-gated surprise events with multi-rival arbitration, bounded pacing and no duplicate/recursive triggers.
- [ ] Preserve normal-event rewards/results through encore losses, exits and interruptions.
- [ ] Verify exact-build single-player, dedicated-server and multiplayer behavior: save/reload, restart, disconnect/rejoin, chunk unload and simultaneous ownership changes.
- [ ] Verify removed abilities and defeated-rival/campaign states persist; optional replay preserves completed outcomes.
- [ ] Profile and improve client/server performance, memory, network traffic and startup/loading in parallel without capability loss or regressions.
- [ ] Validate compatibility, original-mob ownership and existing movement/combat/event behavior.
- [ ] Validate accessibility, audio cleanup, replay pacing and native playtest feedback for intensity, fun and distinct identity.
- [ ] Record original/licensed asset and music provenance.
- [ ] Attach native evidence before checking implementation tasks or changing release claims.

Related designs: [Gabberina's](https://github.com/Herbertofury/JetSetCraft/wiki/Gabberinas%E2%80%90and%E2%80%90Gang%E2%80%90War%E2%80%90Campaign) · [Cumbias](https://github.com/Herbertofury/JetSetCraft/wiki/Cumbias%E2%80%90and%E2%80%90Multi%E2%80%90Rival%E2%80%90Gang%E2%80%90War) · [Gang Wars master specification](Gang-Wars-Boombox-and-Mob-Atlas) · [Roadmap](Roadmap).

## Four-rival campaign and reactive music update

[Hardtekkas](Hardtekkas) is now the fourth major rival, with reputation-unlocked **Tekkistas** as an optional name, tracked in [issue #31](https://github.com/Herbertofury/JetSetCraft/issues/31). The [multi-rival campaign](Cumbias‐and‐Multi‐Rival‐Gang‐War) must reconcile all four crews' control, source-power removal, ownership-aware bosses, surprise arbitration and independent persistent defeats. Gabberina's keeps its explicit jumpstyle/gabber/hardtekk blend and Hardtekk Tutorial reference.

The owner's major [reactive fight/trick-chain music and combo payoff goal](Music‐Driven-Dances-and-Mob‐Themed-Bosses#major-requirement-reactive-fight-trick-chain-and-combo-music) is tracked with solo/crew choreography in [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30). Apply the Killer Instinct-inspired level of musical interaction through original faction-specific audio, responsive combat/trick-chain feedback and memorable announcer-style payoffs; never delay controls/hits to match music, copy reference recordings, or introduce arbitrary feature/participation caps. Proposed technical criteria and tests live in the canonical specification. Design/backlog only.
