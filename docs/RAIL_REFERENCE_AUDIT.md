# Create Rail Grinding 1.2.2 reference audit

JetSetCraft was given `createrailgrinding-1.2.2.jar` by the project owner as an implementation reference.

- SHA-256: `9c27bba88d89e1ec081f6263e47f7b56226817394caaf1c2148e11c31e037c69`
- Embedded mod id: `createrailgrinding`
- Version: `1.2.2`
- Embedded license declaration: MIT
- Runtime target in the supplied binary: NeoForge `[21.1.219,)`, Create `[6.0.8,6.1.0)`
- The binary is therefore **not** a drop-in Forge 1.20.1 dependency. JetSetCraft ports applicable architecture into its own Forge 1.20.1 rail engine instead.

## Valuable architecture recovered from the reference

The bytecode exposes a mature stateful grind controller with concepts worth carrying into JetSetCraft:

- graph and block-level track scanning rather than block-name heuristics;
- exact Bezier track sampling;
- per-track-type lateral/y offsets for standard, narrow, wide, monorail, and universal rails;
- slope-aware acceleration and downhill/uphill tuning;
- smoothed curve-speed factor rather than abrupt turn penalties;
- start grace, reattach grace, post-reattach suppression, stuck detection, and extreme-slope handling;
- cross-dimension / portal re-grind recovery;
- junction steering;
- server-authoritative targets plus client correction / blocked-path detection;
- rail jump charging and momentum-preserving launch;
- fall-damage grace after a grind exit;
- speed-reactive grinding audio, collision audio, particles, camera roll, and debug visualization;
- optional sublevel compatibility and track-gauge compatibility checks.

## JetSetCraft integration status

Integrated into the generalized Forge 1.20.1 engine:

- Create graph/Bezier and block track sampling;
- actual rail-bar offsets instead of centerline-only riding, with track-type-aware narrow/wide/monorail handling;
- curve-aware temporary travel-speed shaping while preserving stored momentum;
- slope momentum influence;
- junction steering and rail transfers;
- stuck detection/recovery;
- extended cross-dimension reattachment grace;
- server-authoritative locomotion and target resampling.

Held as an atomic later runtime gate rather than partially copied:

- steep-slope `noPhysics` mode plus its matching forward obstacle detector / packet safeguards;
- train-overlap/crush handling;
- sublevel/Sable transforms;
- portal graph-hop teleport internals;
- reference audio/particle loop implementation.

Those pieces must be ported and tested together because enabling collision bypass without its obstacle and movement-packet safeguards would be a regression.
