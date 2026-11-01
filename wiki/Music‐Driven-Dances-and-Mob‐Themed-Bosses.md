# Music-Driven Dances and Mob-Themed Bosses

**Priority: major, top-level acceptance requirement.** This is owner-requested design and implementation backlog, recorded October 11, 2026 (UTC-06:00). No new choreography, generator, boss, or gameplay implementation is claimed by this documentation update.

**Shared tracking:** [High-priority issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30).

## Current MoccaSins identity and music register

The owner finalized **MoccaSins** as the canonical name on October 13, 2026. **Thundersteps, Drumrunners, Neon Shawls, Rhythm Daughters and Powwow Dreamers** are unlockable alternative names for this same crew; they are not shortlist candidates or new factions. Preserve progression and stable identity. Exact unlock gates remain unspecified.

The [MoccaSins register](MoccaSins) contains all **17 owner-selected tracks**, including the thirteen latest additions. Preserve the requested chill sequence **Atausiq – One → Ahiani: Elsewhere**; **Rod Crowhop** belongs to the normal soundtrack. Other chill tracks: Tribes, Savanna Sultana, and O i Og i O. Music implementation/listening verification remains backlog work, not a completed feature. This canonical name supersedes older Thundersteps base-name references throughout earlier design records.

## Required experience

1. **Every mob gets amazing, music-driven dances with fun, meaningful variety.** The music must generate or drive the choreography, with memorable entrances, dances and challenge moments that aim to give players goosebumps. A single repeating generic loop or the same routine recolored across mobs does not meet the brief.
2. **Cumbias, Phonkistas, Gabberina's and Hardtekkas each receive outstanding signature dances.** Their musical and movement identities must be recognizable and exceptional, alongside the all-mob quality bar. Carry the spectacle through actual gameplay and repeat encounters, not only an introductory cinematic.
3. **Major-faction ownership changes the mob gang's boss presentation.** When a major group owns a mob gang, give that gang an ownership-aware, mob-themed human boss whose identity and music-driven performance combine the controlling faction with that mob.
4. **Hakken Horse is the explicit example:** when Gabberina's own the horse gang, its themed leader can be Hakken Horse, a human gang leader dressed like a horse. This is a human boss character, not a replacement of the horse entities. The owner supplied reference **GAB-HORSE-001** below for this concept.
5. **Keep the reference collection extendable.** The owner is beginning a collection of desired dance videos; preserve new links and organize them by faction, mob and style without imposing an arbitrary collection cap.

The emotional target is exceptional fun, variety and goosebump-level impact. Validate it through actual playtests and animation review rather than claiming a guaranteed physical reaction.

**Owner clarification:** Gabberina's music and dance identity is a mix of **jumpstyle, gabber and hardtekk**. Treat this blend as explicit creative direction. Exact proportions, song sequencing and movement combinations remain design decisions; do not reduce the crew to a gabber-only style.

## Solo-to-crew choreography for players and enemies

**Major explicit owner requirement:** build an amazing shared choreography system for **both players and enemies**. A participant can emote/dance alone or begin crew choreography that teammates join. Opposing gangs can go **head-to-head** with their members performing together. This must work both **while skating** and **during normal on-foot dancing**, retaining the music-generated/driven choreography ambition and fun variety. This is requested design, not a shipped feature claim.

The owner's fourth Cumbias dance reference, **CUM-DANCE-004**, is included below. Its movement and group formations have not been visually reviewed; the system requirement comes from the owner's text, not assumed video contents.

### Proposed interaction and acceptance design

The following details are proposals to design and validate, rather than extra owner-approved mechanics or claims of implementation:

- Support a clear solo → invite/join → synchronized crew → rival call-and-response/head-to-head → finish/return-to-solo flow. Define who can lead, who may join, membership/permission rules, spectators and how a player starts or leaves without ambiguity.
- Align joins, leaves and phrase changes to suitable beat/bar/phrase boundaries so late arrivals and departures feel musical. Give crew members varied complementary roles, mirrored/answering phrases and formation choices rather than identical clone motion.
- Preserve input agency. Joining player-controlled choreography should be deliberate, with clear exit/cancel and safe transitions back to ordinary movement/combat. Avoid forced player lock, involuntary camera control or enemy actions commandeering the player's character.
- Preserve continuity when the leader leaves, a member disconnects, someone changes movement mode, or an event is interrupted. Define handoff/continuation, recovery and graceful solo fallback when no eligible teammates remain.
- Make moving skate formations respect speed, momentum, terrain, obstacles, personal space and collision avoidance. Test curves, stops, grinds, airborne/landing transitions and constrained areas; adapt spacing or formation safely instead of clipping, teleporting members or freezing the scene.
- Define on-foot ↔ skating transitions and mixed-mode eligibility explicitly. Shared music and event state should remain coherent through compatible transitions; unsupported combinations need visible, graceful behavior.
- Let player and enemy crews express distinct identities during head-to-head routines, with fair readable timing, roles and interaction. Decide whether a particular routine is social, scored, competitive or tied to a campaign event; do not invent automatic wins, rewards or combat rules.
- Use server-authoritative participation/event state and synchronized music/phrase timing for significant choreography choices. Test multiple clients, late join, packet delay, disconnect/rejoin, competing join/leave requests, simultaneous crew routines, save/reload and clean cancellation.
- Respect muted play, reduced motion/flashing, readable cues and existing accessibility controls. Keep solo use enjoyable without requiring a full team.
- Preserve the no-content/no-population-cap and no-regression direction. Optimize formation solving, animation, timing, networking and startup/loading through profiling; do not solve cost by arbitrarily shrinking content or supported participation. Existing chunk-safe temporary-event lifecycle and safety rules remain intact.

### Shared-system to-do

- [ ] Specify player and enemy solo emotes, teammate joining, crew membership/leadership and rival head-to-head participation for skating and on-foot dance.
- [ ] Prototype music-driven solo-to-group choreography with complementary member roles and dynamic formations in both movement modes.
- [ ] Validate beat/phrase-aligned join/leave, leader handoff, input agency, interruption recovery, mode transitions and graceful solo fallback.
- [ ] Test moving formation collision/terrain safety, synchronized multiplayer timelines and repeated/interrupted/late-join flows under realistic network conditions.
- [ ] Native playtest for fun, responsiveness, crew identity and spectacle; profile full participation without arbitrary content/population caps or startup/gameplay regressions.

## Major requirement: reactive fight, trick-chain and combo music

**Explicit owner quality goal (October 12, 2026 UTC):** achieve Killer Instinct-level music integration in fights and while the player chains tricks, including the excitement of the owner's “ULTRAAAAA COMBO” example and music that responds to hits. Music should participate in the gameplay: combat, skate trick chains and combo escalation produce audible musical development and a memorable announcer-style payoff.

This is an inspiration and quality target. It does not claim an identical Killer Instinct implementation, analyze that game's internal audio system, approve particular numerical combo tiers, or claim JetSetCraft already does this. Create JetSetCraft's own sonic language, music and announcer identity; do not copy Killer Instinct recordings, samples, voice performances or protected assets.

### Proposed interaction and audio design

The following is a proposed implementation/acceptance design to prototype, not an additional approved combat or scoring system:

