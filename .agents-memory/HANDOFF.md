# JetSetCraft Handoff

Canonical repo: `Herbertofury/JetSetCraft`; target Forge 1.20.1 / Java 17 / mod version `0.3.0-alpha.1`.

The verified Style Flow checkpoint now has six ride styles (inline, quad, skateboard, BMX, hoverboard, scooter), twenty-four named contextual tricks, twenty-eight named dance moves across six families, no-gear dancing, automatic dance phrases, multiplayer cyphers, repeat penalties, graded landings, Flow ranks, dedicated hoverboard/scooter meshes, accessibility controls, optional Aether/Twilight data hooks, the existing Create/native rail stack, combat-safe animation composition, graffiti, and a complete twenty-five-page wiki source.

Real Forge proof for this checkpoint: six required GameTests passed; the dedicated server reached its ready state; runtime JAR `jetsetcraft-1.20.1-0.3.0-alpha.1.jar` is 3612216 bytes with SHA-256 `979ba60d68767c51df16fdcd41c7e73ac59323f118498fa01b927f7b2a8c679e`. Workflow run `32652330325` on branch `automation/street-gear-0.3-verify` produced the evidence.

Remaining acceptance is deliberately real-client/modpack validation rather than missing implementation: visual ride/dance/trick QA, Create 6.0.8 live tracks, TACZ live weapon composition, optional Aether/Twilight routes, and multiplayer soak. See `docs/STYLE_FLOW.md`, `wiki/Testing-and-Verification.md`, and `.agents-memory/RECOVERY_2026-08-21_STYLE_FLOW.json`.
