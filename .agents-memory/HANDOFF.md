# JetSetCraft Handoff

Canonical repo: `Herbertofury/JetSetCraft`; target Forge 1.20.1 / Java 17 / mod version `0.3.0-alpha.1`.

The verified runtime keeps the complete Style Flow foundation (six ride styles, 24 contextual tricks, 28 dance moves, hoverboard/scooter meshes, Create/native rail hooks, combat-safe animation composition, graffiti and the 25-page wiki) and adds the first production Gang Wars layer: a physical craftable Boombox, visible mob-head target slot, the approved stable vanilla gang atlas, reversible Street-Gear gangification, original-source EntityType event actors, species-aware gang gear, safe staggered Boombox sessions, no-cooldown restart, anti-farm ephemeral cast cleanup, and stable per-gang music slots.

Real Forge proof for this checkpoint: six required GameTests passed (with `street_gear` now covering physical Boombox Zombie Head → Dead Beat tuning and start/cancel/restart), the dedicated server reached its ready state, and runtime JAR `jetsetcraft-1.20.1-0.3.0-alpha.1.jar` is 3933492 bytes with SHA-256 `32dd39c640fe9f813a15fc0fb945d91f4fa14f8a84789e057b28df883a13a8fe`. Workflow run `32684207348` on branch `automation/head-gang-compat-0.3` produced the evidence.

Remaining acceptance is real-client/modpack validation and the broader design roadmap rather than this runtime being untested source: visual Boombox/head rendering and audio smoke, live optional head-mod packs, Create/TACZ/Aether/Twilight integration, multiplayer soak, and later Atlas/reputation/territory/chapter/minigame layers. See `docs/BOOMBOX_GANG_RUNTIME.md`, `docs/GANG_WARS_BOOMBOX_MOB_ATLAS_MASTER_SPEC.md`, and `wiki/Testing-and-Verification.md`.
