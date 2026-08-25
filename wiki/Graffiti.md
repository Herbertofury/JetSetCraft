# Graffiti

The Graffiti Spray Can has two persistent modes: full **Tag** placement and compact **Free Paint**. It shares one selector with the catalog, bounded custom editor, and complete sixteen-color palette. Throwable paint balloons add larger surface-aware splashes.

## Controls

- **Use in air** to open a paged visual gallery containing every catalog entry.
- **Paint custom** opens a bounded 16 × 10 pixel canvas with sixteen palette slots, erase, clear, cancel, and done controls.
- **Mode: Tag / Mode: Free Paint** toggles the current can behavior. Picking a catalog card returns to Tag; picking a color switches to Free Paint.
- **Shift + use on a block** opens the gallery without changing the wall.
- In **Tag**, use a horizontal wall to place the selected catalog or custom decal.
- In **Free Paint**, use any sturdy block face—including floors and ceilings—to spray a compact splat in the selected color.
- Throw any of the sixteen dye-colored paint balloons to expose nearby surfaces with a bounded 1,000-ray splash. Dispensers can launch them too.
- Spray an existing JetSetCraft graffiti entity to repaint it instead of stacking a duplicate in the same space.

## Catalog and provenance

The v0.3.0 runtime catalog contains 139 entries generated from preserved source art. Every entry records a stable ID, resource texture, width, height, and source name when applicable. Aspect ratio is retained rather than forcing all art into the same square.

A clean offline source build always contains four original JetSetCraft fallback tags. When the owner-approved JSR archive is available, the generator accepts it only after exact SHA-256 verification, then normalizes PNG decoding without resizing the original pixels. A failed or partial download is removed and never trusted. Custom designs and generated splats use a fixed 80-byte packed payload, are validated server-side, and persist with the graffiti entity; clients cannot submit arbitrary files or textures.

The Free Paint ray-coverage approach, balloon behavior, balloon textures, and leg-baked rollerblade presentation were adapted from the authorized MIT-licensed Street Art source. Exact revision, file mapping, hashes, and notices live in `docs/ASSET_PROVENANCE.md`, `docs/UPSTREAM-MERGE.md`, and `THIRD_PARTY_NOTICES.md`.

## Server control

Servers can disable all new painting independently and set `maxGraffitiPerChunk` (default 128). Both spray and balloons enforce interaction permissions, loaded-chunk checks, sturdy-face checks, a hard patch count, and the same per-chunk limit. Repainting an occupied support face does not consume another slot. Decals persist through saves, can be removed by attacking them, and automatically clean themselves up when their supporting block disappears.

## Adding original art

Add legally usable source art, preserve creator/source/license notes in `docs/ASSET_PROVENANCE.md`, extend the deterministic generator/catalog, and run asset validation. Do not paste untracked web images directly into runtime resources.
