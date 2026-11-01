# Honkistas NPC quality and skating physics

**Owner-selected quality target, October 20, 2026. Design and implementation requirements, not a shipped-feature claim.**

The circus/clown crew is the **Honkistas**. This reference supplements their existing costume, dance and music collection, named members, recruitment/skin plans and surprise-after-surprise encounter. It does not replace Sav, PESTRiCA, Tay, mennah or Plague, or create another faction.

## Selected CPM benchmark

- Supplied file: **PierrotThefreakCircus-Model.cpmmodel**.
- Embedded name: **Pierrot The freak Circus - Model**.
- Embedded credit: **Model by: Hibrook | Game for: Neko Bueno**.
- SHA-256: fdc031dae4f924f82cc748ecde24f7a6c777be6463c20bb0da3d9ce277884c26.
- Container inspection: 130,127 bytes; valid checksum; embedded model/overflow data and a 256 × 256 preview image. No remote resource was fetched during this inspection.

The owner selected this as the desired level of NPC detail and presentation, especially for the Honkistas. The embedded preview shows a strong theatrical face, long white hair and red/black circus styling. That preview is not a verified in-game render or a complete rig inspection. The later structural and native-preview checkpoints below add direct evidence; complete rig behavior and existing physics still require runtime review.

Preserve creator credit and the original file. Use it as a quality reference, not permission to claim another creator's work as original. Confirm adaptation and redistribution rights before shipping the supplied model or derived assets. Do not publish private download links or assume a model-file checksum establishes a license.

## Required visual quality

- Distinctive, polished character silhouettes, expressive faces and carefully authored hair/costume detail, readable both close up and in moving crowds.
- Original Honkistas member variation, not identical bodies with recolored clothes. Keep each named member recognizable.
- Finished front, back, side and skating views. Match the selected quality benchmark in actual gameplay, not only in a flattering still image.
- Good shoulders, elbows, knees, hands and costume deformation during skating, tricks, dance, taunts and combat. Avoid stiff mannequin poses and intersections concealed by camera framing.
- The same high bar applies to other non-mob music crews with their own identities; the circus treatment belongs specifically to Honkistas.

## Hair and loose parts must respond to skating

The owner's requirement is **motion-responsive secondary physics**, not a looping sway unrelated to movement. Identify eligible parts on each authored NPC: hair locks and tails, loose ribbons, hat tails/tassels, coat hems, loose cuffs, pendants or bells where present. Rigid ornaments, attached collars and roots should remain anchored rather than wobbling indiscriminately.

Required motion cases:

1. Acceleration and braking produce believable lag, follow-through and settling.
2. Turns, powerslides and spins produce directional trailing and recovery.
3. Jumps, airtime, landings, grinds and transfers produce controlled responses without explosive snapping.
4. Dance and head/torso gestures combine with secondary motion without destroying intentional choreography or facial readability.
5. Idle settles naturally; stopping, pausing or changing states does not leave parts vibrating forever.

Use constrained, damped motion with bounded timesteps and stable attachment roots. Define per-part stiffness, damping, mass response and limits as authored settings, not one global floppy-hair preset. These are implementation directions, not proof that the supplied CPM already contains such a system.

## Integration and safety boundaries

- Blend secondary motion with the primary CPM/character animation instead of overwriting bone ownership unpredictably.
- Keep cosmetic motion separate from authoritative movement, hitboxes, damage, trick scoring and AI. Hair simulation must not decide a hit or change skating traction.
- Prevent hair and clothing from passing through the face, torso, arms, ride equipment or held TaCZ weapons. Handle both hands and all supported ride poses.
- Reset or safely reinitialize on teleport, dimension change, entity replacement, death, reconnect, model reload and large time discontinuities.
- Preserve character identity, saved customization and source-owned mob behavior. A renderer/physics failure must leave a usable fallback rather than an invisible NPC or broken movement.
- Use distance/visibility-based simulation budgets and measured crowd workloads. Blend quality transitions so hair does not pop; do not claim performance without measurements.
- Provide reduced-secondary-motion options and readable effects without erasing the character's silhouette.
- Follow the existing version/loader architecture and first-target priority; do not treat a CPM file as automatic compatibility with every target.

