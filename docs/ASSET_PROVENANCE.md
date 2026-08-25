# Asset Provenance and Rights Ledger

JetSetCraft keeps source provenance separate from runtime derivatives so an asset can be replaced, upgraded, re-exported, or removed without losing lineage.

## Current bundled assets

- `models/obj/*.obj`, `models/obj/*.mtl`, `textures/item/*`: generated/original JetSetCraft project assets from `tools/generate_models.py` unless separately noted below.
- `textures/graffiti/tag_0.png` through `tag_3.png`: original deterministic JetSetCraft fallback/project tags.
- `textures/graffiti/jsr/*.png`: 135 runtime decals extracted without redraw from the exact owner-supplied `JSRGraffiti.zip` described below. Dimensions and aspect ratios are recorded in `graffiti/catalog.json`.
- `sounds/music/gangs/*.ogg`: eighty original deterministic JetSetCraft entrance stingers described below.
- `textures/entity/ride/street_art/*.png` and `textures/item/street_art/paint_balloon/*.png`: MIT-licensed
  Street Art rollerblade and 16-color paint-balloon assets described below.

## Authorized MIT upstream merges

### Street Art

- Source: [BeeIsYou/street-art](https://github.com/BeeIsYou/street-art), revision
  `63771c98742b76e2949223215aa2a60f1b6386e9`.
- License: MIT, copyright 2026 BeeIsYou; preserved at `third_party/street-art/LICENSE`.
- Canonical full-source snapshot: `source_assets/authorized/street-art/street-art-63771c9-source.zip`, 355,469
  bytes, SHA-256 `86db7e55ed8153dd52ebfc2be3ed04a191246dbcb426cab812fd4ad43f7078fc`.
- Runtime extracts: 16 original paint-balloon textures; red/green rollerblade textures; rollerblade mesh geometry;
  adapted movement and paint-splash code identified in `THIRD_PARTY_NOTICES.md`.
- Inline texture SHA-256: `9578598b316299c426031e041c5cc014b714567c08c656cb987ac7ceb78d0903`.
- Quad texture SHA-256: `d535d82e7c703b7285d74e1a7c22d86629b6a1bc7864118955c1dd132327bf6e`.

### Dream Burst Spirit Vector

- Source: [HamaIndustries/spirit-vector](https://github.com/HamaIndustries/spirit-vector), revision
  `623e7df026788add9737ea508b65baf80f66623d`.
- License: MIT, copyright 2024 hama Industries; preserved at `third_party/spirit-vector/LICENSE`.
- Canonical full-source snapshot: `source_assets/authorized/spirit-vector/spirit-vector-623e7df-source.zip`,
  58,038,317 bytes, SHA-256 `a494063098ffadb48d21adfba2404fae56838629a05689e0a387cbdac230650c`.
- Runtime use is a Forge 1.20.1 adaptation of selected kickoff, ledge-vault, wall-plane and state-machine ideas;
  Spirit Vector's fantasy items, runes, soundtrack, branding, and alternate HUD are not bundled.

## SEGA authorization evidence

The user supplied a screenshot in this project conversation of a SEGA support email as authorization evidence regarding Jet Set Radio asset use. The exact wording shown in that evidence governs; this ledger does not broaden or reinterpret the grant. The screenshot itself is not currently stored as a repository file.

## Official JetSetCraft brand art

- Source: project-owner-supplied artwork in the ChatGPT project conversation (2026-08-20/21).
- Canonical master: `source_assets/brand/jetsetcraft_official_art.png`, 1,254 × 1,254, 1,399,884 bytes, SHA-256 `08c7e3f4998b268a7776c5a962082eb5eaa86124b4a0f53d9e502a74e2a8bf4d` (preserved in the complete Desktop/Drive project; intentionally excluded from Git).
- Production Forge icon committed to Git: `src/main/resources/jetsetcraft.png`, deterministically derived from the master.
- Repository presentation derivatives: `art/generated/jetsetcraft-icon-512.png` and `art/generated/jetsetcraft-banner-1200x630.png`.
- Policy: preserve exact pixels as source. Generated icon/banner files may only resize or crop with deterministic high-quality resampling; no AI regeneration, repainting, denoising, or style transfer.


## Owner-supplied JSR graffiti archive

- Input: `JSRGraffiti.zip`, supplied by the project owner.
- SHA-256: `8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9`.
- Contents: 135 PNG graffiti designs.
- Canonical preserved source archive: `source_assets/authorized/jsr/JSRGraffiti.zip`, SHA-256 `8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9`, mirrored in the complete Google Drive project.
- Clean Git checkouts may fetch the public byte-identical mirror at `https://storage.googleapis.com/greg-kennedy.com/jsr/JSRGraffiti.zip`; downloaded bytes are accepted only when the SHA-256 matches the owner-supplied master. Offline or unavailable builds delete partial downloads and generate original JetSetCraft fallback tags instead of failing or shipping corrupt data.
- Runtime processing of the authorized archive preserves dimensions and aspect ratio; PNG decoder mode is normalized to RGBA only. No redraw, resize, denoise, AI regeneration, or style transfer is performed.


## Style Flow generated equipment and animation assets

- `tools/generate_models.py` is the authored source for inline skates, quad skates, skateboard, BMX, hoverboard, scooter, spray can, and supporting item textures.
- The hoverboard is a dedicated wheel-free mesh with energy rings, stabilizers, and generator details; it no longer points at the skateboard OBJ.
- The scooter is an original generated mesh with deck, wheels, stem, handlebar, grips, and grind rails.
- `tools/generate_animations.py` deterministically generates hoverboard and scooter ride/boost clips, eight aerial-trick clips, eight grind-trick clips, twenty-eight dance clips, and eight ground-stunt clips. UUIDs are derived deterministically from the JetSetCraft namespace and clip name.
- These generators use project-authored geometry/keyframes and do not download character models, motion-capture files, copyrighted game animations, or AI-generated substitutes.
- `tools/validate_assets.py` enforces model-quality floors, valid OBJ face indices, complete item-model coverage, deterministic clip presence, no held-item bones, and lower-body-only ownership for ride/combat-compatible clips.

## Original gang entrance audio

- `tools/generate_audio.py` synthesizes every packaged gang entrance stinger from deterministic oscillators, envelopes,
  and seeded noise using Python's standard library; FFmpeg only encodes the generated PCM as Ogg Vorbis.
- No sample, stem, recording, model, or third-party audio is downloaded or read by the generator.
- `tools/audio_manifest.json` records the exact packaged SHA-256, byte size, duration, peak, and RMS for each asset.
- `tools/validate_assets.py` rejects missing, malformed, changed, too-short, or effectively silent gang audio.