- **Immediate, meaningful response:** map confirmed fight hits, meaningful trick-chain events, landings, transitions and milestone achievements to coherent accents, fills or motif responses. Distinguish attempted, confirmed, missed and invalidated actions. Never delay input, hit registration, physics, tricks, landing or score settlement to quantize music.
- **Musical structure:** use an event-responsive layer for immediate feedback alongside beat/bar/phrase-aware layer, section or intensity transitions. Preserve musical coherence across rapid input and tempo/section changes without restarting the track at every hit. Final stem, sequencing, synthesis or hybrid technology remains open.
- **Combo arc:** design a clear build → escalation → peak → earned payoff → resolution. Explore increasing layers, rhythmic density, call-and-response or harmonic/timbral variation as a chain develops. Define tier conditions, variety/quality contribution, landing confirmation, breaks, failure, banking/cash-out and recovery only after reconciling existing Flow, trick and combat scoring. Do not invent new guaranteed rewards or alter established scoring merely to trigger sound.
- **Announcer moments:** give major milestones a short, original, readable hype call and musical punctuation. Define names, wording, actor/voice, localization, trigger rules, interruption priority and replay variation. Avoid constant shouting, overlapping calls or announcing unconfirmed success. A later cancelled/invalidated combo must recover gracefully.
- **Faction identity:** give Gabberina's, Cumbias, Phonkistas, Hardtekkas and the mob gangs distinctive instrumentation/motifs, phrase behavior and payoff character. Retain Gabberina's jumpstyle/gabber/hardtekk mix. Shared events use one clear music owner/mix policy, not every rival starting its own full track.
- **Choreography connection:** musical escalation and original combo responses should feed the same significant phrase/timing state used by solo-to-crew, enemy formations, on-foot/skating head-to-head routines, entrances and ownership-aware bosses. Adapt dance roles safely without taking control away from players or breaking terrain/contact correctness.
- **Clean mix and arbitration:** design precedence among gameplay warnings, impacts, music layers, announcer, dialogue and ambience. Use suitable ducking, headroom and voice management so fast chains, simultaneous players and rival surprises remain intelligible. Coalesce redundant audio events without dropping gameplay events or imposing arbitrary gameplay/content/participation caps.
- **Authority and multiplayer:** use server-authoritative combo/scoring/event outcomes with shared timeline/sequence IDs where needed. Define local immediate feedback and correction separately from confirmed shared musical payoffs. Test duplicate/out-of-order delivery, latency/jitter, late join, reconnect, spectators and concurrent crews so rewards, milestones and announcer calls are not duplicated or falsely confirmed.
- **Transitions and recovery:** specify fight-to-skating, chain-to-dance, track/faction handoff, scene change, pause, death, cancellation, chunk unload, missing metadata/assets and resource reload. Preserve earned results and current control/defeat state, release obsolete audio and return to a coherent baseline.
- **Accessibility:** independent music, gameplay effects and announcer volume/mute controls; visual equivalents for significant cues, captioned calls where useful, reduced motion/flashing and configurable presentation intensity. Turning sound or announcer off must not reduce information needed to play or change scoring.
- **Performance and startup:** profile mixing, decoding/streaming, event handling, synchronization, animation coupling, memory, I/O, startup and loading. Precompute/cache appropriate metadata and avoid per-tick raw-audio analysis or unbounded event queues. Maintain all approved capabilities and content coverage; performance work must not silently reduce feature variety or supported participation. Preserve established event lifecycle safety.

### Reactive-music implementation and acceptance to-do

- [ ] Audit current combat, hit-confirm, Flow, tricks, landing, combo, event and audio systems; identify authoritative event contracts and avoid duplicate scoring paths.
- [ ] Define the reactive-music state machine, immediate-feedback path, musical scheduling and failure/recovery semantics; document tier/payoff decisions as proposals until approved.
- [ ] Prototype one native fight sequence and one skate trick-chain sequence from baseline through escalation, successful payoff, break and recovery. Demonstrate that gameplay meaningfully changes the music.
- [ ] Create original or properly licensed stems/phrases/accents and a distinctive commissioned/licensed announcer treatment, with provenance and localization/caption planning. Supplied Spotify references do not grant remix or stem rights.
- [ ] Integrate faction profiles, solo/crew choreography, head-to-head routines, boss phases and rival surprise handoffs without overlapping incompatible music or secretly altering stripped powers.
- [ ] Verify immediate controls/hit/physics response under musical scheduling; compare feel and measured latency with a baseline and document a justified acceptance budget rather than inventing one.
- [ ] Test fast and varied chains, repeated same-action spam, misses, invalidated/late-confirmed hits, failed landings, simultaneous players, combo-tier changes and cancellation/retry.
- [ ] Verify server-authoritative significant state and correct local feedback under lag, jitter, duplicate/out-of-order delivery, late joins, reconnects, spectators, save/reload and server restart.
- [ ] Test music/announcer/effects controls, muted visual equivalents, captions, reduced motion/flashing, clear warnings and clean audio recovery through all interruptions.
- [ ] Profile exact-build client/server cost, audio/memory/networking and startup/loading against a comparable baseline; preserve gameplay, compatibility, content and participation without arbitrary caps or regressions.
- [ ] Native playtest musicality, responsiveness, faction identity, escalation/payoff clarity, replay variety and emotional impact. Attach exact-build captures, tests, performance results and rights evidence before claiming completion.

Shared tracking remains [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30); faction integration belongs in its existing gang issue. This update is design/backlog only.

## Requirement versus proposed implementation

Music-driven/generated choreography is required. The generation method is not selected: authored movement phrases sequenced from track metadata, procedural generation, offline generation, runtime generation, or a hybrid still need evaluation. Do not silently reinterpret this requirement as only playing music over fixed generic animations, and do not claim an AI/model or audio-analysis pipeline already exists.

The following are **proposed acceptance/design criteria**, to refine with reference review and native prototypes:

- Use beat, bar, phrase, section, accent and intensity information to choose and time movement, transitions, formations, entrances and finales. Support tempo/section changes where the chosen track requires them.
- Give each supported mob a recognizable movement vocabulary that fits its anatomy, proportions and locomotion, with multiple phrases, transitions and combinations. Vary energy, sequence and formation coherently; avoid random flailing, obvious identical loops and tempo-only reskins.
- Give each major faction distinct phrasing and signature motifs. Cumbias direction remains Mexican cumbia, Phonkistas remains phonk, and Gabberina's combines the owner-specified jumpstyle, gabber and hardtekk dance/skate identity. Hardtekkas adds a separate hardtekk-centered identity. Final footwork and substyles require direct reference review.
- Test planted feet or appropriate species contact, weight transfer, balance, joint limits, root motion, terrain contact and blending into skating, turns, grinds, airborne moves, stopping and recovery. Nonhuman rigs need appropriate species-specific solutions rather than forced humanoid retargeting.
- Make entrances build anticipation, deliver a synchronized reveal and flow into a playable escalating challenge and finale. Keep clear counterplay, readable hazards, player agency and short/skip options for repeat encounters.
- Use a server-authoritative musical timeline and stable seed/event state where appropriate so clients agree on significant choreography choices, score timing, formation changes and boss identity. Test late join, reconnect, lag, pause/seek/cancel and track handoff rather than assuming local audio clocks stay synchronized.
- Define behavior for missing or invalid beat metadata, unsupported rigs, absent optional mods and interrupted music. Safe fallbacks must be explicit; they must not silently mark the premium choreography requirement complete.

Proposed data keys include stable faction ID, source gang/mob ID, style profile, rig adapter, music/beat profile, phrase set, variation rules, boss profile and ownership-state version. These are design suggestions, not committed APIs.

## Ownership-aware mob boss design

The scope applies across mob gangs controlled by Cumbias, Phonkistas, Gabberina's or Hardtekkas, with Hakken Horse as the first owner-specified example. Each supported faction × mob pairing needs a deliberate boss/music/dance concept; a changed name or costume color alone is insufficient.

- Preserve the source mob gang and its stable identity. The human boss is a themed leadership/presentation layer; do not delete, replace or subclass provider-owned mobs to create it.
- Combine the controller's music and signature style with the source mob's readable theme, costume silhouette, movement motifs and existing permitted abilities.
- Preserve established faction identity, including Gabberina's adult human female crew direction. The Hakken Horse example specifies a human in horse-themed dress; the reference does not authorize copying a performer or inventing an approved final roster.
- Define boss selection, first reveal, challenge role, rewards, despawn/cleanup and restoration when ownership changes. Full ownership, partial influence and contested control must be distinguished explicitly.
- Define neutral/player-owned behavior and faction-to-faction recapture behavior. Whether every pairing has one or several boss variants, and their names and exact abilities, remain design decisions. The every-mob ambition must not be quietly cut down to the horse example.
- Resolve boss and choreography changes deterministically at safe event boundaries. Specify simultaneous ownership changes and in-flight challenge handling; never let a visual change rewrite earned results or source-mob state.

