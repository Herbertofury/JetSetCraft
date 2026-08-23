# Roadmap

JetSetCraft `0.2.0-alpha.1` established the runtime-proven Style Flow foundation: six ride styles, dedicated gear models, server-authoritative momentum, broad grinding, world physics, 24 ride tricks, 28 dance moves, cyphers, boost tricks, repeat/variety scoring, Flow ranks, landings, graffiti, combat layering, optional mod hooks, GameTests, server smoke, and a synchronized wiki.

## 0.3 premium-polish checkpoint

The `0.3.0-alpha.1` source line now adds same-entity physical Street Gear, species-aware mob contact rigs, premium animation timing, generated-mesh topology repair, hostile-data hardening, and a sixth Forge GameTest. Offline gates pass; Forge compilation, all six GameTests, dedicated-server smoke, and client acceptance remain mandatory before it is called a verified 0.3 release.

## Convergence priorities

The next work should deepen presentation and prove real-client acceptance without weakening the current architecture:

1. Compile/reobfuscate the 0.3 source, pass all six Forge GameTests and dedicated-server smoke, then run a real client visual pass for all 68 validated animation clips, mob/player equipment transforms, HUD scaling, first/third person, and multiplayer tracking.
2. Articulated equipment rigs: independent wheel spin, steering, deck/bike compression, pegs, hover field response, and grind contact.
3. Authored sound set and material-aware mix while retaining Minecraft-native fallback audio.
4. Expanded graffiti authoring, palette/customization, and rights-aware creator catalog.
5. Dedicated adapters for extreme player-model/skeleton mods where the generic PlayerAnimator contract is insufficient.
6. Real Create/TACZ/Aether/Twilight interoperability worlds in CI or recorded acceptance runs.
7. Physical Boombox street jams, cached installed-mod Mob Atlas discovery, data-driven gangs/reputation/music, then course/challenge tooling, timed lines, crew battles, replay/ghost support, and optional progression that remains friendly to existing worlds.
8. Later Minecraft/NeoForge/Fabric ports only after the 1.20.1 behavior remains verified and no feature is silently reduced.

## Non-negotiable guardrails

- Never flatten legitimate momentum just to simplify the solver.
- Never require optional adventure/combat mods for base startup.
- Never seize weapon arms with ordinary ride locomotion.
- Never replace real geometry with registry-name guesses when an API exists.
- Never ship unfinished equipment models or fake controls.
- Never call a release verified without a fresh JAR, GameTests, dedicated-server readiness, and exact artifact identity.
