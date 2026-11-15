# Original Honkistas actor prototype

An original theatrical actor prototype is authored from code, with a custom articulated silhouette and ivory/teal/coral/gold color layers. It is not a vanilla-mob reskin, a copy of the private Pierrot reference, or a representation of Sav, PESTRiCA, Tay, mennah, Plague, Lucy, Grinions or another creator's character.

The native entity, model and renderer are registered independently as `jetsetcraft:honkistas_prototype`. There are no natural spawn rules, production event-provider bindings, soundtrack, rewards or character substitutions. The existing mesh/ModelPart and deterministic texture tooling is reused; the white sampler is generated locally without imported artwork.

## Evidence boundaries

- Native GameTest checks actual world registration, original type identity, attributes and save/load class continuity.
- The existing opt-in disposable RivalRushAudit client run appends front, side and back captures and verifies the client sees the original entity with its registered renderer.
- These are preview views, not finished-quality acceptance. Inspect the actual PNGs before judging silhouette, face readability, costume depth and clipping.
- Basic walking/head animation is not movement-responsive secondary physics, authored dance choreography or verified skating.
- Do not enable this prototype as an accepted Honkistas encounter provider merely because compilation and a static preview pass.

## Remaining visual acceptance

Compare with the owner's theatrical quality target without copying the supplied creator model. Review original front/back/side views, then skating and dance poses, shoulders/elbows/knees/hands, held objects, crowd readability, low/high frame times and motion-responsive hair/cloth. Author expressive variation and collision envelopes before promising finished NPC quality.

See [quality requirements](Honkistas‐NPC‐Quality‐and‐Skating‐Physics) and [persistent sequence](Honkistas-Persistent-Sequence).

## First preview review

The first three captures at PR head `b256bb8` verified rendering but inherited the prior underwater fixture. Darkness, crosshair, held item and HUD prevent reliable visual-quality acceptance. These failed-quality captures remain in workflow run `43706166634`. The revised audit uses open daylight airspace, hides HUD and restores camera, HUD state, position, gravity and world time afterward. No terrain or inventory is changed. Revised pixels still require review.

## Daylight review, 2026-11-15

Exact head `7ba4e28845ec257330514f51cc6be3bf8a03b9dd` passed all eleven CI lanes, including all 129 required base GameTests. Workflow `43707174203`, artifact `14159491096`, contains the independently inspected front/side/back PNGs. Camera framing and daylight now expose the original model without HUD interference. The face, asymmetric shoulder treatment, lapels and split tails render. This confirms the renderer and capture path, **not finished visual acceptance**.

The review found a largely blank rear head, slab-like rectangular coat tails, blocky hands and boots that nearly merge (including a slight mesh overlap). These are geometry refinements, not excuses to accept poor capture lighting. Both original and daylight evidence are retained. Standing still above the ocean does not demonstrate walking, skating, choreography, crowd performance or cloth/hair physics. Skating/dance state and a grounded native motion lane are not implemented by this preview.
