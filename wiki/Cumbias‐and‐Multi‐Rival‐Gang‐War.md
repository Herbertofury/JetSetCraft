# The Cumbias: Mexican Cumbia Rival Gang

> **Status: owner-requested design / implementation backlog. Not implemented or runtime-verified.** Requested October 11, 2026 (UTC-06:00). Preserve all existing released features and compatibility.

Tracking: [Cumbias issue #28](https://github.com/Herbertofury/JetSetCraft/issues/28) · [Gabberina campaign issue #27](https://github.com/Herbertofury/JetSetCraft/issues/27)

## Major requirement: music-driven dances and themed bosses

High-priority owner requirement: every mob needs amazing music-generated/driven dances with meaningful, fun variety and goosebump-level presentation. Cumbias, Phonkistas, Gabberina's and Hardtekkas each need exceptional signature dances. When a major faction owns a mob gang, its boss becomes a music-driven, mob-themed human leader tied to the controller; the explicit example is **Hakken Horse**, a human dressed like a horse for a Gabberina-controlled horse gang. Preserve source mobs and existing takeover/power-removal, surprise-challenge and independent-defeat rules.

See the [canonical requirement and extendable reference register](https://github.com/Herbertofury/JetSetCraft/wiki/Music%E2%80%90Driven-Dances-and-Mob%E2%80%90Themed-Bosses) and [shared high-priority issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30). Generation technology, detailed timing/rig criteria and final boss roster remain design work. This is a required quality target and backlog, not an implementation claim.

## Identity and experience
**The Cumbias** are another major competing gang alongside Gabberina's, centered on an amazing **Mexican cumbia** identity. Give them their own elite skating, dancing, music, fashion, crew personality and challenging events. They should feel like a fully realized rival, not a Gabberina reskin.

The existing quality direction applies: entrances, musical reveals and playable challenges should aim for goosebumps, with distinctive timing and choreography. Exact roster, character genders, names, substyle, costumes and final music remain design decisions rather than invented owner requirements.

### Proposed creative direction
- Express the musical groove through fluid skating footwork, weight shifts, turns, formation exchanges and call-and-response challenge phrases. Ground final choreography in direct reference research and skilled cumbia dance interpretation.
- Build an original contemporary Mexican cumbia street-performance identity through considered fashion, typography, color, animation and staging. Research the selected scene/substyle; avoid flattening distinct traditions into generic stereotypes.
- Explore a keyboard-duel-inspired challenge as one option suggested by the supplied track title: rival phrases escalate into a spectacular playable answer-and-response finale. This is a proposal, not a claim that the reference audio has been analyzed.
- Contrast and complement Gabberina's intense gabber identity through a distinctive musical and movement language, while retaining boss-level mastery, fair counterplay and serious competitive stakes.

### Owner dance-video collection

Latest addition: https://www.tiktok.com/@luxurychoreograph/video/7695485710962380046 . Visual/audio review remains pending after read-only retrieval failed.

**Shared choreography requirement:** players and enemies can emote/dance solo, have teammates join synchronized crew choreography, and perform gang-versus-gang head-to-head routines both while skating and while dancing normally on foot. Preserve the music-generated/driven direction. The [canonical system specification](https://github.com/Herbertofury/JetSetCraft/wiki/Music%E2%80%90Driven-Dances-and-Mob%E2%80%90Themed-Bosses) and [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30) hold the proposed join/leave, formation, input-agency, interruption, multiplayer and performance criteria. No implementation is claimed.

The owner supplied four Cumbias dance videos, tracked as **CUM-DANCE-001**, **CUM-DANCE-002**, **CUM-DANCE-003** and **CUM-DANCE-004** in the [extendable reference register](https://github.com/Herbertofury/JetSetCraft/wiki/Music%E2%80%90Driven-Dances-and-Mob%E2%80%90Themed-Bosses). All original URLs are preserved there; each was attempted read-only and remains pending visual/audio review. No footwork or music observations are claimed from inaccessible clips. Add future references by faction/mob/style while preserving the earlier Spotify references below.

## Owner music references
- [Sin Miedo — MUNDIAL DE LOS TECLADISTAS: Luciano Creiner vs Luvi (Octavos De Final)](https://open.spotify.com/track/6VUMSxWQiL6Sj5zbl9nzBX?si=6dd0b78b44b24991)
- [Owner-supplied Cumbias playlist](https://open.spotify.com/playlist/5A1Y4grAfGIK6W1aolWKAm?si=deb5fc48e0c541dc)

The track title/artist was checked against Spotify's public page. The playlist link is preserved exactly; its contents and the audio of either reference were not independently reviewed here. References guide research and mood; they do not grant music extraction, sampling or redistribution rights. Use original music or licenses covering the actual shipped use.

## Role in the multi-rival gang-control campaign
The owner requested a competing gang like Gabberina's. Integrate the Cumbias into the shared gang-war framework rather than a disconnected encounter list. The following shared-system applications are proposed design details to finalize:

1. The player, Gabberina's, Cumbias, Phonkistas and Hardtekkas compete for influence/control of normal mob gangs.
2. Capture, defend, reclaim and counterattack choices change the campaign and the rivals' available mob-inspired ability combinations.
3. Apply the established control-to-power principle consistently: fully taking a source gang away from a rival removes that source gang's corresponding borrowed ability from that rival. Track each rival separately; weakening one must not silently weaken or strengthen another.
4. Keep mixed ability kits distinctive and readable through crew roles, telegraphs, cooldowns, recovery windows and deliberate combinations. Do not just assign all rival gangs identical powers with different colors.
5. Provide meaningful rivalry progression and lasting defeat states. Define whether each rival has its own finale and how the overall campaign resolves; defeating one must not erase progress against the other.

### Ownership model questions to resolve
- Define partial influence versus full control, whether influence can be shared, and how multiple rivals contest one source gang.
- Define the exact full-takeover threshold, current ability availability and any restoration after full rival recapture.
- Show gang-control and borrowed-power source indicators clearly so players can plan how to dismantle combinations.
- Use stable gang IDs, server-authoritative state, bounded updates and reliable saves. Never replace provider-owned mobs or erase their original identity, inventories, ownership or AI state.

## Surprise post-event challenges
Use the established surprise-appearance framework as a shared campaign feature: after a normal mob event resolves, a rival with some control of that source gang may make a spectacular entrance and launch a separate challenge.

For the Cumbias, give the reveal its own cumbia music handoff, performance staging, dance/skate entrance and challenge identity. Detailed eligibility and probability remain to be tuned.

- Resolve and preserve the original event result and rewards first.
- If multiple rival gangs qualify, choose or explicitly stage the outcome through a defined arbitration rule. Do not accidentally start overlapping events or competing audio.
- Full player takeover must remove obsolete eligibility and associated borrowed powers; use current control state.
- Keep the surprise special with fair frequency and clean participation/exit rules. Prevent duplicate callbacks and recursive encore chains.
- Respect defeated-rival and completed-campaign states. Optional rematches must not silently undo a victory.

## Presentation and gameplay acceptance
- A distinctive anticipation → reveal → musical arrival → playable challenge → escalating finale arc.
- Skating and dance physically fit the actual rig and terrain: foot contact, momentum, turn transitions, grind/air recovery and synchronized crew motion.
- Intense but readable pressure with meaningful route, rhythm and ability counters. No permanent stun-locks or unavoidable reveal damage.
- Good replay pacing, short/skip options, visual rhythm cues, separate audio controls and reduced-motion/flashing alternatives.
- Music transitions cannot layer conflicting tracks or strand audio after cancellation, disconnect or chunk unload.

## Implementation to-do
- [ ] Review both owner references directly and define the specific Mexican cumbia musical/dance/art direction.
- [ ] Finalize original crew roster, visual identity, wardrobe, animations and challenge personalities.
- [ ] Reconcile with current gang registry, territory, event, movement, music and compatibility architecture.
- [ ] Define player/Gabberina/Cumbias/Phonkistas/Hardtekkas control, shared influence, contested states and independent victory progression.
- [ ] Map supported source gangs to borrowed powers and distinct Cumbias ability combinations.
- [ ] Implement full takeover → relevant rival power removal, including possible recapture restoration and in-flight ability transitions.
- [ ] Prototype distinctive cumbia skate/dance challenges; evaluate the keyboard-duel/call-and-response concept.
- [ ] Storyboard and build a flagship entrance, music intro, escalating challenge and finale.
- [ ] Implement surprise post-event appearance eligibility and multi-rival arbitration without overlap or recursive triggers.
- [ ] Preserve original event rewards/results through rival challenge defeat, exit and interruption.
- [ ] Verify single-player, dedicated server and multiplayer save/reload, restart, simultaneous ownership changes, disconnect/rejoin and chunk unload.
- [ ] Verify per-rival defeat and final campaign completion persist and optional replay preserves the completed outcome.
- [ ] Profile AI, animation, audio, effects, networking, memory and startup/loading; improve performance in parallel without capability loss or regressions.
- [ ] Verify optional-provider fallbacks, source-mob ownership and existing movement/combat/gang behavior.
- [ ] Validate accessibility modes and native playtest for fun, intensity, identity and emotional impact.
- [ ] Document original/licensed asset and music provenance before shipping.
- [ ] Attach exact-build native evidence before marking implementation items complete or changing release claims.

See also: [Gabberina's campaign](https://github.com/Herbertofury/JetSetCraft/wiki/Gabberinas%E2%80%90and%E2%80%90Gang%E2%80%90War%E2%80%90Campaign) · [Gang Wars master specification](Gang-Wars-Boombox-and-Mob-Atlas) · [Territory & Reputation](Hangouts-Territory-and-Reputation) · [Roadmap](Roadmap).

## Additional rival: Phonkistas

The owner selected **Phonkistas** for the phonk gang. See the [full Phonkistas design and to-do list](https://github.com/Herbertofury/JetSetCraft/wiki/Phonkistas) and [issue #29](https://github.com/Herbertofury/JetSetCraft/issues/29). The campaign now includes Gabberina's, Cumbias, Phonkistas and Hardtekkas as major competing crews. Reconcile rival-specific control/powers, shared-source influence, surprise-event arbitration and independent persistent defeat. Preserve each crew's distinct music and dance/skate identity. This expands the design backlog; no implementation is claimed.

## Fourth rival: Hardtekkas and optional Tekkistas name

The owner added [Hardtekkas](Hardtekkas), a separate hardtekk gang, with **Tekkistas** as an optional reputation-unlocked name through existing Friendly/Crew Naming Rights. See [issue #31](https://github.com/Herbertofury/JetSetCraft/issues/31) for the faction-specific checklist and all three owner music references. Keep stable identity, personal/shared alias permissions and canonical reset; no numeric threshold or irreversible faction replacement is introduced. Gabberina's retains its jumpstyle/gabber/hardtekk blend and Hardtekk Tutorial.

The current campaign roster is **Gabberina's, Cumbias, Phonkistas and Hardtekkas**, plus the player and source mob gangs. Reconcile four-rival influence/control, affected-rival source-power removal on full takeover, independent defeat and surprise-event arbitration. Ownership-aware mob-themed human bosses and solo-to-crew/head-to-head dance/skating apply to Hardtekkas as well. Preserve original event rewards, source mobs, existing features and performance/startup quality.

- [ ] Extend shared influence/control, power-removal, boss selection and independent defeat tests to all four rivals, including simultaneous changes and save/reload.
- [ ] Test multi-rival surprise eligibility and one clean event/music handoff, with no duplicate/recursive encores or revived defeated factions.
- [ ] Integrate the major [reactive fight/trick-chain music and combo payoff goal](Music‐Driven-Dances-and-Mob‐Themed-Bosses#major-requirement-reactive-fight-trick-chain-and-combo-music) through [shared issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30): musical escalation and original announcer-style excitement without delaying controls/hits, incompatible layered tracks, copied recordings or arbitrary feature/participation caps.

Design/backlog only; detailed mechanics and native verification remain pending.
