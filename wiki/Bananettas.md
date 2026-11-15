# Bananettas — French-Language Bubblegum Pop / New-Wave Street Gang

> **Status (2026-11-15):** The project owner confirmed **Bananettas** as a new JetSetCraft gang name, matching the playful naming family of Honkistas, Gabberinas, Phonkistas and Footworkas. The owner also selected **"Le Banana Split" by Lio** as the gang's **first MUST-ADD track**. The design, runtime integration, authorized audio, and finished art remain pending; **this is a design record, not a shipped encounter**.

[Music Library](Music-Library#bananettas) · [Street Culture](Home) · [Gang Profile Standard](Gang-Profile-and-Reference-Standard) · [Music Integration](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/MUSIC_INTEGRATION.md)

## Owner-confirmed foundation

| Field | Canonical decision |
| --- | --- |
| Gang name | **Bananettas** |
| Stable *proposed* ID | `jetsetcraft:bananettas` (design only; **not runtime registered**) |
| Gang type | Distinct human music-culture crew; not a rename of Signal '81 or any existing mob gang |
| Music identity | Playful French-language / Francophone **bubblegum pop**, bright new wave, early synthpop and candy-colored late-'70s / early-'80s street culture |
| First MUST-ADD | **Le Banana Split — Lio** (original 1979 short French-language single, ~2:33 target) |
| Confirmed vs suggested | **Name and track** are owner-confirmed. Styling, lore, member lineup, locations, dances, abilities and exact cues below are proposals. |

**Cultural precision:** The scene is French-language and Paris-pop inspired; Lio is a Belgian singer, not evidence that the singer herself is French. This gang should feel exuberant, cheeky and lively—not like the darker/signal-driven **Signal '81** crew.

## First must-add song — owner selection

| ID | Artist / recording reference | State |
| --- | --- | --- |
| **BANANETTAS-MUSIC-001** | **"Le Banana Split" — Lio**. [Original 1979 7-inch single/discography](https://www.encyclopedisque.fr/disque/65862.html); [Spotify listening reference: track 1 (2:33) on *Best Of: Les Pop Songs*](https://open.spotify.com/album/5m0ka2doYDeh54QWdkaxT3); [current digital version comparison](https://www.qobuz.com/us-en/album/le-banana-split-remix-album-lio/0190374628204) | **MUST ADD for Bananettas only**, owner-selected 2026-11-15; audio/rights/runtime **not integrated or verified** |

**Original-vs-remix lock:** The target is the playful, short **1979 French-language original single** (about **2:33**). A later streaming compilation includes a same-named 2:33 track, but its exact mastering/ISRC identity relative to the original 7-inch release has **not yet been established**. Do not silently replace it with the extended disco recording, an English version, a 1995 remix, a radio mix, a 2016 club version, a cover, or a different artist's recording. The album links are identification/listening references, **not an owner-supplied exact Spotify track ID or a game-use license**.

Discography credits commonly name **Jay Alanski** and **Jacques Duvall (Hagen Dierks)** as writers and **Marc Moulin** as producer. Confirm the final licensed master and complete edition-specific credits before distribution. The original single was issued in **1979**, while the linked Spotify compilation is from **2005**—these are different release dates, not contradictory claims.

No copyrighted recording, video, cover art or sample is included here. The project has **not** obtained or independently verified a Bananettas-specific synchronization/distribution license, source master, game-ready audio or tested music trigger. The general owner report of permissions elsewhere in the music library must not be treated as a verified agreement for this particular recording. Actual beat markers, BPM, intro timestamps, loops, dance timings and stems have **not been measured**.

## Proposed gang aesthetic — not final owner approval

- **Mood:** mischievous bubblegum-pop starlets and skating troublemakers: upbeat, witty, bright and a little absurd, with a distinct late-1979/early-1980s French-language pop flair.
- **Color direction:** banana yellow `#FFD84B`, strawberry pink `#F25A92`, cherry red `#E9425B`, mint teal `#56D5C3` and vanilla cream `#FFF5DD`.
- **Icon and graffiti brief:** original banana/cherry/waffle-cone geometry with dynamic comic shapes; three collectible stages (**Mark → Throwie → Masterpiece**). No copying Lio's single sleeve, existing Banana Splits brand assets or third-party characters.
- **Wardrobe:** big graphic barrettes, playful colored jackets, patterned socks, contrasting skates, plastic-look earrings and wide color-block panels. Make original silhouettes and offer varied NPC styling rather than cloning a music-video costume.
- **Potential territory:** a neon ice-cream arcade with roller-disco lanes, pastel shutters and rotating dessert-sign challenges. Pure concept; **no structure or map has been built**.
- **Proposed gameplay hook:** cheerful pastry-palette graffiti battles and call-and-response crew dances, with readable score prompts and optional reduced-flash/reduced-motion effects. Do not assert choreography matches the song until a properly sourced recording is analyzed.
- **Boundary with Signal '81:** Bananettas owns dessert-colored bubblegum-pop and playful glamour; Signal '81 owns retro broadcast/new-wave aesthetics and its different approved track.

## Concrete integration checklist

- [x] Record owner-confirmed name, reference ID and first must-add artist/title without inventing a Spotify track ID.
- [x] Create machine-readable gang design record in `docs/gangs/bananettas.json`.
- [x] Add music reference to `Music-Library` and an import-preparation-only JSON catalogue entry.
- [ ] Verify the exact original short recording/master, ISRC, edition credits and authorized acquisition route.
- [ ] Confirm game synchronization and distribution rights with the actual copyright holders; retain a rights receipt for the exact master.
- [ ] Implement a real human-crew `jetsetcraft:bananettas` runtime identity, registry and save-safe reputation/territory/player interaction; **do not map the gang onto an arbitrary vanilla mob**.
- [ ] Implement **gang-specific** music selection so a future importer cannot make this recording play for unrelated crews through current global `home`/`rush` contexts.
- [ ] Obtain permitted high-fidelity source and import with `tools/import_music.py`; keep audio, original hash and encoded hash in receipts; never download/rip from streaming links.
- [ ] Review and finalize costumes, three-stage graffiti, NPC roster/leader, territory location, animations and stage choreography with the owner.
- [ ] Test native client and dedicated server behavior: music mute/fallback, crew-specific filtering, save/rejoin, territory restoration, multiplayer, accessibility and performance.

This profile deliberately distinguishes **owner approval**, **proposed creative direction**, **metadata preparation**, **rights clearance** and **playable integration** so later work cannot mistake a wish-list entry for a finished gang.
