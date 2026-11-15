# Décalco Mambo — Rare Cumbias × Bananettas Face-Off

> **Owner-confirmed special encounter and soundtrack (2026-11-15), DESIGN ONLY — NOT SHIPPED.** The player should occasionally face **Cumbias and Bananettas together**. The owner supplied **[“Le Mambo du décalco” — Richard Gotainer](https://open.spotify.com/track/10hnzFqOkWifT35HfvbaNc?si=edfb79914dc9426e)** as the exact, rare-event-only song. “Bannistas” refers to the already named **Bananettas** crew.

[Bananettas](Bananettas) · [Cumbias](Cumbias‐and‐Multi‐Rival‐Gang‐War) · [Music Library](Music-Library) · [Ocarina of Cumbia](Ocarina-of-Cumbia-Easter-Egg)

## Owner-selected foundation

| Field | Decision |
| --- | --- |
| Owner reference | `JSC-RARE-MAMBO-20261115-001` |
| Proposed stable event ID | `jetsetcraft:cumbias_bananettas_decalco_mambo_faceoff` |
| Rival crews | Cumbias and Bananettas; a rare opportunity for the player to confront both |
| Exact owner music | [**Le Mambo du décalco — Richard Gotainer**](https://open.spotify.com/track/10hnzFqOkWifT35HfvbaNc?si=edfb79914dc9426e), Spotify ID `10hnzFqOkWifT35HfvbaNc` |
| Music scope | **ONLY this rare two-crew face-off**. Not ordinary gang radio, general home/Rush music, or Ocarina of Cumbia |
| Rarity | The owner requested **rare**; no exact probability was approved |
| Status | No implemented event, authorized master, playable sound or runtime rarity test |

The underlying single dates to **1982** ([original single discography](https://www.encyclopedisque.fr/disque/7264.html)); Richard Gotainer and Claude Engel are credited in that release. Check the *exact linked recording edition*, master/ISRC, performer credits, synchronization and distribution rights before importing audio. A Spotify link is not a license.

## Proposed implementation and acceptance

1. **Eligibility first:** only roll after an existing server-valid **joint** Cumbias/Bananettas event can begin. Both human crew runtimes and a safe encounter venue must be present. No per-tick global scanning, arbitrary NPC spawning or forced player teleportation.
2. **Rare selection:** proposed starting setting **1% once per eligible event**, configurable, server-owned seeded RNG, cooldown and one-time event receipt. This percentage is a design suggestion, not owner-approved balancing. Record telemetry and add a deterministic test-only trigger.
3. **Distinct from Ocarina of Cumbia:** one rare variant at most per encounter. Never stack special encounters, hijack Ocarina's soundtrack, chain events or duplicate rewards.
4. **Face-off:** original sticker/decal-themed introduction, distinct dance/skate/graffiti responses from each crew, fair scoring and player decline/replay safeguards. Staging, rewards, crew animation, timings and final lore require review.
5. **Audio segregation:** obtain an authorized exact recording; implement an **event-scoped** music route before import. Current `home`/`rush` metadata cannot restrict this song to the encounter. Never add it as a generic Rush track. Missing master or mute gets a safe fallback.
6. **Test gate:** real-game event routing, client/dedicated server, rejoin, protected-world restoration, chance calibration, reduced flash/motion, beat calibration if actually rhythm-scored, missing audio and multiplayer correctness.

- [x] Owner-approved rare joint encounter and exact special-track URL saved in project/wiki
- [x] Independent event JSON and distinct music-scope restrictions documented
- [ ] Joint human-crew encounter, rare selection and event-scoped audio actually coded and tested
- [ ] Licensed master acquired, credits and exact edition verified, sound synced in real gameplay

**This is a production specification, not an in-game implementation.**