## Embedded CPM structure checked on October 20, 2026

A bounded read-only parse of the supplied file’s embedded definition passed its checksum without following its external cache link or executing model commands. This is structural evidence, not a native rendering or skating-physics pass.

- 269 textured cube records, with a maximum parent chain of 10 and no unresolved non-vanilla parent IDs in the parsed definition. These are reference facts, not a minimum cube-count target for every NPC.
- 92 records have at least one zero base dimension before render effects. Preserve intentional planes and extrusion semantics; do not discard a hair/cloth detail merely because its base box is flat.
- The embedded PNG is 512×512, while the declared logical texture coordinate size is 2048×2048. Preserve that distinction when adapting UVs instead of blindly normalizing by PNG dimensions.
- 20 animation definitions were found, including global, head-pitch, standing, sneaking and jumping pose bindings plus layer/value controls. Existing expressions and toggles must survive adaptation. Animation metadata alone does not establish physical hair/cloth simulation.
- The file contains 174 per-face-UV effect records, 106 scale records, 5 hide records, 3 extrusion records, one scaling record and one remove-armor-offset record. These are effect records, not additional unique cubes. Preserve their ordering and ownership when importing.

The skating secondary-motion layer must be composed with the existing rig and animation, not overwrite head-pitch, expression or pose channels. Root transforms, flat textured details, hidden layers and reset behavior need explicit tests in the actual NPC renderer. A bounded native preview is recorded below; actual NPC integration and skating physics remain open.

Format interpretation was checked against the [official CPM source pin](https://github.com/tom5454/CustomPlayerModels/tree/129b76fc7561fb0947107fe45a954881ac774921). The supplied asset and embedded credit remain unchanged. [Implementation tracking #38](https://github.com/Herbertofury/JetSetCraft/issues/38).

## Native reference preview checked on October 20, 2026

The unchanged supplied model rendered successfully in the official CPM 0.6.27a Models preview under Minecraft 1.20.1 / Forge 47.4.28 in an isolated private test instance. Front, opposite-side and rear views were inspected. The actual render shows layered white hair, a sculpted crimson/black jester hat, gold costume trim, embroidered panels and detailed boots. The original embedded creator credit was visible. No skin was applied or uploaded, and the reference binary was not published.

This is a native **reference-preview** checkpoint, not an implemented Honkistas NPC, an in-world skating test, or proof of existing secondary physics. Rig ownership, model-specific collision envelopes, authored pose composition and every skating/physics acceptance case remain open. The standalone original chain-solver prototype has 164 scalar assertions, not 164 native gameplay tests; it is not yet connected to this model or the JetSetCraft NPC renderer.

## Implementation and acceptance checklist

- [x] Inspect the unchanged supplied CPM in an isolated official native CPM preview, including front, side and rear appearance.
- [ ] Complete rig/part ownership and animation-behavior review, including identifying any existing secondary physics.
- [ ] Record reference/adaptation rights and retain credit before distributing assets.
- [ ] Produce original Honkistas member sheets and in-game models at the selected quality level.
- [ ] Separate rigid, animated and physics-driven parts with explicit ownership and collision envelopes.
- [ ] Implement movement-driven hair/loose-part response and interruption/reset handling.
- [ ] Test normal skating, acceleration, hard braking, sharp turns, slides, spins, jumps, landings, grinds, transfers, dance and held-weapon poses.
- [ ] Test low/high frame rates, variable frame time, pauses, teleport, reload, disconnect, multiplayer late join and dense NPC crowds.
- [ ] Capture close and ordinary gameplay views, including opposite-side and rear views, and review clipping, silhouette, settling and choreography.
- [ ] Measure client frame time, allocation and simulation cost; keep untested versions and configurations explicitly open.
- [ ] Obtain visual acceptance separately from structural tests. A valid file, successful import or static preview is not finished quality.

[Existing Honkistas roster and encounter requirements](Music‐Driven-Dances-and-Mob‐Themed-Bosses) · [Animation and model research](Research-Animation-Models-and-Creators) · [World physics](World-Physics) · [Roadmap](Roadmap)
