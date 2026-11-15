# JetSetCraft: Portable Reactive Music Engine, Mortal Kumbia & Killer Instinct Acquisition Atlas

> **Owner-directed design and acquisition specification | 2026-11-15 | Not implemented.** Preserve JetSetCraft's existing music player, music library, gangs, mobility, dance systems, gang-war logic, optional-mod compatibility, and prior owner-selected tracks. **The owner reports permission to use Killer Instinct music and assets in JetSetCraft with attribution**; this is a provenance statement from the owner, not independent verification of the particular grant, its scope, source files, or individual media masters. Nothing below is represented as already downloaded, licensed by this document, imported, or runtime-tested.
>
> **Product north star:** JetSetCraft is a love letter to extraordinary global pop culture, art, movement, dance, fashion and music. It starts as an additive Minecraft mod but is intended to evolve into a standalone, renamed, original AAA-caliber IP with cinematic, human-proportioned characters. **Build the complete high-fidelity art/audio/motion source pipeline now**; Minecraft-only derivatives are exports and must never replace original masters.

**Related:** [Music Library](Music-Library) · [The Cumbias](Cumbias‐and‐Multi‐Rival‐Gang‐War) · [Music-Driven Dances & Bosses](Music‐Driven-Dances-and-Mob‐Themed-Bosses) · [Gang War / Boombox](Gang-Wars-Boombox-and-Mob-Atlas) · [Current music integration](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/MUSIC_INTEGRATION.md) · [Standalone compatibility covenant](Standalone-Compatibility-Covenant).

## Objective, context, constraints and completion

**Objective:** Make music an active game system: every skated trick, dance phrase, fight exchange, combo, boss phase, location shift and crowd response can meaningfully change the *arrangement*, not merely trigger an unrelated song. Build a portable reactive engine reusable in Minecraft and the eventual standalone game. Add **Mortal Kumbia** for the Cumbias gang, including multiple music-battle formats. Curate exceptional musical subcultures globally with documented source, full creator credit and distinct gameplay applications. Acquire/organize the full verified baseline of Killer Instinct compositions and all relevant gameplay audio/visual reference asset classes.

**Current baseline:** The repository already contains a Minecraft client resource-pack soundtrack player and audio importer, original per-gang entrance stingers, music references, dancing concepts and a significant multi-rival backlog. It explicitly **does not** yet perform beat analysis or KI-style real-time music transitions. Do not rewrite or mark those areas complete merely because a design exists.

**Constraints:**
- Treat the existing Forge 1.20.1 integration as an **additive adapter**; do not override vanilla/modded audio or ownership, gameplay or entity behavior. Missing tracks, optional integrations, or audio-device failures degrade gracefully.
- All game/audio systems must use a **game-engine-neutral core** for timing, scoring, state, transitions, metadata and authoring; keep Minecraft audio/render/input adapters isolated. No hard dependencies on Minecraft classes in the core.
- Never turn music scoring into mandatory latency punishment for non-rhythm skating/combat. Allow a non-rhythm route without reducing the sound spectacle. Accurate calibration, colorblind/reduced-flash and accessible audio/visual options are first-class.
- Prefer original multitracks/stems, clean WAV/FLAC masters and actual beat maps. **A stereo album track is not a set of adaptive stems.** Never pretend inaccessible source layers exist or mislabel guessed BPM, key, cue points, permissions or audio as verified.
- **Archive full quality:** lossless original masters, raw DAW sessions when supplied, source composition, dry/wet stems, original animation curves, high-poly character/prop rigs and materials, textures, VFX graphs, credits and hashes. Derived Minecraft OGG/low-res/blocky models are versioned runtime exports, never canonical sources.

**Done when:** all 68 baseline soundtrack entries and all targeted asset families have exact acquisition states; the reference/register links to an implementable engine contract; a real fight can change arrangement in time and announce a true earned combo without timing drift; Mortal Kumbia's two core styles and expandable mode system work with calibrated input; high-fidelity asset sources and Minecraft exports remain separately recoverable; live wiki publication and regression/real-runtime evidence are verified. Until implemented, leave tasks unchecked.

## 1. Killer Instinct games/editions to research and source

| ID | Game / edition | Acquire / compare | Why |
|---|---|---|---|
| KI-G01 | **Killer Instinct (1994 arcade)** | Original arcade music/gameplay captures, announcer, rounds, impacts, finishers, stages, sprites and menus | Original iconic hit/mix identity, separated from later remastered album versions |
| KI-G02 | **Killer Instinct (1995 SNES)** | Original SNES version plus **Killer Cuts** CD track identities and port-specific cue/voice/visual differences | Lofi vs master-quality comparison, classic groove, stingers |
| KI-G03 | **Killer Instinct (1995 Game Boy)** | Portable arrangement/cue variants and concise audio-feedback patterns | Document platform-specific variants; do not mistake them for CD tracks |
| KI-G04 | **Killer Instinct 2 (1996 arcade)** | Stage arrangements, gameplay calls, combos, boss cues, transitions, arcade visuals and audio | Sequel-specific musical language and character patterns |
| KI-G05 | **Killer Instinct Gold (1996 Nintendo 64)** | N64 variants, Gold Cuts album and bonus/early demo/alternate releases | Complete KI2/Gold set and rare versions |
| KI-G06 | **Killer Instinct (2013), Seasons 1–3 + post-Season-3 characters** | All 2013 music, fight states, adaptive arrangement, combo/Ultra/instinct calls, interactive stages, character palettes, match sounds | **Highest-priority technical reference** for the full reactive combat loop |
| KI-G07 | **Killer Instinct: Anniversary Edition (2023) on PC/Xbox** | Current complete content, 29-fighter roster, presentation/quality references and surviving game audio access | Most convenient contemporary playable reference; not a claim source DAW/rigs are included |
| KI-G08 | **Killer Instinct: The Complete Soundtrack (2017 Steam release)** | Exactly 68 official digital soundtrack tracks and published credits, with matching edition IDs | **Primary verified acquisition manifest**; album mixes are not in-game adaptive stems |

