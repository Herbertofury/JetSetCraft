# Ocarina of Cumbia — Rare Cumbias × Zelda Music Encounter

> **Owner-requested priority Easter egg — 2026-11-15. Design and asset-acquisition goal, not shipped or tested.** Part of [The Cumbias](Cumbias‐and‐Multi‐Rival‐Gang‐War), [Music Library](Music-Library) and the [Portable Reactive Music Engine](Reactive-Music-Engine-and-Killer-Instinct-Reference). Preserve vanilla/modded mobs, Boombox, gang events, music settings, existing soundtracks and the standalone high-fidelity source pipeline.

## The one-line promise

**An extraordinarily rare Cumbias encounter transforms into a Hyrule-meets-cumbia street celebration: an ocarina calls the rhythm in, recognizable Zelda characters join the Cumbias, the musical battle reacts to the player's moves, and the discovery becomes a genuine little gaming legend.** It must make players laugh, dance, and want to show friends, without becoming a tedious farm, permanent combat interrupt, cheap joke, or ordinary random spawn.

**Original owner video:** https://www.tiktok.com/t/ZPLK2Fnk7/ . The user explicitly designated this as the inspiration for *Ocarina of Cumbia*, its **rare spawn**, **music**, and **Zelda characters**. The short-link video/creator/sound metadata could not be independently resolved in this session. The broader ocarina + cumbia mashup trend is documented independently, but no specific claims about this clip's shots, characters, arrangement, instrumentation, vocals or creator are verified by that reporting. [Related global creator/music goal](Music-Library).

## Event identity and casting

- **Public event name:** **Ocarina of Cumbia**; stable event key `jetsetcraft:ocarina_of_cumbia` and source reference ID `JSC-EGG-CUMBIA-OCARINA-001`.
- **Faction ownership:** Cumbias. It is a rare variant of an already eligible Cumbias performance/encounter, not a new generic faction, background mob, or replacement for other gangs.
- **Crossover intent:** recognizable **Zelda characters** joining Cumbias musicians and dancers. Design a small cameo troupe with **Link and Princess Zelda** as obvious anchors; **Saria and Ganondorf** are potential additional surprise counterparts. The exact video cast is **unverified** and final release cast remains subject to confirmation from the video plus the relevant permissions/source assets. Preserve the explicit Zelda character goal, without claiming the user's existing Killer Instinct permission covers Zelda.
- **Presentation:** joyful, warm, nostalgic yet surprising. A real cumbia groove and ocarina melody, playful fourth-wall comic timing, vibrant festival light, and sharply differentiated character choreography. No generic stereotypes; consult cumbia performers about dance, rhythm and instrumentation.
- **Character/source quality:** author/obtain full-resolution cinematic human-scale/faithful crossover rigs, materials, facial performance and dance source clips as the source-of-truth; Minecraft models/audio are downstream exports. Preserve asset origin, rights/credits and export hashes. The future standalone game must not be limited to blocky textures or lossy export masters.

## Rare encounter contract

1. A normal Cumbias performance/faction event first passes all established eligibility/ultra-rare encounter rules. Only then roll the **Ocarina of Cumbia** variant, using server-owned seeded state. Never add a per-tick global mob scan, broad biome spawn override or unwanted player teleport.
2. **Proposed initial tuning:** around **1 in 100 eligible Cumbias events (1%)**, independently configurable for pack authors and adjustable using actual encounter-frequency playtests. Because ordinary natural gangs are already rare, the *overall* chance remains very rare. Count and expose real eligible-roll frequency for tuning; do not promise this exact percentage as finalized balance.
3. Require a valid, safe stage/surface and music/performer readiness; otherwise resolve as the original ordinary Cumbias event without repeated retry rolls, spam, half-spawned figures or silent fake success. Respect protected locations and normal server permissions.
4. Avoid repeated sightings in a short area/session through seeded cooldown/deduplication without capping the total world, content, players or playable gang population. The variant must never trigger a second chain of surprise events.
5. Provide a **dev/test-only deterministic force trigger** that proves the full event without undermining the genuine rare public discovery. An opt-in post-discovery replay via a collection/jukebox setlist or Boombox memory is acceptable; it must not change the wild chance.

## Show flow: the music is the magic