## Existing campaign rules remain binding

This requirement expands the shared [Gang Wars master specification](https://github.com/Herbertofury/JetSetCraft/wiki/Gang-Wars-Boombox-and-Mob-Atlas) and the four faction designs; it does not replace them.

- Player and rival factions compete for normal mob-gang influence/control.
- Full takeover removes that source gang's corresponding borrowed power from the affected rival. A boss costume or generated dance must never restore stripped powers secretly. Define recapture/restoration and in-flight transitions explicitly.
- After a normal mob event resolves, an eligible rival retaining some source-gang control may make a dramatic surprise entrance and start a separate challenge. Preserve the original result/rewards first; arbitrate multiple eligible rivals without overlapping event/audio starts or duplicate/recursive chains.
- Keep independent persistent defeat states and a clear campaign conclusion. Boss variants and optional rematches must not revive a defeated hostile faction or erase completed progress.
- Preserve original mobs, UUIDs, inventories, ownership, provider state, AI recovery, saves, optional-mod fallbacks, movement/combat ownership, accessibility and existing capabilities.

## Extendable owner reference register

**Review status:** all six original video URLs were attempted read-only on October 12, 2026 UTC. The retrieval tool reported each inaccessible. No clip was watched or analyzed. All six video entries remain **pending visual/audio review**; no timestamps, footwork, tempo, costume detail or music identity is inferred from the URLs.

| Reference ID | Faction / mob / style key | Original owner URL | Owner-supplied purpose | Review and observations |
| --- | --- | --- | --- | --- |
| CUM-DANCE-001 | Cumbias / general / cumbia dance reference | https://www.tiktok.com/@elialaluviano/video/7682240992384126229 | Desired Cumbias dance inspiration | Pending; no visual observations |
| CUM-DANCE-002 | Cumbias / general / cumbia dance reference | https://www.tiktok.com/@lachina1451/video/7679595256660233492?is_from_webapp=1&sender_device=pc | Desired Cumbias dance inspiration | Pending; no visual observations |
| CUM-DANCE-003 | Cumbias / general / cumbia dance reference | https://www.tiktok.com/@lachina1451/video/7678164832519982357?is_from_webapp=1&sender_device=pc | Desired Cumbias dance inspiration | Pending; no visual observations |
| CUM-DANCE-004 | Cumbias / general / choreography reference | https://www.tiktok.com/@luxurychoreograph/video/7695485710962380046 | Additional desired Cumbias dance inspiration supplied with solo-to-crew system request | Pending; no visual observations |
| GAB-HORSE-001 | Gabberina's / horse / Hakken Horse concept | https://www.tiktok.com/@twi68536/video/7695409049591876878?is_from_webapp=1&sender_device=pc | Human gang leader dressed as a horse; music-generated mob-themed boss example | Pending; description comes from owner, not independent viewing |
| GAB-DANCE-001 | Gabberina's / general / gabber dance reference | https://www.tiktok.com/@hardcoresanni/video/7686213149325921568 | Additional owner-supplied gabber dance inspiration for Gabberina's | Pending; no visual/audio observations |

For new entries, record stable reference ID; faction; mob or general scope; owner-requested style/purpose; original URL; review status/date; reviewer; observed timestamps and factual motion/music notes only after viewing; possible original adaptation; provenance/rights status; related design/issue. Preserve unavailable links so the owner can provide an accessible reference later. Deduplicate exact repeats without dropping distinct clips or owner annotations.

Prior music/video references remain on [Cumbias](https://github.com/Herbertofury/JetSetCraft/wiki/Cumbias%E2%80%90and%E2%80%90Multi%E2%80%90Rival%E2%80%90Gang%E2%80%90War), [Phonkistas](https://github.com/Herbertofury/JetSetCraft/wiki/Phonkistas) and [Gabberina's](https://github.com/Herbertofury/JetSetCraft/wiki/Gabberinas%E2%80%90and%E2%80%90Gang%E2%80%90War%E2%80%90Campaign). They are not discarded or treated as newly reviewed.

### Gabberina intended song references

**GAB-MUSIC-002 · Gabberina's / general / intended song:** [I Need Your Lovin' (Like The Sunshine), Danny Ores](https://open.spotify.com/track/2tvhfItMoRiHmo9QuCnKem?si=c24fd0e2909843d7). The owner explicitly added this as a song reference to be used for Gabberina's. Spotify's public page was checked on October 12, 2026 UTC and identifies Danny Ores, 2025, duration 2:43. Audio, beat/bar/phrase structure and choreography timing have not been analyzed. Preserve the earlier G.A.B.B.E.R. reference and other supplied music/video references.

**GAB-MUSIC-003 · Gabberina's / general / intended song:** [Hardtekk Tutorial, Ski Aggu, DJ TÖRKE and Mr. Polska](https://open.spotify.com/track/26jLDg6jw9KEOQlyh4IybJ?si=16465eeb535b4a65). Owner-selected Gabberina reference, added alongside the earlier tracks. Spotify's public page was checked on October 12, 2026 UTC: 2026, duration 2:44. No listening, beat analysis or detailed choreography inference is claimed.

Planned use must be resolved through an authorized audio source and the permissions needed for the actual implementation. This selection is not evidence of a license, permission to extract Spotify audio, or completed in-game playback.

### Hardtekkas starting song references

[Hardtekkas](Hardtekkas) is the fourth owner-approved major rival; **Tekkistas** is its optional reputation-unlocked name, tied to existing Friendly/Crew Naming Rights without changing stable faction identity. Full naming semantics and faction backlog: [issue #31](https://github.com/Herbertofury/JetSetCraft/issues/31). Gabberina's retains its jumpstyle/gabber/hardtekk blend and Hardtekk Tutorial reference.

- **HARD-MUSIC-001:** [Drucktherapie](https://open.spotify.com/track/5t2MaaBqEwu7P1RSAUv89Q?si=14a3ff4f0f704745), HRDSND and resonant.live; 2026; 2:45.
- **HARD-MUSIC-002:** [Wake Up](https://open.spotify.com/track/64z5X73349aabORkEdkLau?si=25fe4b1966ab4b48), Dr.Sheppat; 2025; 3:38.
- **HARD-MUSIC-003:** [Break It Down](https://open.spotify.com/track/5Db0K6UhL9e3ywaSCMgTBc?si=abd168dd262240bc), Greb Levah; 2026; 2:47. Additional owner-selected reference; preserve the first two.

Spotify public metadata verified October 12, 2026 UTC. All three remain pending audio/beat/phrase analysis; no BPM, instrumentation, choreography timing or listening claim is inferred from metadata. Preserve original links and resolve authorized sourcing and actual use rights before implementation.

## Rights, accessibility and performance

References provide inspiration, not permission to copy a creator's likeness, costume design, choreography asset, recording or other protected material. Create original characters and assets; document any necessary music/asset permissions for the actual use. Do not extract, redistribute, train on or ship reference recordings merely because links were supplied.

Maintain visual rhythm cues with audio muted, separate music/effect levels, reduced motion/flashing, readable telegraphs and player control. Emotional impact should come from timing, composition, animation and musicality.

Preserve the existing no-cap / no-regression / startup-performance direction. Do not reduce mob coverage, dance variety, supported features or compatibility to make optimization easier. Separately preserve established bounded active-event actors, chunk-safe lifecycle and anti-farm safeguards. Profile and improve client/server frame/tick time, memory, networking, audio, loading and startup; record comparable baseline versus exact-build results. Numeric budgets and a coverage plan remain design tasks, not fabricated benchmarks.

## Actionable implementation and acceptance backlog

- [ ] Review supplied clips directly when accessible; populate factual observations and identify the owner's desired phrases without claiming unviewed details.
- [ ] Define all-mob coverage and an extendable faction × mob × rig × style matrix, including optional-provider and missing-adapter behavior.
- [ ] Select and document the music-to-choreography approach, timing metadata, authoring/generation pipeline and provenance boundaries.
- [ ] Prototype multiple coherent dance phrases and combinations per mob; prove music changes meaningfully affect choreography and transitions.
- [ ] Build distinct exceptional Cumbias, Phonkistas, Gabberina and Hardtekkas signature dance/skate sets with entrance, playable escalation and finale; Gabberina combines jumpstyle, gabber and hardtekk. Review both newly selected Gabberina songs alongside the earlier references.
- [ ] Prototype Gabberina-controlled horse gang → original Hakken Horse human boss; verify source horses remain intact.
- [ ] Extend ownership-aware human boss profiles across the supported mob/faction matrix, with explicit neutral, player, rival, contested and recapture behavior.
- [ ] Test full takeover strips powers, stale ownership cannot select an obsolete boss, post-event surprises preserve normal rewards, multi-rival arbitration is clean and independent defeat persists.
- [ ] Review native animation from multiple angles and terrains for rig/contact quality, skating blends, varied combinations and visual clarity.
- [ ] Verify deterministic significant multiplayer choreography and boss state through save/reload, server restart, lag, late join, disconnect/rejoin, chunk unload, cancellation and simultaneous control changes.
- [ ] Verify optional mods present/absent, source-mob preservation, existing gameplay regression coverage, accessibility and audio cleanup.
- [ ] Profile client/server performance, memory, networking and startup/loading without feature loss or arbitrary coverage caps; fix regressions before acceptance.
- [ ] Gather playtest feedback on musicality, recognizable faction/mob identity, variety, fun, fairness and goosebump-level impact; iterate rather than treating checkbox completion as proof of quality.
- [ ] Attach exact-build native runtime captures, test results, performance comparison and asset/music provenance before checking implementation complete or updating release claims.

Related planning: [Hardtekkas issue #31](https://github.com/Herbertofury/JetSetCraft/issues/31), [Gabberina issue #27](https://github.com/Herbertofury/JetSetCraft/issues/27), [Cumbias issue #28](https://github.com/Herbertofury/JetSetCraft/issues/28), [Phonkistas issue #29](https://github.com/Herbertofury/JetSetCraft/issues/29), [Roadmap](https://github.com/Herbertofury/JetSetCraft/wiki/Roadmap).

## Major requirement: now-playing reveals, radio and reputation style perks

### Explicit owner goals

- Show a **stylish title-and-artist reveal when music starts**, making the track feel part of JetSetCraft's presentation.
- Provide **in-game radio/music selection**.
- Pursue **Spotify integration** as an optional authorized music provider.
- Let **reputation perks unlock customization of mobs' music and dance style**.

These are design goals and actionable backlog, not shipped or runtime-verified capabilities. Specific UI layouts, available sources, thresholds, ownership rules and technical interfaces below are proposals to finalize.

### Proposed now-playing and radio experience

Use one provider-neutral UI contract for game-native licensed music, authorized music packs and an optional approved Spotify adapter. Identify the actual source, title and credited artists accurately. Show the reveal only after music has actually started or a confirmed track change occurs; queued, buffering, unavailable, paused and disconnected are distinct states. Deduplicate repeated callbacks and do not restart a reveal on every reactive stem change.

Explore a compact animated title card with a readable persistent now-playing panel, dismiss/replay behavior, adjustable presentation intensity, reduced motion and screen-safe placement. Preserve gameplay warnings, HUD readability and player control. Spotify metadata, attribution and unmodified artwork must follow the applicable [Spotify design requirements](https://developer.spotify.com/documentation/design); any alternate branding requires its own approved scope.

The radio selector should expose permitted stations/tracks/packs, source and connection status, current selection and unavailable-content feedback. Search/favorites, station organization, shuffle and queue behavior are proposals rather than promises of specific API availability. Define interaction between personal listening, faction/event music, reactive gameplay music and server-controlled shared events. Give the active musical context one coherent mix/ownership policy; no competing full tracks or silent takeover of another player's listening.

### Optional Spotify provider: approved scope and verified capabilities

Plan the Spotify adapter against the **actual authorized API and contractual scope**, with verification before release. Do not treat a normal subscription, a reference song link or a playback selection as synchronization, remix, redistribution or broadcast permission. Keep private authorization records outside public wiki/issues; this specification makes no claim of an official partnership or endorsement.

Document an explicit capability matrix: metadata/catalog display; playback/control; gameplay-triggered changes; audiovisual/beat synchronization; analysis/derived beat data; stems/transforms; per-listener playback; multiplayer/group use; distribution and production/API access. Enable only capabilities covered by both the actual permission and available technical interface. Standard public rules remain relevant wherever no applicable exception exists: [Developer Policy](https://developer.spotify.com/policy), [Developer Terms](https://developer.spotify.com/terms).

The [currently-playing Web API](https://developer.spotify.com/documentation/web-api/reference/get-the-users-currently-playing-track) supplies playback state/track metadata, not audio samples. [Connect playback control](https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback) acts on an eligible Spotify player. The [Web Playback SDK](https://developer.spotify.com/documentation/web-playback-sdk) is a browser JavaScript library rather than a native Java/Forge audio engine; its [documented state/control interfaces](https://developer.spotify.com/documentation/web-playback-sdk/reference) do not provide a general raw-audio/stem extraction path or a sample-accurate game timing guarantee.

Therefore retain a **separate, first-class original/commissioned or appropriately game-licensed reactive audio engine** for combat accents, trick chains, combo payoffs and deterministic crew choreography. Do not promise that Connect/SDK playback alone can deliver stems, hit-by-hit remixing, exact beat jumps or synchronized multiplayer music. Advanced approved partner capabilities must be documented and tested on their actual delivery path. Never rip/cache-capture Spotify audio, reverse-engineer a decoder or move restricted behavior into a companion as a workaround.

Keep remote/provider requests asynchronous and outside render/tick-critical paths. Serialize/cancel stale selection requests, avoid one API command per hit/trick, honor rate limits and expose offline/missing-device/expired-authorization/unavailable-track states. The base mod must start and remain playable without Spotify, an account, a subscription or a network connection. Future authorization/security design must use supported flows, minimum scopes and protected client-local credentials, with disconnect/revocation and no tokens in the JAR, game server, logs or packets. This documentation performs no account setup or authorization grant.

For multiplayer, distinguish a player's personal playback from authorized shared event audio. Do not broadcast another person's Spotify audio or credentials; group synchronization requires its own permission/capability. Default to independently authorized per-listener playback where supported, while the server distributes only permitted game/cue state. Preserve fair scoring and shared choreography when tracks differ by account, region or availability. Personal files must not be automatically uploaded or redistributed.

### Reputation-unlocked mob music and dance customization

Use the existing [canonical gang reputation](Hangouts-Territory-and-Reputation#canonical-gang-reputation) and stable gang-ID architecture. The master design already assigns naming rights to configured Friendly reputation. **The owner has not specified the thresholds for these new music/dance perks:** decide whether Friendly or another configured rank unlocks each capability rather than assigning numbers or treating Crew Naming Rights as automatic permission for all style changes.

Proposed design/acceptance:
- Distinguish the player's unlocked customization entitlement, their saved preference, server/world authority and the currently valid presentation. Reputation is tracked by player UUID + gang ID, not farmed separately from each spawned mob.
- Let an eligible player choose supported music/style profiles and restore canonical faction/mob presentation. Define whether a choice affects personal UI/listening, an owned/allied gang, a local chapter, a specific event or the shared world. Personal preferences must not silently overwrite other players' styles.
- Shared or public event changes follow server permissions and explicit ownership/leadership rules. Specify voting/permissions where applicable, and behavior for conflicting edits, operator locks, reputation loss, betrayal/reconciliation and ownership changes.
- Preserve gang ID, source entities/UUIDs, provider state, control, borrowed powers, membership, rewards, boss identity rules and persistent defeat. A cosmetic/music preference must not reset progress, duplicate rewards, restore stripped abilities or revive a defeated faction.
- A chosen dance must suit the actual anatomy/rig, proportions, contact, locomotion, terrain, skating momentum and transitions. Maintain characteristic mob identity and safe missing-adapter/provider behavior instead of forcing every creature onto humanoid motion.
- Keep music-driven variation and shared solo-to-crew/head-to-head choreography coherent. Apply significant shared style/timeline changes at safe boundaries, while preserving immediate input and voluntary participation.
- Define restrictions during an active scored event so a style edit cannot obscure hazards, change score windows or invalidate another player's challenge. Preserve readable counters and accessibility.
- Selecting a track/style grants no recording, remix, choreography-asset or distribution rights. Expose only the capabilities and assets actually authorized for the chosen source; retain the original/licensed fallback.

### Actionable radio, provider and style-perk backlog

- [ ] Specify provider-neutral playback/metadata/capability contracts and radio/now-playing state transitions with truthful confirmation and source attribution.
- [ ] Prototype title/artist reveal, persistent readable now-playing UI, radio selection and reduced-motion/dismiss behavior using original or appropriately licensed music.
- [ ] Define event/faction radio versus personal listening priority, clean music handoffs and compatible coupling to reactive combo/crew choreography.
- [ ] Map actual Spotify approval and API access to each intended capability privately; publish only a safe capability/status summary and independently verify release prerequisites.
- [ ] Prototype only approved Spotify metadata/control/playback routes; test actual platform support and graceful fallbacks without promising stems or sample-accurate synchronization.
- [ ] Verify no secret in distributed builds, no credentials/audio relayed through game servers, explicit per-player opt-in/disconnect and robust network/rate-limit recovery.
- [ ] Resolve reputation ranks/thresholds, unlocks, affected mob/gang scope, personal/shared permissions, canonical restore and rank-loss/ownership-change behavior for music/dance customization.
- [ ] Implement an extendable compatible mob × rig × dance/music-style matrix and safe event-boundary application while preserving identity, momentum, powers and defeat records.
- [ ] Test repeated starts, rapid selection changes, missing assets/device, muted audio, offline/expired provider access, denied capabilities and incompatible profiles.
- [ ] Test two players with different preferences, concurrent shared edits, unauthorized changes, late joins, lag, disconnect/rejoin, save/reload, restart and provider removal.
- [ ] Test varied mob rigs and terrain, on-foot/skating transitions, solo/crew formation continuity, source-mob preservation, clear telegraphs and canonical reset.
- [ ] Profile client/server/audio/network cost, caches, startup/loading and large style/reference collections without feature loss, arbitrary content/participation caps or regressions.
- [ ] Attach exact-build native captures, tests, performance comparisons, accessibility review and authorized asset/provider provenance before implementation or release claims.

Shared implementation remains in [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30); faction-specific references stay on their gang pages/issues.

## Major requirement: nighttime music-synchronized rave presentation

**Explicit owner goal:** when it is night, course generation should add lights and lasers synchronized to the music, creating a full rave atmosphere while the player skates and chains tricks. The owner's flagship quality target is the feeling of going to **“the best rave ever on earth”**: exceptionally fun, immersive and goosebump-inducing, not merely a few extra lights. Carry that ambition through playable movement, musical escalation, original audiovisual art direction, crew energy and competitive areas, rather than only an entrance cinematic. This is an aspirational experience to validate through native playtests, not a guaranteed emotional reaction. This is requested design/backlog; no renderer, course generator or gameplay implementation is claimed.

### Proposed presentation and safety design

- Detect the actual nighttime/environment context of the event. Define dimension-specific sky/light behavior and whether local ambient conditions qualify; do not change world time, weather or the environment merely to force a rave.
- Build a temporary, biome/terrain-aware lighting layout alongside the course: safe fixture/beam positions, sightlines, skate routes, arena boundaries and audience readability. Use original visual motifs fitting the current faction, mob style, music source and course setting.
- Represent lights, emissive accents, lasers and musical patterns as **transient rendering effects/event-owned presentation**, not permanent light blocks or edits to vanilla/player builds. Laser beams are visual effects, not damage, collision obstacles, block breakers or real gameplay hazards unless a separate mechanic is explicitly designed and approved.
- Use approved beat/bar/phrase/section/accent data to generate coherent pulses, moving beams, color/intensity changes and combo accents. Coordinate with the same significant music/crew timeline used by reactive fights, trick chains and choreography. Never delay player input or physics to fit lighting.
- Respect the selected provider's actual capabilities and rights. A Spotify track selection alone does not supply beat analysis, authorize visual synchronization or guarantee timing. Use permitted metadata/cues when available; otherwise expose a clear supported fallback rather than claiming exact music sync. Original/licensed game-native music remains the foundation for exact reactive timing.
- Preserve visibility of terrain, hazards, grind rails, opponents, score cues and exits. Avoid bright beams or strobing that hide critical play information. Spectacle should come from musical composition, spatial design and movement, with reasonable brightness.
- Provide reduced-flash and no-strobe alternatives, reduced-motion options, adjustable effect intensity/brightness and complete disabling of nonessential lighting effects. Muted play keeps useful visual rhythm cues without mandatory flashing. Do not make accessibility settings change scoring, unlocks or competitive difficulty.
- Synchronize significant effect/cue state to event-authoritative timing where necessary, while rendering locally. Define late join, drift recovery, incompatible shader/resource-pack behavior and low-performance rendering without sending per-particle network updates.
- Attach effects to the temporary event lifecycle. Clear them on finish, exit, cancellation, death where appropriate, disconnect, chunk unload, provider/music handoff and server/world shutdown. Never leave persistent beams, lights, audio or orphan controllers after an event.
- Profile allocation, draw calls, shaders, particles, transparency/overdraw, visibility checks, memory and startup/loading. Use appropriate spatial/visibility culling, reusable buffers, cached layouts and adjustable visual quality. Retain gameplay content and participation; optimize rendering rather than imposing arbitrary roster/content limits or removing capabilities. No required shader/mod dependency may be introduced silently.

### Combo-reactive rave and crew reactions

**Explicit owner clarification:** make the presentation react entertainingly when participants pull off chained tricks and combos. Apply the established Killer Instinct-inspired payoff goal to **players and rivals**, with music, lights, lasers and crew reactions escalating together around meaningful confirmed chain milestones.

Proposed design/acceptance:
- Coordinate musical accents/layers, beam patterns, spatial lighting and short original crew hype/answering gestures around successful tricks, confirmed landings, rising combo tiers, major payoffs and breaks/recovery.
- Give factions and mob crews distinctive reactions with coherent variation; avoid every member repeating the same cheer or every hit triggering the maximum spectacle.
- Respect actual combat/trick outcomes and authoritative scoring. Never invent success, delay inputs/hits/physics for a beat, alter earned rewards, or steal control from a player to perform a reaction.
- Preserve readable telegraphs, routes, opponents and UI under peak spectacle. Keep reactions compatible with the mob's rig, locomotion and event role; a rival celebration must not silently bypass its recovery or counterplay rules.
- Arbitrate simultaneous player/rival milestones so music, announcer and effects remain intelligible. Reduced-flash/no-strobe, reduced motion, brightness and effect/announcer controls apply to the entire combined payoff.
- [ ] Prototype player and rival chain escalation, successful landing/payoff, break and recovery with synchronized but varied music/light/laser/crew responses.
- [ ] Test simultaneous chains, short/repeated combos, network corrections, cancellation and replay for fair readable timing without audiovisual spam.
- [ ] Verify native frame/tick stability, complete event cleanup and accessibility at peak intensity without reducing gameplay content or supported participation.

### Actionable nighttime-rave backlog

- [ ] Define nighttime/dimension eligibility and event-owned lighting descriptors in the temporary biome/terrain-aware course contract without modifying world time or source terrain.
- [ ] Prototype one native nighttime skate/trick course with a readable, musically coherent light/laser layout and original faction motifs.
- [ ] Integrate permitted music cues, reactive combo escalation, solo/crew performance and safe track/provider handoffs; distinguish supported exact timing from approximate or unavailable sync.
- [ ] Validate terrain/route visibility, collision-free visual effects, no unintended damage or world edits, and preservation of vanilla/player builds.
- [ ] Implement reduced-flash/no-strobe, brightness/intensity and reduced-motion/off controls; test that essential cues and fairness remain available.
- [ ] Test late join, lag, reconnect, simultaneous events, chunk transitions, daylight transition, dimension change, cancellation and finish cleanup.
- [ ] Test shader/resource-pack combinations and optional-mod present/absent behavior with safe local fallbacks.
- [ ] Profile stable frame/tick time, memory, graphics/network cost and startup/loading on representative systems without content/participation caps or feature regressions.
- [ ] Playtest the whole rave as a flagship experience: musical coherence, movement flow, crowd/crew energy, combo escalation, spatial composition, fun and goosebump-level impact. A checklist of extra lights alone is insufficient. Iterate from real player feedback.
- [ ] Attach exact-build native nighttime captures, cleanup tests, accessibility review and performance comparisons before claiming completion.

Track shared music/presentation work in [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30), and crosslink the temporary-course design when that page is published.

## Linked course research and latest Hardtekkas references

[Full terrain-aware circuits, boss arenas and underwater skating research](Terrain‐Aware-Event-Circuits-and-Boss-Arenas) defines the non-destructive course and night-rave lifecycle. [Hardtekkas](Hardtekkas#owner-music-reference-register) retains the complete growing collection of owner-selected songs, including DOPPLER, Ober Mega Affen Geil, Sonntagskorg, Norti im Keller and die stimmen gewinnen langsam - super slowed. Metadata and audio-review limitations are recorded per entry; these are references, not claims of implemented playback.


## Latest approved rival roster: six crews

The owner selected **[Krumpaz](Krumpaz)** for krump and **[Footworkas](Footworkas)** for Chicago footwork. They join Gabberina's, Cumbias, Phonkistas and Hardtekkas; this six-crew roster supersedes earlier four-rival expansion counts without deleting prior identities or requirements. [Implementation and music-audition backlog #33](https://github.com/Herbertofury/JetSetCraft/issues/33).

The new pages contain six Krumpaz and seven Footworkas researched listening candidates. Current owner-selection and review statuses are maintained on each faction page; reference approval is distinct from production licensing and implemented playback. Shared control/power removal, independent defeats, surprise arbitration, choreography, music/rave, customization and safe-course systems must support all six. Final rosters and new unlock thresholds remain open; no implementation claimed.



## Nether and End event lighting eligibility

Owner requirement: full rave lights, lasers and music/chain-combo reactions are available throughout active Nether and End courses and boss events regardless of Overworld time. Use dimension/event context, not a night check or local darkness threshold; nearby lava and luminous blocks must not suppress the show. Preserve biome-native palettes, route readability, reduced-flash/no-strobe and brightness controls. Effects remain temporary and session-owned, clearing on event completion/cancellation without modifying world time, weather or real light blocks. Source-themed boss rooms inherit this rule through their event manifest.

See [course presentation and acceptance tests](Terrain‐Aware-Event-Circuits-and-Boss-Arenas#nether-and-end-rave-staging-at-any-time). Design requirement only; native implementation/testing remains open.


## Genre-specific CPM character quality

**Owner requirement, October 13, 2026:** the non-mob, music-genre gang members should be memorable, richly detailed CPM characters with excellent hair physics and attractive movement. The supplied [quality-reference collection](https://drive.google.com/drive/folders/1WILlzLBan19eQyfLMbi3gBXY_AmyfFVi) sets a model-detail and movement-quality target. Access remains controlled by the owner's Drive permissions.

**Explicit clothing clarification:** create appropriate clothing, styling, hair and accessories for each gang's music genre instead of copying the reference outfits. Reference character species, identity, costume and exact design are not required replacements for the human music-gang brief. Preserve each crew's recognizable identity and varied individual members.

Reference review status: the folder was opened and its image/video entries verified. The WamzeeWoozy-2096527696905822261-2.png still was visually inspected: layered hair, dimensional silhouette, expressive face, detailed clothing geometry and textures provide useful quality cues. Its associated three-second MP4 was opened and a rendered frame inspected; this is not a complete motion/physics audit or proof of a particular CPM implementation. Remaining reference review and asset provenance are pending.

### Character implementation and acceptance backlog

- [ ] Review the full reference collection and record concrete silhouette, face, hair, rigging, material and movement targets; retain source attribution and review status.
- [ ] Build original genre-specific model sheets for Gabberina's, Cumbias, Phonkistas, Hardtekkas, Krumpaz, Footworkas and Industrials, with distinct silhouettes and member variation.
- [ ] Prototype CPM-compatible hair secondary motion and suitable accessory/clothing motion through dance, high-speed skating, grinds, spins, jumps, landings and abrupt stops. Verify support on the actual selected CPM version before promising a feature.
- [ ] Verify stable settling, collision/clipping control, readable faces and silhouettes, terrain/contact correctness and clean animation transitions. Include multiplayer, pause/resume, teleport, reload and event cleanup tests.
- [ ] Connect character presentation to the existing music-driven solo/crew choreography, entrances, rival challenges and combo reactions without forcing player controls.
- [ ] Profile model loading, animation, physics, rendering, allocations and worst-frame cost. Improve speed and startup alongside the feature work without losing approved capabilities, introducing arbitrary content/population caps or causing regressions.
- [ ] Attach native captures and exact-build evidence before claiming these characters or physics are implemented. Confirm reuse rights separately from using a reference for inspiration.

## Roster addition: Industrials

**Owner-selected name: Industrials.** This is the industrial-dance gang. It extends the six previously named music crews to seven named crews; older six-crew headings are historical rather than a roster limit. Earlier suggested alternative industrial names and reputation titles were not selected.

**IND-DANCE-001:** [owner-supplied industrial dance reference](https://www.tiktok.com/@psr1257_12/video/7693819463061359886). Preserve the exact source; detailed visual review is pending. Do not infer specific choreography, clothing, performers or music from the URL alone.

- [ ] Review IND-DANCE-001 and develop a distinctive industrial dance/musical identity, genre-appropriate clothing and original CPM character designs.
- [ ] Integrate Industrials with shared rival-control, challenge, choreography, music/lighting and safe-course systems, retaining fair counterplay and the performance/startup invariant.
- [ ] Define any reputation title, progression thresholds and final music selections separately; none are approved by this naming decision.

## Roster addition: MoccaSins / unlockable names

**Owner-selected name: MoccaSins (renamed from Thundersteps).** This is the requested Native American powwow-inspired music/dance crew. **Drumrunners is an optional high-reputation name**, explicitly selected by the owner. Use MoccaSins as the canonical display name and preserve the existing stable gang ID; unlocking or selecting the alternate name must not reset ownership, reputation, powers or defeat progress. The exact qualifying reputation rank/threshold is not yet specified; do not invent a numerical level or silently reuse another crew's threshold.

**THUNDER-DESIGN-001:** [owner-supplied costume/design reference](https://www.tiktok.com/@xay.balam/video/7675497186519420178). This is specifically a costume/design reference, not an approved music track or proof of a particular dance tradition. **Partial visual/page review, October 13, 2026:** the cloud browser loaded the post. It explicitly displays “Creator labeled as AI-generated” and tags Mexico, Azteca and Maya; the caption names Humo De Bisonte. A viewed frame shows a feathered headpiece, long dark hair, dark patterned clothing and metallic-looking accessories. These are visible design cues, not verified cultural provenance. Preserve this as the owner's visual inspiration; do not label it verified powwow regalia/choreography, infer the creator's nation or assume permission to reproduce ceremonial elements. No music selection is implied by saving this costume reference.

This brings the current named music-crew roster to **eight**: Gabberina's, Cumbias, Phonkistas, Hardtekkas, Krumpaz, Footworkas, Industrials and MoccaSins. Tekkistas and Drumrunners are reputation name options, not additional rival gangs. Earlier six- and seven-crew counts are historical, not limits.

### MoccaSins implementation and acceptance backlog

- [ ] Review THUNDER-DESIGN-001 and document the specific visible costume/design cues and provenance, separating observation from assumption.
- [ ] Research music and movement with specific Native artists/dancers' sources; identify relevant traditions accurately and avoid conflating distinct nations or treating ceremonial material as generic reusable decoration. Seek appropriate collaboration and permissions for production assets.
- [ ] Develop original, genre-grounded CPM member designs, detailed silhouettes and stable secondary motion at the shared character-quality bar.
- [ ] Define and implement the high-reputation Drumrunners naming option using canonical player/gang reputation, clear unlock feedback and preserved stable identity/progress. Exact threshold remains a design decision.
- [ ] Integrate the crew into shared rival challenges, ownership and power removal, entrances, music-driven solo/crew choreography, combo reactions, biome-aware courses and temporary rave presentation.
- [ ] Validate fun, readability, cultural/artistic specificity, performance/startup, multiplayer and safe cleanup; preserve capabilities and avoid arbitrary content/population caps or regressions.

These are requirements and planned work, not claims that assets, choreography, unlocks or gameplay have been implemented.



## Roster addition: Honkistas

**Owner-selected base name: Honkistas.** This is a distinct clown-themed gang, with clown phonk or a related musical direction under consideration. The owner explicitly approved all seven other suggested names as optional high-reputation unlocks: **Clownistas, Clownaz, Clownettes, Harlequinas, Jesterinas, Clownksters and Circusistas**. These are selectable names for the same gang, not separate factions. Preserve its stable gang ID, ownership, powers, reputation and defeat history when renaming. Exact unlock thresholds/order and music selections remain undefined.

The current named roster now has **nine crews**: Gabberina's, Cumbias, Phonkistas, Hardtekkas, Krumpaz, Footworkas, Industrials, MoccaSins and Honkistas. Earlier counts are historical; reputation aliases do not increase the faction count.

**CLOWN-DESIGN-001:** [owner-supplied clown costume reference](https://www.tiktok.com/@s1535132ifj/video/7695717644472700180?is_from_webapp=1&sender_device=pc). Preserve this specifically as a costume/design reference. Visual review is pending; no specific outfit, performer identity, music track or reuse permission is inferred from its URL.

- [ ] Implement Honkistas as the base identity and all seven approved optional high-reputation names; define unlock thresholds/order using canonical reputation without inventing approved values. Reconcile roster links.
- [ ] Review CLOWN-DESIGN-001 and develop original clown-themed CPM member designs at the shared detail, hair/secondary-motion and genre-specific styling quality bar.
- [ ] Define the musical and dance identity, including whether clown phonk is the final direction, while keeping the crew distinctive from Phonkistas.
- [ ] Apply the shared rival, choreography, combo/music/rave, safe-course and performance/startup requirements. Preserve readable gameplay and varied members; implementation remains pending.


### Honkistas special role: surprise after the surprise

**Explicit owner requirement:** Honkistas are a special, randomly appearing high-challenge gang. After a player finishes a gang-related event and then defeats its already-random rival challenge, Honkistas can appear as a further, more intense clown-themed challenge. The intended sequence is **gang event completion → random rival challenge → rival defeated → possible Honkistas takeover**. The trigger is the victory over the intervening challenge, not merely completion of the original event. Support eligible events associated with any gang; do not silently restrict this to Gabberina's.

The desired character is theatrically unhinged, unnerving, surprising and more challenging: a manic clown-show escalation with memorable entrances and a distinctive performance. Express that through fictional characterization, choreography, audiovisual direction and gameplay rather than a clinical diagnosis. It must be genuinely fun to overcome, with readable counterplay rather than opaque or unavoidable punishment.

**Music takeover requirement:** twist the preceding encounter's music into a more clown-themed treatment so the player recognizes the takeover. **CLOWN-MUSIC-001:** [owner-supplied clown music/tone reference](https://www.tiktok.com/@honeymiauuuu/video/7683964537854364946). Detailed audiovisual review is pending; no title, artist, exact musical technique or specific audio asset is inferred from the link.

#### Proposed encounter design and acceptance checks

- Treat the Honkistas follow-on as its own event state with a single authoritative trigger decision after the preceding rival victory. Define eligibility, probability, progression gating, cooldown/repeat behavior and encounter intensity; no numerical rates or thresholds have been chosen by the owner.
- Record the already-earned event/rival victory and rewards before handing off. Prevent duplicate rewards, duplicate spawns and multiple clients independently rolling the same encounter. Define decline/exit/failure and recovery behavior without erasing earned progress.
- Arbitrate overlapping rival and Honkistas triggers. Explicitly decide whether any further chain can occur and prevent unintended recursive spawning. This safety requirement must not become an arbitrary content/roster/participation cap.
- Give the takeover a recognizable reveal and escalating original clown choreography, fake-outs and musical responses. Fake-outs must still have learnable tells and fair escape/counter windows. Keep player control responsive and hazards readable through peak effects.
- Explore an original/licensed clown arrangement that echoes the preceding motif and then transforms its timbre, accents and phrasing. These musical techniques are proposals pending reference review. Use only assets/provider capabilities with actual remix/synchronization rights; a Spotify selection alone is not a stem or remix pipeline. Retain a coherent original reactive-score fallback.
- Keep shared music ownership clear: transition from the defeated rival's cue to one Honkistas score rather than playing competing full tracks. Coordinate music, entrance, choreography, combo responses and temporary lighting with the same event timeline.
- Reconcile special-spawn eligibility with established faction defeat/ownership rules. Do not silently respawn a permanently defeated gang, restore stripped powers or invent immunity to campaign rules; define any exception explicitly.
- Reuse safe biome/terrain-aware course staging and cleanup. Do not destroy builds or strand the player when escalating or switching arenas; preserve canonical return and interruption recovery.
- Test each supported preceding gang/event type, random challenge wins/losses, simultaneous victories, multiplayer lag/late join, duplicate/out-of-order messages, save/reload, disconnect, death, dimension transitions and cancellation.
- Test musical handoff with missing/unsupported sources, muted play and reduced-flash/no-strobe options. Profile client/server frame/tick time, allocation, streaming, networking and startup, preserving capabilities and avoiding regressions.

- [ ] Specify the authoritative nested-encounter state machine and unresolved eligibility/progression decisions.
- [ ] Review CLOWN-MUSIC-001 and CLOWN-DESIGN-001; create an original Honkistas entrance, music transformation and readable high-challenge prototype.
- [ ] Integrate rewards/progress safety, campaign state, multiplayer synchronization, accessibility and complete temporary-event cleanup.
- [ ] Native-playtest surprise, intensity, fairness, replay variety and performance; attach evidence before claiming the mechanic is implemented.


### Honkistas combined costume, dance and music reference

**CLOWN-COMBINED-001 — owner-selected October 13, 2026:** [pestrica.fan.page video](https://www.tiktok.com/@pestrica.fan.page/video/7693937868900797710).

The owner explicitly selected this clip for **costume, dance and music** inspiration. Preserve all three reference roles. The accompanying [pestrica.fan.page profile](https://www.tiktok.com/@pestrica.fan.page) is an additional discovery/source link; do not treat every profile post as individually reviewed or approved. The account name does not establish original authorship or asset rights.

Detailed audiovisual review is pending. Record observed clothing/silhouette, dance phrases/contacts/transitions and identifiable musical cues only after direct inspection; do not invent track metadata. Relate reviewed cues to Honkistas' original CPM designs, high-challenge nested surprise entrance, recognizable clown-styled music takeover, and normal/skating crew choreography. Retain CLOWN-DESIGN-001 and CLOWN-MUSIC-001 as separate references rather than replacing them.

- [ ] Review the specific clip across costume, dance and music, identifying original sources where available.
- [ ] Select relevant original design and movement targets and test them in playable on-foot/skating sequences.
- [ ] Resolve production permissions and authorized music/animation sources; a reference link alone is not an implementation or redistribution license.


## Named Honkistas members: Sav and PESTRiCA

**Owner-selected members, October 13, 2026:**

- **Sav**: full-character reference [ranmaruspov](https://www.tiktok.com/@ranmaruspov). The owner explicitly named her Sav.
- **PESTRiCA**: full-character reference [pestrica.fan.page](https://www.tiktok.com/@pestrica.fan.page), extending CLOWN-COMBINED-001. Preserve this exact name capitalization. The trailing period in the latest pasted profile URL is treated as punctuation; the earlier exact profile link is retained.

Make both standout, memorable Honkistas members with distinctive detailed CPM designs, recognizable silhouettes, hair/accessory secondary motion, expressive presentation and exceptional dance/skating performances. They are members of the same gang, not new factions or reputation aliases. Individual personalities, roles and ability kits need design; no invented biography is an owner-approved fact.

Full profile review is pending. Record observed character traits and original attribution accurately; a fan-page username does not establish original authorship. Confirm applicable likeness and asset permissions before production reuse, and do not imply endorsement. Reference-inspired skins and original in-game characterization must preserve each member's recognizable identity.

## Major progression goal: recruitable character collection

**Explicit owner requirement:** high reputation unlocks opportunities to recruit memorable members from **each gang** into the player's own gang, including Sav and PESTRiCA. Aim for the satisfaction of discovering favorites and building a character collection that the owner described as Pokémon/“animo”-inspired. Do not turn this into generic interchangeable recruits or assume a particular implementation from the inspiration names. The intended loop centers on memorable individuals and meaningful crew collection; exact roster size is not capped by this documentation.

### Challenger reveal and recruitment victory gate

**Latest owner clarification:** use a Super Smash Bros.-inspired character-unlock flow: **reach the qualifying high level/reputation → present a dramatic challenger screen → defeat that specific character's challenge → unlock recruitment into the player's gang**. High reputation grants the opportunity, not automatic recruitment. The precise metric/rank/threshold and encounter rules remain unspecified; reconcile canonical gang reputation and any separate player level rather than silently equating them.

Create original JetSetCraft presentation, art, audio and UI. The reference is the excitement and structure of a challenger reveal, not copying Smash assets or claiming its internal implementation. Character challenges should showcase that member's signature skating/dance/combat abilities as appropriate; the exact win condition is a design task rather than an assumed fight-to-death mechanic.

Recruitment challenges are distinct from Honkistas' random surprise-after-surprise event. Define how the two interact; defeating a random rival encounter must not silently recruit every member or bypass individual unlock prerequisites.

### Unlockable member skins

**Explicit owner requirement:** unlock skins for these characters inspired by the supplied references. Keep each member recognizable while offering distinctive costumes/outfit variants and genre-appropriate styling. A skin is attached to a stable character identity, separate from recruiting that character and separate from renaming the gang. Individual skin unlock conditions are not yet selected. Do not introduce paid purchases, loot boxes, time-limited scarcity or stat bonuses without a separate design decision.

### Proposed implementation and acceptance backlog

- [ ] Define stable gang/member/skin IDs, named character catalogs, discoverability, individual profiles and a readable collection/crew-management interface.
- [ ] Define canonical reputation/level eligibility and the threshold for each member; keep locked, eligible, challenge available, challenge active, defeated/unlocked and recruited states distinct.
- [ ] Prototype an original challenger reveal with a strong musical entrance, clear character identity, readable challenge conditions and a safe transition to the encounter.
- [ ] Build distinctive character challenges and a proposed retry route after loss, exit or interruption. Define retry timing and rewards without inventing approved numerical values or erasing prior progress.
- [ ] Commit unlocks only on an authoritative valid victory; prevent duplicate unlocks, duplicate recruits, reward farming and bypass through repeated UI/network events.
- [ ] Define whether recruitment creates a playable roster entry, summons an allied companion or transfers a persistent NPC; reconcile rival-roster continuity, world uniqueness, friendship/ownership, party assignment and safe release/dismissal behavior.
- [ ] Preserve character identity/progress across skins, gang renames, save/reload, death, dimension changes and multiplayer reconnects. Define personal collection versus shared-world membership, host/server permissions and simultaneous recruitment conflicts.
- [ ] Design unique model sheets and signature performances for Sav and PESTRiCA, then equivalent memorable recruits across all supported gangs. Keep reference review/provenance status explicit.
- [ ] Implement reference-inspired cosmetic skin unlock/equip/preview and canonical restore, with compatible rigging, hair/clothing motion, equipment, skating and solo/crew choreography. Missing assets must fail safely without corrupting the roster.
- [ ] Reconcile recruited members with existing borrowed powers, control, permanent faction defeat and Honkistas' special event. Cosmetic or roster changes must not restore stripped powers or revive defeated factions accidentally.
- [ ] Test skill readability, fair win conditions, repeat attempts, reveal skip/reduced motion, muted cues, accessibility, late join, lag, duplicate/out-of-order events, rollback/reload and event cancellation.
- [ ] Profile large character/skin catalogs, model loading, animation, secondary motion, networking, UI memory and startup. Improve performance in parallel without arbitrary collection/content/population caps, capability loss or regressions.
- [ ] Attach exact-build native gameplay evidence and playtest memorable identity, collection satisfaction, crew attachment and replay variety before marking implementation complete.

This records requirements and a proposed acceptance plan. No character, skin, recruitment flow or challenger screen is claimed implemented.


### Honkistas roster additions: Tay, mennah and Plague

Owner-selected October 13, 2026: add **Tay** and **mennah** as two named Honkistas members using the [smeepycats profile](https://www.tiktok.com/@smeepycats), and add **Plague** using the [slimthicccsenpai profile](https://www.tiktok.com/@slimthicccsenpai). Preserve these spellings, including lowercase mennah. They join Sav and PESTRiCA: five explicitly named members, not a roster cap.

**Plague primary look, dance and costume reference:** [owner-selected clip](https://www.tiktok.com/@slimthicccsenpai/video/7550674610161863966). This specific clip takes priority for the main design; retain the profile for additional reference discovery. Detailed visual review is pending.

All five share the high-reputation/level eligibility -> original challenger reveal -> individual challenge victory -> recruitment-unlock progression and reference-inspired unlockable skins. Give each distinctive detailed CPM models, memorable signature performances, genre-appropriate styling and stable hair/accessory motion. Exact thresholds, abilities, personalities and encounters remain design work.

Profile review and exact visual-to-name mapping are pending. Do not guess which depicted person corresponds to Tay or mennah, invent biographies or imply endorsement. Confirm attribution and applicable production permissions for referenced likenesses/assets.

- [ ] Review the profiles and specific Plague clip; establish individual reference mappings and model sheets.
- [ ] Design each member's signature challenger encounter, recruitment behavior, crew choreography and skin variants.
- [ ] Test distinct IDs, persistence, multiplayer, animation/physics, event cleanup and performance/startup without capability loss or regressions.


## Honkistas CPM quality benchmark and skating physics (2026-10-20)

The owner supplied **PierrotThefreakCircus-Model.cpmmodel** as the desired NPC-quality benchmark, specifically for the Honkistas. Preserve the embedded credit **Model by: Hibrook | Game for: Neko Bueno**. The container checksum and embedded preview were inspected; the actual rig, animation and existing physics are not yet native-verified.

Hair and eligible loose costume/accessory parts must respond to skating acceleration, braking, turns, spins, airtime, landings, grinds and dance, with anchored roots, damping, collision handling and clean state resets. A generic looping sway is not sufficient. Preserve the existing named members, aliases, recruitment, skin unlocks and nested-surprise encounter.

[Full quality, physics and acceptance specification](https://github.com/Herbertofury/JetSetCraft/wiki/Honkistas%E2%80%90NPC%E2%80%90Quality%E2%80%90and%E2%80%90Skating%E2%80%90Physics) · [Mod implementation checklist #38](https://github.com/Herbertofury/JetSetCraft/issues/38). These are required development goals, not a claim that the supplied model is licensed for redistribution or that the feature is shipped.