**Official entrypoints:** [Complete 68-track soundtrack on Steam](https://store.steampowered.com/app/720530/Killer_Instinct__The_Complete_Soundtrack/) · [Anniversary Edition on Steam](https://store.steampowered.com/app/2625580/Killer_Instinct_Anniversary_Edition/) · [Anniversary Edition on Xbox](https://www.xbox.com/en-us/games/store/killer-instinct-anniversary-edition/9N04NS8TP202) · [Historic Killer Cuts](https://www.guinnessworldrecords.com/world-records/106013-first-videogame-soundtrack-cd) · [KI2 Gold Cuts reference](https://killerinstinct.gamebub.com/sounds.php) · [Rare's classic/port-specific soundtrack register](https://killerinstinct.fandom.com/wiki/Killer_Instinct%3A_The_Complete_Soundtrack).

### Complete official 68-track base catalog

**Source:** Steam's official 2017 *Killer Instinct: The Complete Soundtrack*, whose listing enumerates exactly **68 tracks**. Original capitalization, corrections/variants and contributor credits should follow the final acquired file metadata; the source page itself contains minor typographical inconsistencies (for example **Shatterhall** vs **Shatterhail**, and the truncated bracket in **The Way U Move [SeamlessR Remix**). Use stable **KI-OST-001–068** IDs, never title alone, to prevent collisions between two versions of *The Instinct* and similar names. All 68 begin as `verified listing / audio not supplied to this session / in-game use not implemented`.

#### Season 1: KI-OST-001–011, Mick Gordon and collaborators

| ID | Official listing title | Use to study / proposed JetSetCraft application |
|---|---|---|
| 001 | Touch me and I'll break your face | Explosive combat identity; matchup/intro and successful-combo escalation |
| 002 | The Instinct | Signature leitmotif, comeback and ultimate climax |
| 003 | Hinnamatoom | Character-specific rhythm and strong percussion identity |
| 004 | Warlord | Dramatic character motif, late-combat menace |
| 005 | The Tiger Warrior | Instrumental identity and dynamic fight pacing |
| 006 | Type-03 | Mechanical combat textures and rhythmic effects |
| 007 | Shatterhall (also catalogued as Shatterhail) | Contrasting texture and hit-response transitions |
| 008 | Ballet of death | Rhythm/animation contrast and theatrical escalation |
| 009 | Lycanthropy | Long-form tension and phase evolution |
| 010 | The End | End-state release, results and victory timing |
| 011 | The Way U Move [SeamlessR Remix] | Dance-driven battle, old/new motif crossover |

#### Season 2: KI-OST-012–020, Mick Gordon and collaborators

| ID | Official listing title | Use to study / proposed JetSetCraft application |
|---|---|---|
| 012 | I'm Back (to Rise!) | Combo payoff, lyrics, character comeback |
| 013 | Village of Whispers | Atmospheric narrative and dynamic tension |
| 014 | Inferno | Combative build, sparking impact accents |
| 015 | Temperance & Vengeance | Dramatic melodic contrast and counterattack |
| 016 | Polemos | Rhythmic fight pacing and heavy accents |
| 017 | Sandstorm | Percussive combat layers and environmental shifts |
| 018 | .execute | Electronic combat intensity and systems feedback |
| 019 | Hatchery 09 | Aggressive syncopation and chase states |
| 020 | Herald of Gargos | Boss revelation and transformation |

#### Season 3 and later 2013-game additions: KI-OST-021–032, Celldweller / Atlas Plug (Klayton and Tom Salta) and collaborators

| ID | Official listing title | Use to study / proposed JetSetCraft application |
|---|---|---|
| 021 | A Warrior's Journey | Character introduction and action anticipation |
| 022 | The Dragon Spirit | Melodic fight identity and rapid counterplay |
| 023 | For the Horde! | Gang-vs-gang momentum and ensemble chants |
| 024 | Enter the Battletoad | Crossover personality and playful attack accents |
| 025 | Kneel to Your God | Boss intimidation and adaptive phase shifts |
| 026 | Nocturne in Blood | Dark theatrical fighting |
| 027 | The Champion of Sanghelios | Epic scale, layered orchestration |
| 028 | The Watchman of the Gods | Mystery and character-specific rhythm |
| 029 | A Shattered Eyedol | Final boss catastrophe, breakdown and return |
| 030 | An Eagle's Call | Emotional story climax and returning themes |
| 031 | CCIX | Alternate fight palette and intense groove |
| 032 | Shin Hisako | Late-era character theme and contrast |

#### 1994 game/Killer Cuts release set: KI-OST-033–048, Robin Beanland / Graeme Norgate and collaborators

| ID | Official listing title | ID | Official listing title |
|---|---|---|---|
| 033 | K.I. Feeling | 034 | The Way U Move |
| 035 | Controlling Transmission | 036 | Oh Yeah |
| 037 | It's A Jungle | 038 | Do It Now! |
| 039 | Full Bore | 040 | The Instinct |
| 041 | Yo Check This Out! | 042 | Freeze |
| 043 | Trailblazer | 044 | Tooth & Claw |
| 045 | Ya Ha Haa | 046 | Rumble |
| 047 | The Extreme | 048 | Humiliation |

**Classic variants beyond the 68 baseline:** the collector's *Killer Instinct 1 & 2: The Complete Soundtrack* catalogs arcade, SNES, Game Boy and additional vintage/unused/remixed cue versions separately. Inventory those variants only after checking exact edition/recording identity; do not silently collapse them with the 2017 album tracks.

#### Killer Instinct 2 / Gold and early/alternate recordings: KI-OST-049–068

| ID | Official listing title | ID | Official listing title |
|---|---|---|---|
| 049 | KI2Gold - Killer Instinct Title Theme | 050 | KI2Gold - Character Select |
| 051 | KI2Gold - Maya - Game Version | 052 | KI2Gold - Maya - Remix |
| 053 | KI2Gold - Sabrewulf | 054 | KI2Gold - Jago - Game Version |
| 055 | KI2Gold - Jago - Shakuhachi Remix | 056 | KI2Gold - Combo |
| 057 | KI2Gold - Tusk | 058 | KI2Gold - Kim Wu |
| 059 | KI2Gold - Orchid | 060 | KI2Gold - Glacius |
| 061 | KI2Gold - Fulgore | 062 | KI2Gold - Spinal |
| 063 | KI2Gold - Gargos | 064 | KI2Gold - Training Mode |
| 065 | KI2Gold - Character Select - Alternate Version | 066 | KI2Gold - Combo - Early Demo Track |
| 067 | KI2Gold - Maya - Early Demo Track | 068 | KI2Gold - Killer Instinct Title Theme - Alternate Version |

**Coverage assertion:** IDs 001 through 068, inclusive = 11 Season 1 + 9 Season 2 + 12 Season 3 / postlaunch + 16 KI1 + 20 KI2/Gold = **68 distinct album positions**. The IDs denote acquisition targets; they are not proof any audio file is currently in the repository. Preserve exact composer, arranger, featured performer, instrumentalist, label, source edition and recording master separately, using the official Steam credits and [MusicBrainz recording relationships](https://musicbrainz.org/release/d542ef03-9b19-454c-af1f-9861171501d7).

## 2. What Killer Instinct **assets** to acquire (not contained in an album)

| Priority | Asset family | Exactly what to capture / request | Needed for JetSetCraft |
|---|---|---|---|
| P0 | **Adaptive music sessions / stems** | Per-fighter music sections, stems/buses, safe bar boundaries, intro/idle/advantage/pressure/critical/Ultra/finish segments, transition one-shots, tempo maps and cue metadata, preferably native DAW + lossless stems | Same musical continuity and state-responsive editing as KI, not crude random song swapping |
| P0 | **Announcer voice bank** | Fight-start, ready, KO, win/loss, lockout, combo-breaker, counter-breaker, instinct, Ultra, combo size/rank/intensity, comeback, round-end; original language/actor/line IDs/clean dry takes when provided | Context-sensitive and earned hype; game-mode-specific narrator controls |
| P0 | **Combat SFX/impact layers** | Hit confirms by material/heaviness, counter, block/guard, parry, armor break, knockdown, clash, throw, whiff, finish, sparks, sub-bass impacts, whooshes, Foley, spatial room and dynamic range curves | KI-strength feedback without muddying the music or masking game cues |
| P0 | **Mix and implementation data** | Wwise session/project if the permission package includes it; bus topology, sidechains, RTPC/state mappings, duck envelopes, attenuation, surround/spatial metadata, mix presets, reference loudness | Reproduce the *behavior* with JetSetCraft's portable system; not a requirement to ship Wwise |
| P1 | **Music-driven animations** | Character intro, taunt, victory, defeat, Ultra, combo-break, recovery, synced camera and keyframe/tempo links | Stage choreography, dance response, fighter personality |
| P1 | **Presentation VFX** | Hit sparks, trails, bursts, screen effects, shadow, stage lighting, character-specific palettes, audio reactive shaders/particles | Make each combo feel huge without destroying readability or reduced-flash support |
| P1 | **UI / typography / symbols** | Combo counter and rank timing, state banners, training timing graphs, fight start, end cards, character-select meter and beat HUD | Clear dynamic feedback; learn from KI, build JetSetCraft original branded UI unless authorized exact assets are deliberately selected |
| P1 | **Character and stage master assets** | Where supplied: high-poly meshes, rigged humanoids, original anim curves, facial blendshapes, materials, source PBR textures, props, arenas, cameras | Archive cinematic master assets *separate* from any Minecraft remesh/LOD export |
| P1 | **Fighter/state reference captures** | Verified game build, game patch/platform, 60-fps+ capture, clean audio channels if available; examples for low/mid/high combo, break, Ultra, loss | Build reproducible event-to-audio behavior fixtures and audio comparison tests |
| P2 | **Classic arcade/SNES/GB/N64 variants** | Distinct individual ROM/hardware arrangement identities, compressed voices, classic UI sounds, pixel-era hit style and bonus/demo cues | Optional nostalgia references without misleading 'full quality' claims |
| P2 | **Artwork and branding** | Official cover art, promotional renders, stage art, character turnarounds, palette documentation, logos/credits where supplied | Provenance-backed study library, not a substitute for the future original standalone-IP art bible |

**Acquisition order:** (1) complete official 68-track album with original per-track provenance; (2) modern KI Anniversary Edition for hands-on state/mix research; (3) the owner-authorized material package for any **true stems, VO banks, project files, rigs and VFX**; (4) classic KI1 and KI2/Gold edition-specific variants and unused tracks; (5) extras, alternate mixes and live implementation captures. For every item, record `asset_id`, `KI edition`, `exact source URL/package`, `creator/performer credits`, `permission reference`, `format/channels/sample rate`, `SHA256`, `raw master present`, `Minecraft export present`, `standalone export present`, `verified state`, `next action`. Never invent a direct download for files that have not been located.

## 3. Research: what made Killer Instinct combat feel so responsive

**Authoritative technical reference:** Zack Quarles' [KI accessibility-through-audio postmortem](https://www.gamedeveloper.com/audio/postmortem-greater-accessibility-through-audio-in-i-killer-instinct-i-) and [GDC 2014: Next-Gen Audio in Killer Instinct](https://gdcvault.com/play/1021196/Next-Gen-Audio-in-Killer). Quarles describes (a) separating giving/receiving/blocking damage audibly, (b) substantial sidechain/bus ducking so critical impact and announcer content punch through powerful music, (c) match-driven music suites with **4–8 bar loops** transitioning on measure/downbeat boundaries, and (d) aggressively legible position and combat feedback for players with low/no vision. Reuse these verified **principles**, not speculative claims about exact unreleased stem counts.

**JetSetCraft adaptation:**
- **Kinetic event source:** `trick_started`, `grind_land`, `flow_chain_extended`, `perfect_dance_step`, `attack_hit`, `damage_received`, `block`, `parry`, `counter`, `combo_extended`, `combo_breaker`, `near_KO`, `boss_phase`, `crowd_hype`, `victory`.
- **Musical action:** select a compatible section and layer/motif; quantize nonurgent transitions to a beat or bar, inject immediate hit/voice transients on confirmed gameplay event time, transition between sections without click/pop or phase drift. Do not make player input await a downbeat.
- **Critical contrast:** dynamically sidechain/duck background music and crowd for impact, combo call, and gameplay warnings; protect speech legibility and headroom, avoid full-scale static loudness that clips on stacked events.
- **Meaningful hype:** earned combo names, synchronized crowd reactions, escalating per-crew music layers, final-round break and properly timed last-hit/Ultra stinger. Never announce a combo that gameplay rejects or duplicate a one-shot on rollback/network replay.
- **Non-combat reuse:** skate combo builds bass/percussion, transition/grind adds musical fill, graffiti wall completes a melodic response, a district owns an identifiable musical instrument palette, crews trade motifs, dance becomes audible call-and-response.

## 4. Portable Reactive Music Engine architecture

### 4.1 Engine ownership

`GameEventAdapters (Forge / standalone engine / tests)` → `Timestamped Gameplay Event Bus` → `Transport + Beat Grid` → `Music Director (finite/hierarchical state + intensity)` → `Score/Combo Director` → `Arrangement & Quantized Scheduler` → `Mixer/Sidechain & Voice/Impact Bus` → `Audio Backend Adapters (Minecraft / native, optionally Wwise/FMOD)` → `Audio/visual feedback + persistent provenance`.

**Canonical contracts** (language/platform neutral; serialized declaratively, not duplicated in each adapter):

```yaml
schema: 1
track_id: KI-OST-002
recording_status: catalogued_not_imported
source_edition: KI_2013_complete_soundtrack
music:
  bpm: null               # verify from actual master, never guess
  first_downbeat_samples: null
  meter: null
  beat_grid: null
  loop_regions: []        # only if actual decoded song/editor verification exists
  stems: []               # stereo album mix does not imply separable stems
  adaptive_states: []     # authored from properly sourced segments
triggers:
  - event: combo_extended
    minimum_combo: 5
    desired_state: advantage
    quantize: next_beat
    impact_sfx: independent_confirmed_hit
integrity:
  source_sha256: null
  provenance_manifest_id: KI-OST-002
```

- **Clock:** audio callback/sample-frame clock is authoritative for audio; synthesize beat/bar positions from verified tempo maps; allow tempo/time-signature changes, count-in, seek, pause, restart, sample-rate device change and looping. Game ticks are observers, **not** a sample-accurate music scheduler.
- **Score:** server-authoritative actual hit/trick/dance event ownership; client predicts only explicitly reversible feedback; dedupe by event ID; reconcile jitter and rollback; no fake scoring; separate audio latency calibration from input timing windows.
- **Arrangement:** weighted style/state graphs, safe exit points, beat-accurate transitions, musical key/mode compatibility, stem phase alignment and crossfade/mix envelopes; smooth fallback to stereo-only full-track mixing with one-shots if native stems are unavailable. Do **not** present stereo fallback as equivalent to KI's section-level reactivity.
- **Playback adapters:** preserve the existing resource-pack player and `/jetsetmusic` preferences; integrate by one owner-of-playback routing/mute coexistence, not two overlapping music tracks. Native standalone adapter targets full source stems and high-dynamic-range pipelines. Forge dedicated servers never require an audio device.
- **Authoring tools:** waveform, editable/manual beat grid, loop/region editor, beat-map/notes editor, state graph preview, stem alignment inspector, loudness/ducking monitor, playback provenance and clear 'audio missing' states. Automatic onset/tempo analysis is a **suggestion**, followed by audition/manual verification.
- **Settings:** accessibility/reduced flash, music/voice/impact sliders, announcer frequency, dynamic-range presets, audio timing calibration per device, input remaps, easy/casual/precision modes, streaming-safe replacement audio, user-selected radio, persistent preferences.

### 4.2 Deterministic demonstration cases

1. Ride seamlessly into a gang confrontation: street groove stays in key while stems change; player control and hits stay immediate.
2. Three real confirmed hits create rising percussion/harmony; a legitimate combo break returns to tension and calls the correct event once.
3. Major boss near defeat creates a controlled, quantized final escalation; actual last hit triggers immediate impact and an on-beat victory cadence.
4. Two players in multiplayer hear a consistently timed gang event while scoring remains authoritative; late packet and rollback do not cause duplicate announcers.
5. Stereo-only track (including any KI official album mix without stems) uses honest fallback. Missing track never crashes Forge, never silences another mod, and never blocks game controls.

## 5. The Cumbias: **Mortal Kumbia** music-battle festival

**Identity:** Mexican cumbia and broader regional cumbia histories approached through real artists, styles, choreography, fashion, instrumentation, DJs, dancers, stage craft, live keyboard virtuosity and charismatic rival characters. Treat the Cumbias as an authentic cultural crew with individuality, not a parody faction or a Gabberina reskin. Integrate battles with existing Boombox, gang territory, rival challenges, dance system, crew identity and chapter progression.

| Mode | Player experience | Scoring/music reaction |
|---|---|---|
| **MK-01: Kumbia Highway** | Guitar Hero-style scrolling note highway, optional multi-lane controller/keyboard patterns, solos, sustain notes and live band/keyboard duel | Each completed phrase adds/removes instrument layers, crowd callouts, rival response and solo spotlight |
| **MK-02: Dance Arrows** | Dance Dance Revolution / rhythm-arrow style up/down/left/right patterns, on-foot or skated footwork, duo/co-op/cypher formations | Calibrated early/perfect/late feedback, musician-led patterns and dance animation aligned to actual beats |
| **MK-03: Call & Response** | Listen to a rival's short musical/dance motif, then answer via movement/tricks, phrase memory or expressive selected steps | Melodic/crowd response composed from legitimate performance input |
| **MK-04: Freestyle & Trick Remix** | Branching choreography and skating combos with no forced note-chart accuracy; combine rolls, grinds, graffiti, hops and flourishes | Stylish actions remix groove in real time, earned accents, never sterile score chasing |
| **MK-05: Crew Clash / Boss Encore** | Multiple Cumbias, allies and rival-gang crews trade rounds; team choreography and arena presentation escalate | Music leader, bass, percussive counterpoints and arena visuals follow who is winning without stealing agency |

**Acceptance:** local/online, all current supported input styles, timing-calibration UI, multiple difficulty curves, optional no-fail/casual route, genuine high-difficulty expression, replayable beat maps, useful controller remapping, clear chart attribution, stage readability and no visual flashes forced on sensitive users. Use **original/authorized playable chart versions**, not an album track without a verified grid. Preserve precise player agency between choreography and gameplay.

### Rare Cumbias Easter egg: **Ocarina of Cumbia** (owner-requested)

**JSC-EGG-CUMBIA-OCARINA-001** is an ultra-rare music-driven **Cumbias × Zelda** crossover surprise. When a normal eligible Cumbias performance/event occurs, a second server-seeded rare roll (proposed **1% of eligible Cumbias events**, configurable/tunable) can transform it into an ocarina-led cumbia showcase, with recognizable **Link, Zelda and other Zelda-character cameos** dancing with the Cumbias. Full design: [Ocarina of Cumbia — Rare Music Encounter](Ocarina-of-Cumbia-Easter-Egg). **Owner video:** https://www.tiktok.com/t/ZPLK2Fnk7/ . The exact creator, soundtrack, characters actually visible in the clip, master and audio metadata are **unverified**, not invented. The requested Zelda cast is a release goal, not a claim that crossover rights/assets are already available.

The surprise must have immediate ocarina foreshadowing, genuine cumbia rhythm, theatrical appearance, playable or watchable dance/skate battle, varied personality-driven choreography, musical accent responses to combos, and satisfying phrase-quantized finale. Players may opt in or keep playing normally. Preserve existing rare-encounter eligibility, mod compatibility, normal mob identity, world saves, Cumbias rules and music settings; never broaden global spawns or create a per-tick scan. Record the discovery and enable an optional replay without making the wild surprise commonplace. High-fidelity character, music and animation masters remain canonical for the future standalone game.

## 6. Global culture/music discovery program

JetSetCraft is not a genre monoculture. Curate music that is *genuinely extraordinary* in its local context with informed criticism and community participation, not a shuffled stereotype playlist. The existing 160-candidate research shelf and owner selections remain preserved. Expand deliberately across **cumbia (Mexican, Colombian and regional variants), champeta, baile funk, amapiano, gqom, footwork/juke, jersey club, UK garage/jungle, highlife, soukous, coupé-décalé, raï, dabke, qawwali, gamelan, dangdut, mor lam, taiko, Japanese jazz/funk, Gnawa, Balkan brass, bhangra and regionally specific dance scenes**. This is a research map, **not** a claim each named tradition already has vetted game-ready tracks.

For every music discovery candidate: official playable reference → exact recording ID and artist/collaborator credit → local scene/context and precise subgenre → outstanding distinguishing characteristic → verified performance/video reference → gameplay opportunity (district, ride, dance, fight, crew, calm moment, finale) → quality/editorial score and human listening evidence → release/master access and owner-reported rights state → available beat maps/stems → acquisition/integration status. Prefer musician/scene-led partnerships and cross-scene collaborations to token playlists. Build optional radio channels/stations and real places for the music to live. Preserve artist identity and respectful voice, including accurate accent/language and choreography.

### Second owner must-add: Jazztick — Kokiri Forest (Ocarina of Time), JSC-MUST-CUMBIA-20261115-002

- **Exact recording:** [Spotify](https://open.spotify.com/track/6P4ufhmrdt07Vpjm66VpiE); `spotify:track:6P4ufhmrdt07Vpjm66VpiE`. **Verified metadata:** Jazztick; *Kokiri Forest (From The Legend of Zelda: Ocarina of Time)*; *The Legend of Jazztick: Tetraforce* (2018), eighth track, 3:26. [Artist’s official Bandcamp album](https://jazztick.bandcamp.com/album/the-legend-of-jazztick-tetraforce) and [artist discography](https://jazztick.net/producto/the-legend-of-jazztick-tetraforce-2018/) provide authoritative acquisition and credit research sources.
- **User direction:** **MUST ADD to the Cumbias**, never replace with a generic Kokiri Forest, another cover, another Jazztick performance, or a synthesized imitation. Prioritize use with [Ocarina of Cumbia](Ocarina-of-Cumbia-Easter-Egg), after musical, gameplay, and source/credit verification; also enable Cumbias Boombox/radio/crew events when appropriate. Exact use in the TikTok reference is **unverified**. The identified recording is a jazz/fusion Zelda arrangement; do not claim it is itself a cumbia recording without evidence.
- **Adaptive implementation requirement:** obtain an appropriate high-quality recording/master and real stems when available; measure musical downbeats, BPM and phrasing (never guess); author beat-sync/cue entry, transitions and reactive crowd/ocarina/gameplay layers without altering the source recording identity, or use truthful full-song playback when stems are unavailable. Keep stems, masters, metadata and credits portable across Minecraft and the future AAA game. **Status:** identified and explicitly selected; master, stems, beat map and playable import all pending.

### Priority owner-selected Cumbias recording: JSC-MUST-CUMBIA-20261115-001 (2026-11-15)

**Must add, exact reference:** https://open.spotify.com/track/6k3L5dAbeLrNN5TIoLtSze (`spotify:track:6k3L5dAbeLrNN5TIoLtSze`). The owner selected this recording specifically for the Cumbias on 2026-11-15. This selection is **required soundtrack scope**, independent of the earlier TikTok and Killer Instinct selections and not an optional research suggestion. Spotify's public track metadata was not reliably accessible in this session: **do not guess title, artist, release, remix, instrumentation, BPM, key or stems; preserve the ID and exact recording until verified**. Acquisition status is **reference saved; source master and in-game playback pending**.

**Required workflow:** verify source metadata against Spotify/artist/label → acquire the exact permitted/high-quality master and any real stems → record credits and original release/edition → listen and assess fit for Cumbias crews/Boombox/territory/radio/Mortal Kumbia → author real beat map and adaptive cue/state or use truthful non-rhythm playback until mapped → prove the exact recording plays in a real mod session → preserve the original lossless source and generate Minecraft + standalone-native derivatives independently. Do not falsely attribute this recording to the Ocarina of Cumbia or any TikTok creator merely because it was requested for the same gang.

### Distinct owner selections: NiNi for general music, “chismosa” for Cumbias (2026-11-15)

- **JSC-MUST-ARTIST-NINI-20261115-001 · owner-selected general soundtrack MUST ADD artist:** [NiNi / Nini Music](https://ninimusic.me/media-kit-1) ([official TikTok](https://www.tiktok.com/@ninimusic.me); [Spotify artist catalogue](https://open.spotify.com/artist/4iggZUAEXfBeJH9DjKwccm)). Her Taiwanese ruan, liuqin, sanxian and DaoYu electric-lute fusion with rock, metal and electronic styles is a strong culturally specific discovery for the **entire** game, not a Cumbias crew theme. Artist was explicitly named a personal favorite in the ProjectDump [Favorite Music Artists register](https://github.com/Herbertofury/ProjectDump/blob/main/wikis/Favorite-Music-Artists.md). Work on actual track-specific audition/credits, original quality masters, and general-world music routes without inventing owner approval for every song in the catalog.
- **JSC-MUST-CUMBIA-CHISMOSA-20261115-001 · owner-selected Cumbias MUST ADD song:** [“chismosa” by Cain Culto & Jarina De Marco](https://www.youtube.com/watch?v=T5rSwbuJuWc), [Apple Music release](https://music.apple.com/us/album/chismosa-single/1860155960), [Qobuz high-resolution master reference](https://www.qobuz.com/us-en/album/chismosa-cain-culto-jarina-de-marco/mrd8dyixvc83z). 2026 Hive Music single, ~1:50; credits include Cain Culto, Jarina De Marco, producer Alex Krispin and lyricist Andrew Padilla. Marketplace [Beatport](https://www.beatport.com/track/chismosa/30827475) lists 135 BPM in G major as **provisional**, not a measured beat grid. Intended Cumbias radio, Boombox and Mortal Kumbia charts after authentic source/beat mapping. This song is **not** an NiNi recording, and cannot be silently replaced by a similar song or other mix. **Exact owner-confirmed Spotify track:** [chismosa](https://open.spotify.com/track/3Kq6mDB5rLbYhdQcq5Ticg) (`spotify:track:3Kq6mDB5rLbYhdQcq5Ticg`), confirmed as Cain Culto & Jarina De Marco.
- **Portability and verification:** preserve exact performance/track metadata, attribution and full-quality source audio; independently export Minecraft formats and future standalone-native sources. Map sound to interactive skating/dancing/fighting states only after actual acquisition and beat/phrase analysis. Keep both selections as **not yet installed**, and preserve previous Spotify Cumbias must-adds, KI, Ocarina and TikTok queues.

### Owner-selected Cumbias additions: verified Spotify recordings (2026-11-15)

- **Existing chismosa entry updated:** `JSC-MUST-CUMBIA-CHISMOSA-20261115-001` → [exact Spotify version](https://open.spotify.com/track/3Kq6mDB5rLbYhdQcq5Ticg), Cain Culto & Jarina De Marco. This ID resolves the prior open catalog field. Retain original source metadata/creator credits, no runtime-master integration implied.
- **JSC-MUST-CUMBIA-20261115-003 · MUST ADD — “PIZALE” by Susobrino & Xiuhtezcatl:** [exact Spotify recording](https://open.spotify.com/track/7kXUQovYN1rCipqtoBSAhN) (`spotify:track:7kXUQovYN1rCipqtoBSAhN`); [official Susobrino Bandcamp artist discography](https://susobrino.bandcamp.com/album/so); [Susobrino official link hub](https://linktr.ee/susobrino); [Qobuz digital single](https://www.qobuz.com/us-en/interpreter/susobrino/4441356). **Single released August 13, 2026 through Worldwide Records**, approximately 2:36. This is an individually owner-approved **Cumbias must-add**, separate from “chismosa” and the earlier Cumbias Spotify picks. Place in Cumbias Boombox/radio, entrances, dance/freestyle challenges and possible Mortal Kumbia chart only after source listening and exact recording/beat-grid validation. Artist-specific recording credits, full-quality master, beat offsets, stems and in-game playback are **not yet acquired or verified**. Keep indigenous/Latino artistic context grounded in actual creator materials instead of invented genre/cultural generalizations.
- **JSC-MUST-CUMBIA-20261115-004 · MUST ADD — “TUSI” by judith:** [exact owner-selected Spotify recording](https://open.spotify.com/track/4cOJ9DJttuDraD6RRFXDPy) (`spotify:track:4cOJ9DJttuDraD6RRFXDPy`). Verified recording identity: **judith** (lowercase artist presentation), *TUSI* single, **April 22, 2026**, **GOOD BOY RECORDS**, approximately **2:29** ([Apple Music](https://music.apple.com/us/album/tusi-single/1891447109), [Audiomack artist post](https://audiomack.com/judith/song/tusi-1640447)). This is an owner-directed **Cumbias gang MUST ADD** selection even though the source release categorizes it as **Latin pop**, not documented as traditional cumbia. Retain original recording, exact artist attribution and source-version identity; do not substitute another song named TUSI or recategorize its musical origins. [Beatport](https://www.beatport.com/artist/judith/142569/tracks?page=1&per_page=50) lists **160 BPM / C minor** as catalog metadata, **not** a verified in-game beat chart. Candidate uses after listening: Cumbias Boombox/radio, dance challenges and Mortal Kumbia musical encounters. **Status: MUST ADD recorded; high-fidelity master, exact cue/beat grid, separate stems and working game playback not yet acquired or verified.**
- **Selection coverage:** This is a newly approved *single track*, not authorization to classify all Susobrino or Xiuhtezcatl releases as must-add; the older 001/002 Cumbias items, both TikTok references, the separate NiNi general artist selection and the rare Ocarina Easter egg remain unchanged.
- **Action:** source the exact recordings, credit all actual creators, retain pristine source masters and verified hashes, establish real source-derived beat/bar/phrase maps, author optional reactive grooves and dance/scoring cues via the existing portable music engine, and verify actual Cumbias playback in Minecraft before promoting either to implemented status.

### New owner-submitted TikTok track awaiting identity

- **ID:** `JSC-MUST-TIKTOK-20261115-001`.
- **Submitted reference:** https://www.tiktok.com/t/ZPLKY1fPj/ . Owner asked to identify and add the track to JetSetCraft on November 15, 2026.
- **Status:** **must-identify / selection requested; artist, title, original sound/video ID, recording edition, BPM, stems and audio master all unverified**. A TikTok short-link redirect/media read did not resolve in this research environment. **Do not guess a track name, substitute a generic cumbia or claim audio imported.** Next actionable step: inspect the source video's sound label through an authorized logged-in view or owner-provided original video URL/screenshot/audio clip; match exact song+version against official artist/release metadata, then append to the owner-selected Music Library with its validated credit and acquisition path. Preserve this exact link and ID until resolved.

### Additional creator-first TikTok research goals (owner-selected)

- **JSC-CREATOR-TIKTOK-20261115-002:** https://www.tiktok.com/t/ZPLK2mF7q/ . Identify video uploader, musician/composer/producer, exact featured audio and official release; audit the creator's **broader music catalogue** as a high-priority artist-inclusion goal, not simply one track.
- **JSC-CREATOR-TIKTOK-20261115-003:** https://www.tiktok.com/t/ZPLK2Fnk7/ . Same creator/music-catalogue research priority, **plus** the canonical owner-selected **Ocarina of Cumbia** Zelda crossover Easter egg. Do not assume the two posts share one creator or sound until verified.

Both short links were inaccessible to source-media inspection. Do **not** guess artist names, title, edition, Zelda scenes, lyrics, key, BPM or permission. Identify the uploader separately from underlying audio composer, arranger, cover artist and master owner; once resolved, add standout creator works to the Music Library with documented credit, style/context, audition notes, master/stem acquisition and appropriate roles in the portable reactive engine. Preserve all previous owner-selected entries independently.

## 7. Native source quality, attribution and asset preservation

- Full-fidelity master per asset with stable immutable ID and SHA-256; source DAW session, individual stems and instrument group buses, 24-bit/48 kHz or original higher-resolution where actually provided, original source textures/materials/meshes/animation rigs where supplied. Do not upsample and call it a master.
- Derivative export manifests per target: `minecraft_forge_1_20_1`, future native standalone build, archive/reference. Document source hash, codec, channel count, sample rate, trim, loop metadata, toolchain/version, final hash and exact credits. Link derivatives back to the master; never reverse-derive a master from lossy Minecraft audio.
- Per-track attribution in-game now-playing and credits, featuring artists, composers, performers, labels as appropriate, associated release/edition, and any text specified by the actual permission. Preserve owner-reported **Killer Instinct permission-with-credit** as a provenance fact pending matching individual assets/grant terms; this document is not a substitute for exact scope matching. The final standalone original IP needs a coherent license/provenance registry so future migration does not accidentally convert licensed reference assets into allegedly original designs.
- Separate `requested`, `source located`, `metadata verified`, `permission owner reported`, `raw master acquired`, `playable export generated`, `beat chart verified`, `adaptive layers verified`, `runtime verified`. Never collapse states or claim playable because a source URL exists.
- Asset tiers: highest possible quality master; high-quality native runtime rendition; game-appropriate Minecraft export; optional accessibility/lightweight alternatives **without capping the canonical source**. Human-scale characters and original distinctive creative direction are the future baseline.

## 8. Resumable execution backlog (all unchecked: specification, not feature-completion claim)

- [ ] **T001** · Acquire the official complete soundtrack baseline and reconcile all KI-OST-001–068 file hashes, title/edition/credit metadata against the verified official listing; retain duplicate-name variant IDs.
- [ ] **T002** · Locate and inspect the actual owner's Killer Instinct permission/source bundle; catalog covered recordings/VO/music/characters/VFX/masters, missing scopes and exact attribution obligations. Do not treat game install as equivalent to editable source assets.
- [ ] **T003 [P]** · Acquire and index exact KI original arcade/SNES/Game Boy/KI2/Gold variant, bonus, unused, demo and hardware-specific versions, preventing duplicates with the 68-track album baseline.
- [ ] **T004 [P]** · Collect clean KI fight reference events and available sound/VFX/announcer/animation/mix assets by title, patch, scene and source. Identify source-provided adaptive stems vs album stereo mixes.
- [ ] **T005** · Design stable `music_event`, `cue`, `beat_grid`, `stem`, `state`, `credit`, `acquisition` schema and versioned portable core boundary; prove Minecraft classes never leak into the core API.
- [ ] **T006** · Implement verified sample-accurate transport, state graph, quantized scheduler, music bus mixer, sidechain ducking, prompt immediate impacts, duplicate-suppressed announcements and real game state triggers.
- [ ] **T007** · Wire a single real, reversible combat-combo/audio vertical slice into the existing JetSetCraft Forge music player; preserve `/jetsetmusic`, optional-mod compatibility, server safety and normal worlds.
- [ ] **T008** · Implement expressive trick/grind/dance/graffiti/crowd music events and expose per-crew arrangement hooks with no mandatory-rhythm punishment.
- [ ] **T009** · Build **Mortal Kumbia** Guitar Hero-style highway and **Dance Arrows** modes with authorable chart, calibrated controls, real music stems or truthful fallback, scoring and stage feedback.
- [ ] **T010** · Extend Mortal Kumbia with call-response, freestyle/remix, crew clash and boss encores; preserve real performer/culture research and unique Cumbias faction identity.
- [ ] **T011 [P]** · Expand global music/artist discovery with source-linked verified listening and creator-aware cultural research; preserve the entire existing owner-approved music catalogue.
- [ ] **T012** · Resolve the exact submitted TikTok audio at `JSC-MUST-TIKTOK-20261115-001`, verify original release/recording and add it to the playable-import pipeline when an authorized master is available.
- [ ] **T013** · Implement high-fidelity source asset vault and deterministic Minecraft/native export with quality, hash, credits, provenance and recovery checks.
- [ ] **T014** · Build authoring/QA UI for waveform, beat grid, adaptive segment timeline, intensity automation, stem alignments, and calibration and accessibility state.
- [ ] **T015** · Verify real-player 60-fps music/combat/dance path under representative busy server/modpack conditions, 60/120/144 Hz display targets and multiple audio buffers/devices; measure event-to-audible-impact latency, timing jitter, underruns, clip peak and wrong-state/crossfade rate on equivalent complete musical content. Optimize without muting important cues or limiting content.
- [ ] **T016** · Validate multiplayer authority, pause/restart, audio device switch, pack uninstall, missing rights/media entry, incomplete stems, settings persistence, reduced-flash/accessibility and source-mod behavior regression. Save repeatable evidence and correct failures.
- [ ] **T017 [P]** · Resolve exact source/video/creator and audio metadata for `ZPLK2mF7q` and `ZPLK2Fnk7`; determine whether they actually share an artist; audition/credit and shortlist creator music without inventing any track identity.
- [ ] **T018** · Implement owner-specified **Ocarina of Cumbia** rare Cumbias variant with optional recognized Zelda-character cameo cast, properly verified source audio, portable adaptive music, scene/rewards/replay and ultra-rare non-invasive spawns; satisfy all [OCA acceptance gates](Ocarina-of-Cumbia-Easter-Egg).
- [ ] **T019** · Resolve and import **JSC-MUST-CUMBIA-20261115-001** using the owner's exact Spotify ID 6k3L5dAbeLrNN5TIoLtSze: verify official title/artist/edition, acquire a usable full-quality master and credit provenance, author a legitimate cue/beat map if used for a scored Mortal Kumbia mode, and prove playback inside JetSetCraft without replacing any earlier selection.
- [ ] **T020** · Acquire and integrate the **exact owner-selected Jazztick “Kokiri Forest” recording** (`JSC-MUST-CUMBIA-20261115-002`, Spotify `6P4ufhmrdt07Vpjm66VpiE`): validate master provenance, performer/composer/arrangement credits and original-quality preservation, audition and map a genuine cue for Cumbias radio/Boombox and the [Ocarina Easter egg](Ocarina-of-Cumbia-Easter-Egg), then prove exact track playback + optional synchronized layers at runtime without falsely matching either TikTok reference.
- [ ] **T021** · Integrate NiNi artist-level requirement in **general** soundtrack: find and audition specific artist works, acquire the exact chosen highest-fidelity masters with artist/featured credits, then prove general radio and adaptive reactive playback independently of Cumbias.
- [ ] **T022** · Acquire exact Cain Culto & Jarina De Marco “chismosa” master and source credits, validate true tempo/beat grid and implement Cumbias Boombox/encounter and Mortal Kumbia music with tested audio playback; avoid mislabeling NiNi, Ocarina, or unrelated prior Cumbias selections.
- [ ] **T023** · Acquire the exact [Susobrino & Xiuhtezcatl “PIZALE” recording](https://open.spotify.com/track/7kXUQovYN1rCipqtoBSAhN) (`JSC-MUST-CUMBIA-20261115-003`), source its official credits and permitted high-fidelity master, measure actual beat/downbeat data, implement crew-affinity playback and optional Mortal Kumbia charting, and prove correct Cumbias gameplay playback without modifying earlier approved tracks or introducing guessed stems.
- [ ] **T024** · Acquire and author the owner-selected judith **“TUSI”** recording (`JSC-MUST-CUMBIA-20261115-004`, Spotify `4cOJ9DJttuDraD6RRFXDPy`), preserving official original song/artist/release credits and a high-quality music source independent of Minecraft derivatives; verify measured true beat grid (catalog 160 BPM / C minor is unconfirmed), enable actual Cumbias player/Boombox playback and an optional real Mortal Kumbia integration, test multiplayer timing/availability and mark done only after observed runtime playback.
- [ ] **G001 · GATE** · All 68 KI track acquisitions correctly accounted for, all asset families/edition extras statused, no false imported claims, source/credit lineage recorded.
- [ ] **G002 · GATE** · Reactive system produces true sample-timed musical responses and earned KI-strength combat feedback in Minecraft while remaining standalone-portable.
- [ ] **G003 · GATE** · Both named Mortal Kumbia core modes plus expressive extensions are genuinely playable with calibrated scoring and cultural identity.
- [ ] **G004 · GATE** · Full-fidelity vault, runtime export lineage, existing gameplay/audio regressions and cross-device performance gates pass without hidden quality/quantity limits.
- [ ] **G005 · FINAL** · Entire spec audited and exact wiki published/read back from the **live** GitHub Wiki, not repo mirror alone; Drive complete `.md` checkpoint persisted; relevant tests and practical gameplay proof attached; all accepted tasks/gates satisfied without unsupported completion claims.

**Execution rule:** Reuse canonical repository IDs, resolved sources and proof; continue the earliest executable unchecked task instead of restarting. Implement one real vertical slice, target-test it, checkpoint source, then extend without shrinking scope. An inaccessible TikTok, absent stem, or external permission document is a **tracked, actionable unresolved input**, not a reason to fake completion or silently substitute. This page records the vision and an executable backlog; it does **not** change the shipped-status table by itself.