# Street movement and paint upstream merge

JetSetCraft remains the owning product and Forge 1.20.1 runtime. This merge adds proven ideas from Street Art and
Dream Burst Spirit Vector without removing JetSetCraft's six ride styles, loadout, server authority, combos,
rail/edge/Create grinding, dance system, mob gear, graffiti catalog, or bounded custom editor.

## Movement merged

- stride-pulsed ground acceleration with a responsive input floor;
- angular drift steering, accumulated turn charge, and a small intentional release kick;
- three grounded-reset wall kicks, wall-plane reuse protection, reduced wall gravity, and a 20-tick wind redirect;
- sprint+jump kickoff and collision-checked ledge vaults;
- existing JetSetCraft coyote time, world-material physics, micro-terrain assist and external impulses remain active;
- exact neutral stop and the complete vanilla fluid velocity/pose boundary remain hard requirements.

## Paint merged

- all 16 vanilla dye paint balloons with the authorized Street Art textures;
- 1,000-ray bounded balloon splashes and smaller 240-ray free-spray splashes;
- removable, persistent decals on all six faces, with deterministic custom splat canvases and per-chunk limits;
- the existing spray-can gallery and custom 16x10 editor remain intact;
- the selector now toggles between full Tag placement and Free Paint, with a 16-color palette.

## Models merged

Inline and quad skates now use Street Art's leg-baked rollerblade mesh and authorized textures. The model copies the
actual player-leg pose every frame, so it follows ride, drift, wallrun and trick animation rather than hovering as a
generic duplicated item transform.

Full immutable upstream snapshots are stored in the private canonical project under
`source_assets/authorized/{street-art,spirit-vector}/`; public source distributions contain the adapted source,
included assets, revision provenance, and required MIT notices.
