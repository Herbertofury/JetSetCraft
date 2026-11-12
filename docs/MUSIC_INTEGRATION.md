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

## Later musical gameplay

The current Soundcheck Circuit is a **crew dispatch assignment**. Future interactive music ideas belong in the design layer until built and tested:

1. A player-built rehearsal circuit whose rails, targets and dance spots become practice stations.
2. A house launch party that records a clean line through the build and turns it into a short Rush objective.
3. Manually authored beat maps for the exact supplied recording, with latency calibration and optional non-rhythm scoring.
4. Gang setlists that change presentation at chapter boundaries without changing the player's selected run build.
5. Cooperative call-and-response graffiti or dance sessions with individual input timing and server-owned score.

Do not derive beat scoring from an album name, guessed BPM or arbitrary playback position. The exact recording and tested synchronization behavior must exist first. The current player is contextual playback, not rhythm-scored combat.
