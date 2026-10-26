# Gang Profile and Reference Standard

[Mob Gangs](Mob-Gangs) · [Vanilla roster](Vanilla-Mob-Gangs) · [Modded roster](Modded-Mob-Gangs)

**Owner requirement:** every individual mob gang gets its own music reference, dance reference, eventual tagging art, quirks, abilities, unlocks and memorable gameplay identity. A shared family is organization, not a substitute for an individual profile.

## Required profile fields
| Field | What must be recorded |
| --- | --- |
| Identity | Stable gang_id; canonical name; aliases; source entity ID; provider/version; family; adult/junior/boss/member relationships; authored/wired/tested status |
| Music references | Exact track/version and artist; playable source URL; entrance/build/drop/challenge/chill/victory role; timestamp/cue; approval and production-use status; what makes it fit this creature |
| Dance references | Creator and exact video URL; relevant timestamps; named steps/style where verified; signature movement; on-foot and skating adaptation; solo, paired and crew variants; anatomical constraints |
| Tagging art | Bespoke creature-linked motif, handstyle, palette, silhouette; Mark → Throwie → Masterpiece; source/art references and credits; delivered asset paths and preview when created |
| Personality and quirks | Source behavior versus authored personality; entrance, taunts, reactions, victories, failures, junior variants and interactions with other gangs |
| Abilities | Source-owned ability versus proposed JetSetCraft technique; tells, wind-up, resource/cooldown conditions, range, counterplay, environment interactions, friendly-fire/world-safety rules |
| Challenges | Signature modes, routes/obstacles, difficulty progression, team roles and tactics; underwater/flight/ground requirements; fair defeat and rematch rules |
| Rewards and unlocks | Exact reward and progression condition; reputation/naming rights; technique/gear/dance/music/tag/skin rewards; recruitable named members and challenger-screen duel requirements; persistence and duplicate-reward protection |
| Rival control | Which abilities a controlling major gang borrows; full-takeover removal; partial-influence surprise eligibility; controller-specific dance/music and human boss variant |
| Compatibility proof | Model/ride rig, AI/state ownership, pet/boss exceptions, provider removal/upgrades, multiplayer and restoration evidence; measured startup/frame/tick cost |

## Reference records must be useful
For each music/video/art source retain: title, creator, exact URL, version, timestamp range, intended use, reason for fit, review state, approval source, provenance/permission status and implementation asset once one exists. Do not imply a link has been watched/heard if it has not. Never fabricate BPM, timing markers, creator identity or approval.

Use separate states: **needed → candidate → reviewed → owner-approved → integrated → exact-build verified**. Track licensing/permission alongside creative approval rather than assuming one field proves the other. Keep supplied permissions scoped to the actual source/material. References and playable assets are distinct records.

A source creature does not inherit an approved song simply because its controller's playlist contains it. Record the explicit per-mob assignment or mark that assignment proposed. Shared tracks are allowed when intentionally chosen, but the choreography, cue usage and presentation must still distinguish the creature.

## Unlocks must be playable, not vague
Specify both the reward and how the player earns it. Existing design supports reputation-based naming and art progression, plus high-standing challenger encounters for recruiting memorable members. Numeric thresholds remain configurable/design-pending until approved; do not invent final values.

A recruitment record needs: eligible reputation/level state; challenger presentation; encounter rules; win/lose/rematch behavior; recruit receipt; team-slot/availability behavior; costume/skin unlocks; save/reconnect handling. Losing should not destroy the member or silently create a permanent collection dead end.

## Profile completion checks
- [ ] Exact entity/provider identity and retained canonical name are correct.
- [ ] Individual music and dance references are reviewed; pending fields remain visibly pending.
- [ ] Quirks and choreography suit the creature's anatomy and source behavior.
- [ ] Ability tells/counterplay, signature challenges and concrete rewards are specified.
- [ ] Tagging art has a bespoke brief, then real verified assets rather than a placeholder badge.
- [ ] Reputation, recruitment, aliases, rival control and unlock receipts survive save/reload and multiplayer.
- [ ] Original mob AI/owner/boss state is restored on win, loss, cancel, disconnect, unload and recovery.
- [ ] Exact-build visuals/gameplay/performance evidence exists before calling the whole profile finished.

## Current roster defaults
The initial roster pages preserve existing authored names and hooks. Unless an entry has an explicit approved reference or asset record, its individual music/dance assignment and finished art remain **pending**. This shared rule avoids hundreds of repeated empty template paragraphs while keeping every mob's missing work visible. Expand entries into individual pages when real references, art or mechanics are available; link those pages from the same canonical roster.

