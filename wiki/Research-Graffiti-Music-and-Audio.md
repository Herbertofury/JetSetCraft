# 🎨 Graffiti, Music & Audio

> [!NOTE]
> References for spray interaction, surface placement, custom art, paint projectiles, physical music systems, multiplayer playback, sound design, and texture sourcing.

**[← Inspiration & Research Atlas](Inspiration-and-Research)** · [Canonical 173-row Sheet](https://docs.google.com/spreadsheets/d/1liWhRQL7rmPy8LLgSRVlkrLANqUIJE4UbFX3--KMJjg/edit?gid=1570163195#gid=1570163195)

## Graffiti & Painting · 9

Surface-aware painting, decals, custom creation, palettes, splashes, and paint projectiles.

| Project | Environment | What to study | JetSetCraft opportunity | Links | Rights / contact |
|---|---|---|---|---|---|
| **SDF Graff**<br><sub>Mod reference · score 8.35</sub> | Java<br>Fabric<br>1.21.1 | Realistic spray painting; distance controls radius/opacity; close spray creates thin opaque lines; particles, sound and cleaner. | Top paint-feel reference for nozzle distance, line thickness and analog spray behavior beyond decal placement. | [Project](https://www.curseforge.com/minecraft/mc-mods/sdf-graff) | Ask author / verify terms |
| **Immersive Paintings**<br><sub>Mod reference · score 8.20</sub> | Java<br>Fabric / Forge / NeoForge<br>Multi-version; updated 2026 | Custom images, pixelation/post effects/dithering, frames, caching/lazy streaming, transparent images usable as wall graffiti, datapack/admin limits. | Architecture reference for safe user media streaming/caching and admin controls if JetSetCraft expands custom image tagging. | [Project](https://modrinth.com/mod/immersive-paintings) | Ask author / verify terms |
| **Fresco**<br><sub>Mod reference · score 7.70</sub> | Java<br>NeoForge<br>1.21.1 (2026) | Pigmentum canvas API, brush, stamps, spray cans and cloths; paint/copy/spray/blend pixels on block surfaces. | Fresh reference for multiple painting tools sharing one surface data model and for blending/editing workflows. | [Project](https://modrinth.com/mod/fresco) | Ask author / verify terms |
| **rtGraffiti**<br><sub>Mod / source reference · score 7.70</sub> | Java<br>Fabric / NeoForge<br>1.21.1 | Pencil, eraser, fill and picker tools; pixel art on any block surface. | Useful tool UX reference for editing directly on world faces and preserving crisp pixel intent. | [Project](https://modrinth.com/mod/rtgraffiti) | Ask author / verify terms |
| **Graffiti - Fureniku**<br><sub>Mod reference · score 7.30</sub> | Java<br>Forge<br>1.15.2 | Draw/text on walls, floors and ceilings; multiple canvases per blockspace; canvas editor; 16x16 to 128x128; JSON import/export. | Strong reference for canvas data format, scaling, editing and multiple independent artworks sharing one block volume. | [Project](https://modrinth.com/mod/graffiti) | Ask author / verify terms |
| **Spray Madness**<br><sub>Mod reference · score 6.85</sub> | Java<br>Fabric<br>1.18-1.19 | Import PNG/JPG sprays, eight-slot spray wheel, emissive sprays, client persistence and resource-pack spray packs. | Reference for user-imported spray content, radial selection and data-pack/resource-pack distribution. | [Project](https://www.curseforge.com/minecraft/mc-mods/spray-madness) | Ask author / verify terms |
| **Joy of Painting**<br><sub>Mod reference · score 6.30</sub> | Java<br>Multi-loader<br>Many versions | Blank canvas/palette and in-game painting flow. | Simple interaction reference for creative painting tools and intuitive palette UX. | [Project](https://modrinth.com/mod/joy-of-painting) | Ask author / verify terms |
| **Graffiti Canvas**<br><sub>Mod reference · score 5.05</sub> | Java<br>Fabric<br>26.x (2026) | Paint/load images on a Canvas Block. | Small modern image/canvas reference; lower priority than free-surface painting systems. | [Project](https://www.curseforge.com/minecraft/mc-mods/graffiti-canvas) | Ask author / verify terms |
| **Spray Cans**<br><sub>Mod reference · score 4.95</sub> | Java<br>NeoForge<br>1.21.1 | Spray cans and color tools for blocks/sheep; box/brush/bucket interactions. | Useful for survival crafting, durability and dye-language ideas around spray consumables. | [Project](https://www.curseforge.com/minecraft/mc-mods/spray-cans) | Ask author / verify terms |

## Music, Radio & Boombox · 8

Physical music objects, radio systems, sequencing, synchronization, and social playback.

| Project | Environment | What to study | JetSetCraft opportunity | Links | Rights / contact |
|---|---|---|---|---|---|
| **Etched**<br><sub>Mod reference · score 8.55</sub> | Java<br>Forge / NeoForge<br>1.20.1 / 1.21.1 | Album jukebox, jukebox minecart, radio, hopper automation, custom records via URL, MP3/WAV/OGG/SoundCloud/Bandcamp/Minecraft sounds, labels and artwork. | Best broad reference for media metadata, physical music devices, playlists and safe custom-record UX around JetSetCraft boomboxes. | [Project](https://modrinth.com/mod/etched) | Ask author / verify terms |
| **Glaiden's Audio**<br><sub>Mod reference · score 8.50</sub> | Java<br>Forge / NeoForge<br>1.20.1 / 1.21.1 (2026) | Boombox, radio transmitter, receivers/towers, dyeable cassettes, YouTube/SoundCloud/MP3, interference based on distance/height/weather, Sound Physics compatibility. | Excellent reference for in-world radio propagation, cassettes, signal degradation and environmental audio behavior. | [Project](https://modrinth.com/mod/glaidens-audio) | Ask author / verify terms |
| **AudioPlayer**<br><sub>Mod / source reference · score 8.15</sub> | Java<br>Fabric / server-side<br>Active 2026 | Upload MP3/WAV from URL/file/web, apply to discs/goat horns/heads, range controls and bulk application; Simple Voice Chat integration. | Reference for secure custom-audio ingestion, media objects and proximity playback. | [Project](https://modrinth.com/mod/audioplayer) · [Source](https://github.com/henkelmax/audio-player) | Ask author / verify terms |
| **Simple Voice Chat Addon Ecosystem**<br><sub>Ecosystem reference · score 7.40</sub> | Java<br>Fabric / Forge / NeoForge / Bukkit ecosystem<br>Broad | Spatial audio foundation plus music/radio/player-disc style addons. | Useful interoperability target if JetSetCraft boombox/radio features should coexist with proximity voice and custom audio mods. | [Project](https://modrinth.com/collection/9fRvCpKA) | Ask author / verify terms |
| **Boombox Radio**<br><sub>Mod reference · score 7.30</sub> | Java<br>NeoForge<br>2026 | Boombox with radio-channel UI and favorites; standalone evolution of an Ad Astra radio block. | Direct UX reference for favorites, station selection and a physical boombox object. | [Project](https://modrinth.com/mod/boombox-radio) | Ask author / verify terms |
| **Iam Music Player**<br><sub>Mod reference · score 6.85</sub> | Java<br>Fabric / Forge / NeoForge / Quilt<br>Through 1.20.1 | Boombox and shared playlists; Otyacraft Engine ecosystem. | Reference for shared playlist persistence and a compact multiplayer boombox flow. | [Project](https://modrinth.com/mod/iam-music-player) | Ask author / verify terms |
| **Sculk Radio**<br><sub>Mod reference · score 6.80</sub> | Java<br>Simple Voice Chat ecosystem<br>Modern | Streams jukebox or voice-chat audio through sculk radios/mics/speakers using frequencies and redstone signals. | Useful reference for multiplayer synchronized audio networks and redstone-controllable music events. | [Project](https://modrinth.com/mod/sculk-radio) | Ask author / verify terms |
| **Soundscape**<br><sub>Mod reference · score 6.50</sub> | Java<br>Modern<br>Modern | Volume/pitch/range/shuffle/queue/persistence, boombox URL playback and ambient discs. | Reference for quality-of-life playback controls and persistent queue state. | [Project](https://modrinth.com/mod/soundscape) | Ask author / verify terms |

## Audio & Texture Sources · 2

Libraries and production sources whose individual rights must be checked before use.

| Project | Environment | What to study | JetSetCraft opportunity | Links | Rights / contact |
|---|---|---|---|---|---|
| **Sonniss GameAudioGDC**<br><sub>Audio asset source · score 7.05</sub> | Asset<br>Download archive<br>Current | Large royalty-free game-audio GDC archives. | Excellent high-quality source pool for impacts, urban ambience, mechanical sounds and transitions; search the archive instead of settling for generic stock. | [Project](https://sonniss.com/gameaudiogdc/) | Verify archive license for each yearly pack |
| **Pixabay Sound Effects**<br><sub>Audio asset source · score 6.35</sub> | Asset<br>Web library<br>Current | Large searchable sound-effect library. | Source/reference for skate wheel loops, grind hits, spray hiss, urban ambience and UI sounds after rights verification. | [Project](https://pixabay.com/sound-effects/) | Check Pixabay license and per-asset restrictions |

---

**19 references on this page.** Inclusion records research value, not bundled code/assets or automatic redistribution permission. Return to **[Inspiration & Research](Inspiration-and-Research)**.

