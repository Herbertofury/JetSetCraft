# JetSetCraft Handoff

Canonical repo: `Herbertofury/JetSetCraft`; target Forge 1.20.1 / Java 17 / mod version `0.1.0-alpha.1`.

Current source has inline, quad, board and BMX ride states; momentum/boost/manual/tricks/combo; arbitrary block-edge grinding; first-class vanilla/Forge/custom rail grinding; native optional Create 6.0.8 track geometry including long Bezier curves; rail junction steering, rail-hop transfers and rail tricks; wall rides; powerslide; graffiti; PlayerAnimator lower-body clips; TACZ `IGun` detection; configurable camera effects; procedural dense OBJ models.

Immediate next gate: publish the coherent `jetsetcraft-alpha` repo checkpoint, run GitHub Actions clean build, fix every compile/runtime issue, download the produced JAR, and mirror verified checkpoint/artifact to Google Drive. The exact owner-supplied JetSetCraft icon/cover PNG is now preserved as `source_assets/brand/jetsetcraft_official_art.png` and drives deterministic icon/banner generation. The owner-supplied `createrailgrinding-1.2.2.jar` and 135-file `JSRGraffiti.zip` were inspected and integrated. See `docs/RAIL_REFERENCE_AUDIT.md` and `docs/ASSET_PROVENANCE.md`.
