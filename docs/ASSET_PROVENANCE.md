# Asset Provenance and Rights Ledger

JetSetCraft keeps source provenance separate from runtime derivatives so an asset can be replaced, upgraded, re-exported, or removed without losing lineage.

## Current bundled assets

- `models/obj/*.obj`, `models/obj/*.mtl`, `textures/item/*`: generated/original JetSetCraft project assets from `tools/generate_models.py` unless separately noted below.
- `textures/graffiti/tag_3.png`: original JetSetCraft generated tag.
- `textures/graffiti/tag_0.png` through `tag_2.png`: generated from a locally available JSRF reference sheet when that optional source is present; otherwise the clean-source build generates original JetSetCraft fallback tags so no shipped decal is blank.
- `source_assets/authorized/jsrf/graffiti/poison_jam_sheet.png`: authorization-backed source/reference sheet kept outside runtime resources; the build only ships derived graffiti decals. It is optional: if absent, the clean-source build generates original JetSetCraft fallbacks.

## SEGA authorization evidence

The user supplied a screenshot in this project conversation of a SEGA support email as authorization evidence regarding Jet Set Radio asset use. The exact wording shown in that evidence governs; this ledger does not broaden or reinterpret the grant. The screenshot itself is not currently stored as a repository file.

## User-created JetSetCraft graffiti

The user stated that additional graffiti assets were created specifically for JetSetCraft. Those exact image files did not arrive in the current attachment/file layer at the time of this checkpoint, so they are **not yet claimed as bundled**. The graffiti catalog was refactored so those assets can be added without changing the entity/network format once the files are available.

## Researched but not bundled

Potential CC0/original-source candidates have been researched for future visual upgrades (including Kenney CC0 skate-park assets and CC0 BMX/board sources). A research result is not permission to claim the bytes are bundled: add an entry here only after the exact downloaded artifact is recorded with source URL, creator, license/grant, hash, and any required attribution.


## Official JetSetCraft brand art

- Source: project-owner-supplied artwork in the ChatGPT project conversation (2026-08-20/21).
- Canonical master filename when mounted: `source_assets/brand/jetsetcraft_official_art.png` (verified durable copy in Google Drive Assets; intentionally not duplicated into Git).
- Production Forge icon committed to Git: `src/main/resources/jetsetcraft.png`, deterministically derived from the master.
- Policy: preserve exact pixels as source. Generated icon/banner files may only resize or crop with deterministic high-quality resampling; no AI regeneration, repainting, denoising, or style transfer.


## Owner-supplied JSR graffiti archive

- Input: `JSRGraffiti.zip`, supplied by the project owner.
- SHA-256: `8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9`.
- Contents: 135 PNG graffiti designs.
- Canonical preserved source archive: exact owner-supplied `JSRGraffiti.zip`, SHA-256 `8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9`, mirrored to Google Drive Assets.
- Clean Git checkouts fetch the public byte-identical mirror at `https://storage.googleapis.com/greg-kennedy.com/jsr/JSRGraffiti.zip` and abort unless the SHA-256 matches the owner-supplied master.
- Runtime processing: dimensions and aspect ratio are preserved; PNG decoder mode is normalized to RGBA only. No redraw, resize, denoise, AI regeneration, or style transfer is performed.
