# Signal '81 — Retro Broadcast / New-Wave Street Gang

> **Status (2026-11-15):** The project owner selected **Signal '81** as the gang's **canonical name** and specified **two must-include songs**. Visuals, roster, choreography, gameplay, exact music cues, licensing, and in-game activation remain **proposals / pending**. This is a new human music-culture crew concept, not an existing creature gang or a shipped rival encounter.

[Music Library](Music-Library#signal-81) · [Street Culture](Home) · [Shared gang reputation](Hangouts-Territory-and-Reputation) · [Music integration constraints](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/MUSIC_INTEGRATION.md)

## Owner-confirmed foundation

| Field | Canonical decision |
| --- | --- |
| Name | **Signal '81** |
| Stable *proposed* ID | `jetsetcraft:signal_81` (design registry, **not yet runtime-registered**) |
| Role | Distinct human music-driven street/rival gang; not a rename of any existing gang |
| Core influence | 1980s new-wave / synthpop / dark broadcast imagery crossed with the late-'80s to early-'90s Jazz-cup/Memphis graphic aesthetic discussed with the owner |
| Required selections | **Break Through the Noise** — **Brighter Than a Thousand Suns** (2025) and **She's a Star — Kano** (1981), both exact owner-linked Spotify recordings |
| Approval | Name and both must-add recordings only; other details below are exploratory design proposals |

**'81 is an aesthetic cue, not a release-year requirement for all gang tracks.** “Break Through the Noise” was released **December 4, 2025**; the second approved song **“She's a Star”** belongs to Kano's original **1981** era. Keep the modern original release and the period-authentic recording distinct.

## Owner-approved must-include recordings

| Reference ID | Recording, artist and sources | Creative-use priority | Real-world status |
| --- | --- | --- | --- |
| **SIGNAL81-MUSIC-001** | **[Break Through the Noise](https://open.spotify.com/track/4rXK0sJImyiw9D7pFJclVS?si=b96b23c795924c04) — Brighter Than a Thousand Suns**. [Artist's release and credits](https://brighterthanathousandsuns.bandcamp.com/track/break-through-the-noise); released 2025-12-04 | **Must include for Signal '81**. Proposed gang introduction, radio takeover or main chase set; exact placement and synchronization **not approved** yet | **Owner-selected and title/artist/release verified; no master, game-sync or distribution clearance supplied; not playable in game** |
| **SIGNAL81-MUSIC-002** | **[She's a Star — Kano](https://open.spotify.com/track/2r1J3yv6Ozhi5LYxybeQen?si=c45fe7d7444241ba)**, original 1981 song on [*New York Cake*](https://open.spotify.com/album/2v1VGo4RYpLnSxDKC3ROUb) ([album credits](https://www.qobuz.com/gb-en/album/new-york-cake-kano/cbnmw8outfd6a)) | **Must include for Signal '81 only**. Proposed retro-disco broadcast, skate run or gang-rival challenge; exact beat cues/placement not yet approved | **Owner-selected exact Spotify ID; artist/title and original album verified; specific linked edition/ISRC, source audio, sync/distribution permissions and gameplay pending** |

**#001 provenance:** The project owner supplied the **exact Spotify URL** on **2026-11-15**. Artist-controlled Bandcamp release gives the recording date and performers, **Angelika Roswell** (vocals, drums, synths) and **Randy Roswell** (guitars, synths, programming), but credits do not grant a mod-game reuse license.

**#002 provenance:** The owner separately supplied [the exact Spotify track ID `2r1J3yv6Ozhi5LYxybeQen`](https://open.spotify.com/track/2r1J3yv6Ozhi5LYxybeQen?si=c45fe7d7444241ba) on **2026-11-15**. The performer is **Kano, the Italian Italo-disco / synth-funk group**, *not* the British rapper. **“She's a Star”** appears on the group's [1981 *New York Cake*](https://open.spotify.com/album/2v1VGo4RYpLnSxDKC3ROUb) as a roughly **5:50** album recording; the duration, ISRC and master of the *exact selected Spotify URL* still need verification. [Qobuz album credits](https://www.qobuz.com/gb-en/album/new-york-cake-kano/cbnmw8outfd6a) identify Stefano Pulga, Luciano Ninzatti, Barbara Lynn Addoms, and Anna Milena Cantu as contributors; confirm credits against the licensed master. Keep early-1980s funk/disco origins accurate rather than incorrectly calling this a British grime recording.

No audio has been downloaded, copied, reverse-engineered, sampled, uploaded to GitHub, or represented as an installed soundtrack. Do **not** infer music tempo, phrase boundaries, BPM, loop points, or a synced dance from public track metadata. Check the exact rights scope for game synchronization, pack distribution, and platform use before acquiring an authorized master. The gang music assignment is **not** a general-game playlist selection.

## Proposed visual identity — for owner review

- **Broadcast icon:** a bent television antenna, stylized number **81**, three offset concentric signal arcs and a broken horizontal raster line; original iconography, not a corporate TV/MTV logo.
- **Color direction:** electric turquoise `#34D2CE`, ultraviolet `#7A35D5`, interference red `#F04463`, off-white `#F7F3ED`, and midnight charcoal `#181828`. These colors are proposed, **not approved assets**.
- **Graffiti language:** rough halftone printing, diagonal TV-scan breaks, offset registration, color-band smears, and asymmetric Memphis-influenced shapes. Reference the *feel* of the 1990s Jazz paper cup, **do not trace or copy its protected artwork**.
- **Wardrobe:** patterned windbreakers, cropped boxy jackets, cassette belts, mismatched skate wheels, retro audio gear and asymmetrical accessories. Keep readable silhouettes across diverse NPC models.
- **Place:** an abandoned local TV station / rooftop transmitter beside a roller rink or shopping arcade; a venue concept, not a created Minecraft structure.

## Proposed music and skating encounter language

**Original fantasy: steal the airwaves instead of stealing the streets.** Signal '81 can paint transmitter towers, occupy rooftop routes and issue broadcast duels to challenge rival neighborhoods. A deliberate analog-signal motif could visually connect a music trigger to a territory state without needing to access a real Spotify account.

1. **Tune-in / entrance:** screen-snow ripple, antenna icon and a clear **SIGNAL 81** title reveal. Use an **original** stinger, not a ripped recording sample.
2. **Street-run:** interconnected roof-to-rink lines with tricks, controlled powerslides, crossovers and crew formation moves, respecting physically plausible ride speeds.
3. **Challenge climax:** distinct readable radio-channel/TV scan effects that signal the next movement pattern; rival player input and score feedback remain legible, even with reduced-motion enabled.
4. **Sign-off:** original transmitter-tag flourish and independent territory/reputation receipt. No rewards, thresholds or NPC roster are approved yet.

All four phases are design **proposals**. Avoid claiming exact cue timing or fully reactive music before an authorized audio master is analyzed and real gameplay is tested.

## Integration boundaries and concrete next work

The shipped game currently supports **mob-linked gang identity** through `GangRegistry` and a separate **client home/Rival Rush soundtrack catalogue**. Neither proves this human faction is spawned, challengeable, or supported by a gang-filtered music playback route. The catalogue metadata for this song may exist for credits and future import preparation; **catalogue metadata is not a bundled recording**.

- [x] Owner-approved canonical gang name and **two must-include recordings** recorded with exact links and credited artists.
- [x] Add **She's a Star — Kano** as `SIGNAL81-MUSIC-002`, recording its genuine 1981 lineage while keeping exact streaming edition, ISRC and audio acquisition pending.
- [x] Dedicated gang concept page, Music Library reference and machine-readable design record.
- [x] Optional soundtrack **metadata only** prepared for discovery/import tooling; no copyrighted audio bundled.
- [ ] Decide owner-approved palette, logo, outfit silhouettes, roster, leader and final lore.
- [ ] Decide whether this is a general human rival, a special event, or has additional recruitable NPC roles; connect it to a **new appropriate human-crew runtime**, not arbitrary mob-entity mappings.
- [ ] Implement stable identity, territory/reputation, encounter selection, UI and save/load/multiplayer acceptance with no accidental duplication of rival gangs.
- [ ] Add a **gang-specific playback filter** before enabling an actual recording. Current `home`/`rush` contexts alone are **not** equivalent to a Signal '81 association.
- [ ] Confirm recording-specific sync/distribution rights and obtain authorized master/stems; keep checksums and complete artist/release credits in the import receipt.
- [ ] Audit audio accurately for cues and original choreography; set animation timing only from measured, permitted audio.
- [ ] Produce original graffiti Mark → Throwie → Masterpiece, outfits, signature entrance and no-flash/reduced-motion effects.
- [ ] Validate in a native client and dedicated server: entering/leaving territory, muted play, missing master silent fallback, NPC lifecycle, rejoin, save/reload, multiplayer, clean world restoration and performance.

See [Gang Profile and Reference Standard](Gang-Profile-and-Reference-Standard) and [Reactive Music Engine](Reactive-Music-Engine-and-Killer-Instinct-Reference) for shared acceptance standards.
