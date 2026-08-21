# Graffiti

The Graffiti Spray Can places persistent wall-mounted graffiti entities with selectable art, aspect-aware sizing, and repaint-in-place behavior.

## Controls

- **Shift + use** the spray can to cycle the selected catalog entry.
- **Use on a wall** to place the selected decal.
- Spray an existing JetSetCraft graffiti entity to repaint it instead of stacking a duplicate in the same space.

## Catalog and provenance

The runtime catalog is generated from preserved source art. Every entry records a stable ID, resource texture, width, height, and source name when applicable. Aspect ratio is retained rather than forcing all art into the same square.

A clean offline source build always contains four original JetSetCraft fallback tags. When the owner-approved JSR archive is available, the generator accepts it only after exact SHA-256 verification, then normalizes PNG decoding without resizing the original pixels. A failed or partial download is removed and never trusted.

## Server control

Servers can disable graffiti independently with the common/server configuration. Existing entities remain ordinary saved entities; the setting controls new use rather than silently deleting player art.

## Adding original art

Add legally usable source art, preserve creator/source/license notes in `docs/ASSET_PROVENANCE.md`, extend the deterministic generator/catalog, and run asset validation. Do not paste untracked web images directly into runtime resources.
