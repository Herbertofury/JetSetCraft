# Roadmap

JetSetCraft `0.3.0-alpha.1` now has two runtime-proven pillars: the Style Flow movement/dance/trick foundation and the first production Gang Runtime. The current branch has passed Forge compilation, all six required real GameTests, and dedicated-server startup with the physical Boombox, source-owned gang actors, anti-farm cleanup, stable gang music slots, and mob-head targeting.

## Current 0.3 convergence

The current pass is closing the remaining architecture gap between the Gang Wars specification and the verified runtime:

1. **Data-driven gangs and targeting** — server/datapack overlays for gang presentation/source mappings plus exact item → mob/gang mappings, while stable built-ins remain fail-safe defaults.
2. **Real-client acceptance** — visual Boombox/head rendering, ride/dance/trick transforms, HUD/VFX/reduced-motion behavior, and gang audio smoke on an actual client.
3. **Live compatibility worlds** — Create 6.0.8 track geometry, TACZ aim/fire/reload composition, Aether/Twilight route mechanics, and popular Forge 1.20.1 mob-head packs.
4. **Multiplayer soak** — movement/cyphers/combat, Boombox concurrency, dimension transfer, persistence, disconnect/reconnect, and cleanup under more than one player.
5. **Mob Atlas + reputation** — cached discovery and durable stable-gang reputation/aliases/relationships built on the now-real Gang Registry, not parallel identity systems.
6. **Territory, hangouts and competition layer** — street-jam spaces, challenge/minigame rules, crew battles, timed lines, rewards and later chapters, all using the same bounded actor lifecycle.
7. **Presentation depth** — gang-colored Boombox/equalizer presentation, authored owner-approved music, stickers/graffiti, articulated ride equipment, and more species-aware entrance/victory choreography.
8. **Later loader/version ports** — NeoForge/Fabric/newer Minecraft only after the Forge 1.20.1 behavior remains reproducibly verified with no silent feature reduction.

## Non-negotiable guardrails

- Never flatten legitimate momentum just to simplify the solver.
- Never require optional adventure/combat/head mods for base startup.
- Never seize weapon arms with ordinary ride locomotion.
- Never replace source mobs just to make them gang members.
- Never guess ambiguous player-head identity from textures/display names.
- Never turn event casts into loot/XP/Street-Gear farms or permanent world clutter.
- Never ship unfinished equipment models, fake controls, malformed audio, or UI that implies unsupported behavior.
- Never call a checkpoint verified without a fresh JAR, required real Forge GameTests, dedicated-server readiness, and exact artifact identity.
