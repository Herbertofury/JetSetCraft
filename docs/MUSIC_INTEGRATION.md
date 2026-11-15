# Crew-house and Rival Rush soundtrack

## Current implementation

The development alpha includes a **client resource-pack soundtrack player**, two identified recording entries, and a local audio importer. It plays available tracks in the owner's crew-house plot or during that player's active Rival Rush session. It fades in over two seconds, fades out over one second when leaving those activities, avoids an immediate repeat when another eligible track is available, and credits the title and artist in chat when a track starts.

Playback uses Minecraft's **Jukebox/Note Blocks** volume control. `/jetsetmusic off` disables this optional player; `/jetsetmusic on` re-enables it, `/jetsetmusic next` advances after the current fade, and `/jetsetmusic status` reports installed recordings separately from catalogue entries. This local preference is saved in `config/jetsetcraft-music.json`.

The player owns its own sound instances. It does not stop another mod's audio, control a Spotify account, fetch music from the internet, or analyze beats. Existing gang entrance stingers and Rush's dance-stop rewards remain their own systems. Ordinary vanilla music can overlap; use Minecraft's music volume control to choose the preferred mix.

**The two Sonic-related recordings are not bundled yet.** Their metadata is present, but no audio master was supplied in this implementation session. Missing optional recordings stay silent without issuing a missing-sound request on every tick. This distinction is deliberate: an identified track is not an installed recording, and an original placeholder is not a Sonic track.

## Identified recordings