1. **Foreshadowing:** during a qualified Cumbias event, a brief authentic ocarina phrase cuts through the mix, followed by a recognizably cumbia-style response. Player movement and combat controls stay responsive; music is routed through the normal adjustable game sound buses.
2. **Reveal:** the Cumbias leader stops for a theatrical beat, musical lighting sweeps the street, and Zelda-themed guests appear with a polished emote/landing, not harsh blocky pops. A concise event title/now-playing reveal includes real artist/track credits.
3. **Dance and play:** recognizable character-specific dances, callbacks, ocarina-led counterpoints, band keyboard/bass/percussion, optional dance battle or skate/trick freestyle. The player may join or watch without being dragged into compulsory rhythm controls.
4. **Reactive escalation:** on every genuine earned combo, successful arrow/phrase, break, counter or dance duet, the **Portable Reactive Music Engine** brings in matching percussion, crowd claps, melodic flourishes, character responses and quantized arrangement layers. Big plays earn a musical payoff while immediate hit feedback remains instant. Every stem/phrase is grounded in actual beat/loop analysis, not guessed TikTok BPM.
5. **Encore:** successful performance ends with a satisfying synchronized reveal/final flourish, music resolves on a phrase boundary, and a discovery record records the exact variant and credits. Failing or leaving resolves cleanly without punishing normal gameplay.
6. **Reward:** optional original **Ocarina of Cumbia** collectible/cosmetic instrument, unlockable dance emote, character-themed title/banner or replay entry. Do not confer unbalanced combat bonuses. If rights/asset scope is incomplete, keep unreleased crossover-dependent rewards gated rather than substituting uncredited media.

## Music, artist discovery, and source manifest

| Field | Required value / current state |
|---|---|
| Event/video source | https://www.tiktok.com/t/ZPLK2Fnk7/ — owner-selected |
| Clip creator/performer | **Unverified**. Preserve author discovery as an active research task |
| TikTok audio label/sound ID | **Unverified**. Inspect original post and sound attribution |
| Original composition/arrangement | **Unverified**. Distinguish original cumbia, Zelda melody, edited/remix sound and cover artist |
| Exact master, stems, musical key, BPM and latency | **Not acquired/verified**; no playable integration claim |
| Other artist reference | https://www.tiktok.com/t/ZPLK2mF7q/ — also owner-selected for author's music; whether the posts share a creator is **not verified** |
| Credits and use permissions | Record creator, composer, arranger, vocalist/instrumentalist, publishers/master owner and any franchise-specific terms with exact asset provenance; user has not reported a Zelda-specific permission in this exchange |
| Cross-platform source | Preserve lossless music master/DAW project and highest-available original rig/animation/texture source; export Forge and future standalone derivatives independently |

The research goal is to **find the actual creator(s) behind both linked TikToks, explore the complete relevant catalogue, and prioritize their strongest music for JetSetCraft**. Where one clip uses another creator's sound, distinguish uploader, musician, composer, remixer, and recording owner. Do not silently assume both links feature the same musician. Capture exact audio/video metadata and artist release links before assigning track names. Preserve the earlier `JSC-MUST-TIKTOK-20261115-001` entry as a **separate** unresolved requested song.

## Integration and acceptance backlog

- [ ] **OCA-001** — Resolve both TikTok short links to creator IDs, canonical video IDs, exact sound IDs and verified artist/recording credits; record source URLs, retrieval date and whether creator identities match. Preserve uncertainty when unavailable.
- [ ] **OCA-002** — Research the artist's catalogue and official masters; audition and shortlist distinctive tracks, with culture/story/reason for use and rights/source status for each recording.
- [ ] **OCA-003** — Obtain/verify actual source master/stems and Zelda-specific character/music/visual asset scope, if intended for shipping; preserve high-quality native sources and attribution. Never infer Zelda permission from Killer Instinct permission.
- [ ] **OCA-004** — Implement additive, server-seeded **rare variant selection** only after Cumbias eligibility; configurable 1% initial target, cooldown/deduplication and zero broad vanilla/modded spawn takeover.
- [ ] **OCA-005** — Author character cast, choreography, high-fidelity scene and Minecraft derivatives. Ensure human-scale source quality and truthful placeholder/license distinction.
- [ ] **OCA-006** — Create a fully playable cinematic reveal and optional dance/skate challenge, triggered by the authoritative event state and accessible without forced input. Integrate Mortal Kumbia where available.
- [ ] **OCA-007** — Route ocarina and band stems through the shared reactive engine (not an independent timing engine), with verified tempo grid, quantized transitions, instantaneous hits, crowd/reaction layers, real credits and proper volume buses.
- [ ] **OCA-008** — Persist discovery, expose collectible/replay and support multiplayer spectators, late joins, restarts, asset-missing fallback, pause/resume and correct cleanup.
- [ ] **OCA-009** — Prove deterministic event probability over many seeded eligible events, zero event duplication, no global spawn/performance regressions, real-source music sync and native gameplay with one force-triggered and one naturally eligible path.
- [ ] **OCA-010 · GATE** — No in-game "implemented" claim until exact owner reference is resolved, intended assets/sound identified, relevant permissions/source quality recorded, intended cameo/music actually playable, release-compatible tests pass, and the live wiki/Drive master source are synchronized.

**State as of 2026-11-15:** design requirement recorded. No media was extracted from the two TikToks, no character/music asset was imported, and no rarity or playback was tested. The broad cumbia/ocarina trend is not proof of what appears in the exact user-supplied clip.