| Track | Exact credited artist | Version/reference | Proposed use |
|---|---|---|---|
| **Mystic Cave Zone** | **Kr0w!** | [Owner's Spotify recording](https://open.spotify.com/track/3F9Vgaws51YS7pd3BlnWG0), 2023 | Home or Rush playlist entry; final placement should follow listening to the supplied master. |
| **Collision Chaos** | **SEGA SOUND TEAM** | [Owner's Spotify recording](https://open.spotify.com/track/4IuWcXy4xrmcTPVbvRsnWR), *SONIC THE HEDGEHOG CD Original Soundtrack (20th Anniversary Edition)*, 2011 | Home or Rush playlist entry; this is the linked Sonic CD recording, not an assumed Sonic Mania version. |

Titles, credited artists and album identities were read from Spotify's official public track metadata and oEmbed on 2026-11-01. The music was not auditioned here. No guessed BPM, beat offset, loop point, composer attribution for an individual track, or soundtrack stem data is shipped.

The project owner reported obtaining rights associated with Sonic Mania, SEGA SOUND TEAM, Kr0w!, Masato Nakamura, Tee Lopes and Naofumi Hataya. [`tools/music_rights_manifest.json`](../tools/music_rights_manifest.json) records that statement and the two exact recording references. The statement is the permission provenance supplied by the owner; the catalogue does not broaden it into unspecified recording-specific terms. When audio is imported, the generated receipt identifies the actual source and encoded file hashes.

## Owner-reported permission and credits

On November 12, 2026, the project owner reported that the authors of the recommended songs granted permission to include them in JetSetCraft provided they are credited. Each included recording must credit its artist, featured performers and release in the in-game track display and accessible soundtrack credits. This is the owner's report; the underlying agreements, any separate label or master rights, and audio masters have not been independently verified. Listing a track does not install it.

## Owner-selected must-include track

- **SIGNAL81-MUSIC-001 — Break Through the Noise — Brighter Than a Thousand Suns (Signal '81 gang)**: [Exact owner-supplied Spotify recording](https://open.spotify.com/track/4rXK0sJImyiw9D7pFJclVS?si=b96b23c795924c04) · [Official artist release](https://brighterthanathousandsuns.bandcamp.com/track/break-through-the-noise); single originally released **2025-12-04**. On **2026-11-15** the owner confirmed **Signal '81** as the gang name and this as its **first must-include track**. **Gang-specific assignment only; do not reclassify as general in-game music**. A metadata-only client catalogue entry and [dedicated gang concept](../wiki/Signal-81.md) preserve the request. The player currently supports only global `home`/`rush` contexts, *not* per-gang music routing, and no recording is bundled. Author/recording permissions, approved master, measured musical cues and gang-specific playback are pending. No permission statement for other artists extends automatically to this recording.

- **DASSHIT — BONESAW**: [Spotify reference](https://open.spotify.com/track/3iYkaU4qu9sMqef1mKW81c?si=588f044b0dd44cc2). The project owner selected this as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **LOSING.CTRL - HYPER TRIGGER OST — BONESAW**: [Spotify reference](https://open.spotify.com/track/6C28FJC1vPw81isMAK0bMN?si=a191b70ba2f2475b). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **MATCHup — BONESAW**: [Spotify reference](https://open.spotify.com/track/2pDvAM9c2MRHbGq6Vy8qhz?si=2c3c8990e93c4a58). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Shooting Star — Qteku**: [Spotify reference](https://open.spotify.com/track/7f9fa3IUf0NRv5gL1bmQlw?si=d5c0b9c60f2a456c). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Cash Flow — Hydraa**: [Spotify reference](https://open.spotify.com/track/4Ne4imQAtGcVyl5OkTgMBD?si=6a6b20391fab4d90). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Starlit Pursuit — Qteku**: [Spotify reference](https://open.spotify.com/track/0fsUe3oa8PdEZFkLNWyeam?si=d427e70fa0d44454). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Wasted — Hydraa, Jerez**: [Spotify reference](https://open.spotify.com/track/13Oe3EwA5DvGDfEZNlK7kV?si=0b75a3f96dc443d4). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Wait For Me — BONESAW**: [Spotify reference](https://open.spotify.com/track/6z8xYqV8Jez1kpMro8ptCd?si=4c7a06ecd9944d31). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **havin fun :3 — Qteku**: [Spotify reference](https://open.spotify.com/track/7k71st22xoGTzcQAjE1T2t?si=3ac2813765c24c37). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Circuit Breaker — Kinda Rad**: [Spotify reference](https://open.spotify.com/track/2O2aHSZNZwwuj1TQoaTPR1?si=c88e505c19fb45f2). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **Beyond Fuji ~富士の彼方~ — Tee Lopes**: [Spotify reference](https://open.spotify.com/track/0oSZhHwuO8o8DNqSrD4Zov?si=d27d4f502a784dfa). Selected by the project owner as a must-include JetSetCraft track on 2026-11-11. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

- **R3DHOT — BONESAW**: [Spotify reference](https://open.spotify.com/track/4Wz6n6DASRztYq9Z7ymXld?si=a752b80801274559). Selected by the project owner as a must-include JetSetCraft track on 2026-11-12. The owner reports author permission subject to credit; the recording master and any separate rights remain to be confirmed. This reference alone does not install or play audio.

## Importing the supplied masters

Install `ffmpeg` and `ffprobe`, then run the importer from the repository. Supply the project recording as a local file:

```bash
python tools/import_music.py \
  --track krow_mystic_cave_zone \
  --input /path/to/Mystic-Cave-Zone-master.wav \
  --output-pack build/music-packs/JetSetCraft-Krow

python tools/import_music.py \
  --track sega_collision_chaos \
  --input /path/to/Collision-Chaos-master.flac \
  --output-pack build/music-packs/JetSetCraft-Collision-Chaos
```

Each invocation creates a complete optional Minecraft 1.20.1 resource-pack folder. Copy the resulting folders into the instance's `resourcepacks` folder and enable both packs. Keeping each track in its own pack makes inclusion and replacement straightforward; Minecraft merges the distinct sound-event definitions. Use a new output directory when importing another revision. The importer refuses an existing output path and leaves the input recording intact.

The importer converts the first audio stream to stereo 44.1 kHz Ogg Vorbis, checks that the encoded result contains a valid audio stream of at least one second, and writes:

- `pack.mcmeta` for resource-pack format 15.
- The streaming `sounds.json` entry and encoded `.ogg` file.
- The track's title, artist, source reference and supported contexts.
- `CREDITS.txt` and `jetsetcraft-music-receipt.json`, including source SHA-256, encoded SHA-256, duration, codec, sample rate and channel count.

It stages a complete pack before publishing its output folder. It performs no network requests. Audio stays outside the normal source tree unless the project deliberately incorporates an approved distribution asset later.

## Adding another track

Add a metadata file under `assets/jetsetcraft/jetsetcraft_music/` in a client resource pack. The bundled examples are under `src/main/resources/assets/jetsetcraft/jetsetcraft_music/`. The JSON schema is small:

```json
{
  "title": "Track title",
  "artist": "Exact credited artist",
  "sound_event": "jetsetcraft:licensed/example_track",
  "audio_resource": "jetsetcraft:sounds/music/licensed/example_track.ogg",
  "contexts": ["home", "rush"],
  "volume": 0.65,
  "source_url": "https://example.com/recording"
}
```

The runtime accepts at most 256 metadata entries, validates local resource identifiers and a finite volume in `(0, 1]`, and ignores malformed entries. Playback requires both the actual local Ogg resource and a nonempty sound-event definition. A metadata URL is a reference for humans; the player never streams it.

The importer accepts another metadata directory through `--catalogue`, so a project-specific pack can be built without changing the mod. Filename IDs use lowercase letters, digits and underscores. The sound definition must have `stream: true` so a full music track is streamed from the resource pack rather than treated as a short effect.

## Bananettas owner-selected gang and first must-add — 2026-11-15

- **BANANETTAS-MUSIC-001 · MUST ADD — "Le Banana Split" by Lio (1979):** Owner confirmed **Bananettas** as a distinct French-language bubblegum-pop/new-wave gang, with this track as its **first required selection**. Original short French-language single (~2:33) is the intended version; do not substitute the extended disco, English or later remix editions. Sources: [1979 single discography](https://www.encyclopedisque.fr/disque/65862.html) and [Spotify compilation listening reference, track 1](https://open.spotify.com/album/5m0ka2doYDeh54QWdkaxT3). The Spotify reference is not an exact owner-supplied track URL or verified identical master/ISRC. Songwriters Jay Alanski and Jacques Duvall (Hagen Dierks), production Marc Moulin (reconfirm final master credits). **Status:** song and crew concept documented; no acquired/installed recording, rights proof, beat chart, or in-game Bananettas runtime. See the [gang profile](../wiki/Bananettas.md) and [music library](../wiki/Music-Library.md#bananettas). The metadata-only catalogue entry is not a licensed audio asset; current global `rush` playback cannot supply gang affinity without code changes.

## Amygdlya owner-selected gang must-add, 2026-11-15

- **JSC-MUST-AMYGDLYA-20261115-001 · MUST ADD — “La Promesa” by Calva Louise:** [exact owner-supplied Spotify recording](https://open.spotify.com/track/5anCJxgVfQLzlHJ53tXOXm?si=e3335e04d7d84f2c) (`spotify:track:5anCJxgVfQLzlHJ53tXOXm`). **Crew: Amygdlya**. The owner explicitly requires this track for that gang, not merely for the generic soundtrack. Spotify public-page title/artist identification is available. **Current state:** reference and priority recorded; source audio, recording-specific licensing, full credits, performance/beat information, and playable gang wiring are not independently verified. When authorized source audio is available, import the *exact* recording at high quality, preserve master/source provenance and credits, implement Amygdlya-specific selection and validate actual gang playback; the existing `home`/`rush` player contexts do not themselves implement gang affinity. Mirror record: [Music Library — Amygdlya](../wiki/Music-Library.md#amygdlya).

## New Cumbias owner must-add, 2026-11-15

- **JSC-MUST-CUMBIA-20261115-001 — exact Spotify source:** https://open.spotify.com/track/6k3L5dAbeLrNN5TIoLtSze (`spotify:track:6k3L5dAbeLrNN5TIoLtSze`). Explicitly selected by the project owner as a must-add Cumbias recording. The public metadata fetch did not establish exact title/artist/version; status remains **selected, not yet acquired/imported or runtime verified**. Resolve official credits and a permitted audio master through the existing importer, keep source quality independent of Minecraft, and validate actual crew playlist/Boombox playback before marking installed. Future reactive music and Mortal Kumbia charting requires verified beat/offset/stem data rather than guesswork. See the [Music Library](../wiki/Music-Library.md), [Cumbias](../wiki/Cumbias‐and‐Multi‐Rival‐Gang‐War.md), and [portable engine specification](../wiki/Reactive-Music-Engine-and-Killer-Instinct-Reference.md).

### Second owner-selected must-add: Jazztick — Kokiri Forest

- **`JSC-MUST-CUMBIA-20261115-002`:** [Kokiri Forest (From The Legend of Zelda: Ocarina of Time) — Jazztick](https://open.spotify.com/track/6P4ufhmrdt07Vpjm66VpiE); *The Legend of Jazztick: Tetraforce* (2018), track 8, 3:26. Metadata verified from [artist's Bandcamp](https://jazztick.bandcamp.com/album/the-legend-of-jazztick-tetraforce) and official album page. **MUST ADD for Cumbias**, priority placement candidate for [Ocarina of Cumbia](../wiki/Ocarina-of-Cumbia-Easter-Egg.md). Preserve the exact version/artist, full-quality master, credits, and non-Minecraft source asset; do not substitute another Zelda remix. **Current status:** referenced and selected only, not yet imported, played, stem-mapped or native-tested. Use existing music importer safely, then verify real playback and any beat-synced reactive-event mapping without assuming the selected recording is a traditional cumbia track.

## Owner-selected general artist and Cumbias song: 2026-11-15

- **General soundtrack, MUST ADD artist:** **NiNi / Nini Music** ([media kit](https://ninimusic.me/media-kit-1), [TikTok](https://www.tiktok.com/@ninimusic.me), [Spotify](https://open.spotify.com/artist/4iggZUAEXfBeJH9DjKwccm)). Preserve the distinct owner-favorite musician memory and find actual verified songs for global radio/skating/combat; **not a Cumbias assignment**. Stable ID: `JSC-MUST-ARTIST-NINI-20261115-001`.
- **Cumbias gang, MUST ADD exact song:** **“chismosa” by Cain Culto & Jarina De Marco** ([official video](https://www.youtube.com/watch?v=T5rSwbuJuWc), [Apple Music](https://music.apple.com/us/album/chismosa-single/1860155960), [Qobuz](https://www.qobuz.com/us-en/album/chismosa-cain-culto-jarina-de-marco/mrd8dyixvc83z)). 2026 Hive Music, 1:50; official listed artist and writer credits retained. Stable ID: `JSC-MUST-CUMBIA-CHISMOSA-20261115-001`. **Exact owner-confirmed Spotify track:** [chismosa](https://open.spotify.com/track/3Kq6mDB5rLbYhdQcq5Ticg) (`spotify:track:3Kq6mDB5rLbYhdQcq5Ticg`), confirmed as Cain Culto & Jarina De Marco.
- **True implementation state:** reference and spec only; neither selection's audio is provided/imported or verified playable. Use exact masters/credits, maintain a source-quality vault independent of Minecraft streams, measure rhythm data before adding scoring, preserve all existing player controls, settings and other source selections.

## Owner-confirmed Spotify IDs and new Cumbias must-add, 2026-11-15

- **“chismosa” — Cain Culto & Jarina De Marco:** [owner-confirmed exact Spotify recording](https://open.spotify.com/track/3Kq6mDB5rLbYhdQcq5Ticg) (`spotify:track:3Kq6mDB5rLbYhdQcq5Ticg`). Update the existing `JSC-MUST-CUMBIA-CHISMOSA-20261115-001` master/source provenance and future import manifest; **the song remains not installed**.
- **JSC-MUST-CUMBIA-20261115-003 · MUST ADD — “PIZALE” by Susobrino & Xiuhtezcatl:** [exact Spotify recording](https://open.spotify.com/track/7kXUQovYN1rCipqtoBSAhN) (`spotify:track:7kXUQovYN1rCipqtoBSAhN`); [official Susobrino Bandcamp artist discography](https://susobrino.bandcamp.com/album/so); [Susobrino official link hub](https://linktr.ee/susobrino); [Qobuz digital single](https://www.qobuz.com/us-en/interpreter/susobrino/4441356). **Single released August 13, 2026 through Worldwide Records**, approximately 2:36. This is an individually owner-approved **Cumbias must-add**, separate from “chismosa” and the earlier Cumbias Spotify picks. Place in Cumbias Boombox/radio, entrances, dance/freestyle challenges and possible Mortal Kumbia chart only after source listening and exact recording/beat-grid validation. Artist-specific recording credits, full-quality master, beat offsets, stems and in-game playback are **not yet acquired or verified**. Keep indigenous/Latino artistic context grounded in actual creator materials instead of invented genre/cultural generalizations.
- **JSC-MUST-CUMBIA-20261115-004 · MUST ADD — “TUSI” by judith:** [exact owner-selected Spotify recording](https://open.spotify.com/track/4cOJ9DJttuDraD6RRFXDPy) (`spotify:track:4cOJ9DJttuDraD6RRFXDPy`). Verified recording identity: **judith** (lowercase artist presentation), *TUSI* single, **April 22, 2026**, **GOOD BOY RECORDS**, approximately **2:29** ([Apple Music](https://music.apple.com/us/album/tusi-single/1891447109), [Audiomack artist post](https://audiomack.com/judith/song/tusi-1640447)). This is an owner-directed **Cumbias gang MUST ADD** selection even though the source release categorizes it as **Latin pop**, not documented as traditional cumbia. Retain original recording, exact artist attribution and source-version identity; do not substitute another song named TUSI or recategorize its musical origins. [Beatport](https://www.beatport.com/artist/judith/142569/tracks?page=1&per_page=50) lists **160 BPM / C minor** as catalog metadata, **not** a verified in-game beat chart. Candidate uses after listening: Cumbias Boombox/radio, dance challenges and Mortal Kumbia musical encounters. **Status: MUST ADD recorded; high-fidelity master, exact cue/beat grid, separate stems and working game playback not yet acquired or verified.**
- **JSC-MUST-CUMBIA-20261115-005 · MUST ADD #005 — “Veneno - A COLORS SHOW” by La Dame Blanche & COLORS:** [exact owner-selected Spotify recording](https://open.spotify.com/track/3NQNJcX6jLyiZfNWKVcgyH) (`spotify:track:3NQNJcX6jLyiZfNWKVcgyH`), [official COLORS performance](https://www.youtube.com/watch?v=YWaTsluOXNY), [Apple Music single](https://music.apple.com/us/album/veneno-a-colors-show-single/1821692696), [Qobuz 24-bit/48 kHz source reference](https://www.qobuz.com/ca-en/album/veneno-a-colors-show-la-dame-blanche-and-colors/bmjji3q2n6sia). **Release:** July 1, 2021; **label:** COLORSxSTUDIOS; **duration:** 2:39; credited artist La Dame Blanche with COLORS. Qobuz lists Marc Damblé (composer/producer), Yaité Ramos Rodriguez (lyricist), Paul Lorton (mixing/engineering), and COLORS/La Dame Blanche (performers); confirm full master-level credits on acquisition. **Exact-version rule:** this is the **COLORS SHOW** performance/master, not the separately released “Veneno” recording (2:32) from *Atómica* or its other single. **Crew assignment:** owner-approved Cumbias **MUST ADD**, regardless of editorial genre; La Dame Blanche’s Afro-Latin/cumbia/hip-hop/dancehall/reggae approach is a relevant artistic reference without misrepresenting the COLORS recording's exact subgenre. Possible placements: Cumbias Boombox and radio, entrance performance, breakdance/cypher rounds and **Mortal Kumbia** once verified beat maps exist. Preserve highest-fidelity original master, rights/credits provenance, sample-accurate cue timings and portable standalone-game assets; **track is catalogued, not yet acquired/imported or verified playable**.
- **JSC-MUST-CUMBIA-20261115-006 · MUST ADD #006 — owner-selected Cumbias Spotify recording (title and artist verification pending):** [Exact track](https://open.spotify.com/track/1SmuEN3W3rPzM5DgnTteFd) (`spotify:track:1SmuEN3W3rPzM5DgnTteFd`). **Selection:** requested on 2026-11-15 for the **Cumbias** gang as a mandatory, individually selected recording; never substitute a similar song, other mix, or genre guess. **Identity state:** Spotify track ID and link are owner-supplied and exact; title, performing artists, featured performers, release/album, edition, duration, original credits, BPM/key and beat/stem metadata remain **unverified** after public lookup did not return the recording's identifying metadata. Resolve the canonical Spotify track metadata directly or from the artist/label/distributor, validate its availability and exact version, and record official credits and any release changes. **Planned placements:** add to Cumbias Boombox/radio, crew entrances, skating/dance and Mortal Kumbia music battles only after authentic listening and measured beat/phrase analysis. Preserve the original highest-quality master independently of Minecraft export and for a future standalone JetSetCraft successor. **Status:** owner-confirmed MUST ADD queued; no audio downloaded, integrated, beat-mapped, or verified playable. Continue all prior Cumbias selections and global NiNi artist requirement unchanged.
- **Next implementation action:** import only exact sourced/approved masters, preserve original full-quality audio and performer credits, register both Cumbias crew-affinity entries in the player/library, analyze real beat offsets before reactive chart use, then run visible audio playback and multiplayer/state persistence QA. NiNi remains a separate general-game artist must-add.

## Later musical gameplay

The current Soundcheck Circuit is a **crew dispatch assignment**. Future interactive music ideas belong in the design layer until built and tested:

1. A player-built rehearsal circuit whose rails, targets and dance spots become practice stations.
2. A house launch party that records a clean line through the build and turns it into a short Rush objective.
3. Manually authored beat maps for the exact supplied recording, with latency calibration and optional non-rhythm scoring.
4. Gang setlists that change presentation at chapter boundaries without changing the player's selected run build.
5. Cooperative call-and-response graffiti or dance sessions with individual input timing and server-owned score.

Do not derive beat scoring from an album name, guessed BPM or arbitrary playback position. The exact recording and tested synchronization behavior must exist first. The current player is contextual playback, not rhythm-scored combat.
