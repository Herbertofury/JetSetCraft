# Roadmap

JetSetCraft `0.2.0-alpha.1` establishes the complete Style Flow foundation: six ride styles, dedicated gear models, server-authoritative momentum, broad grinding, world physics, 24 ride tricks, 28 dance moves, cyphers, boost tricks, repeat/variety scoring, Flow ranks, landings, graffiti, combat layering, optional mod hooks, GameTests, server smoke, and a synchronized wiki.

## Major living-world expansion: Gang Wars, Boombox & Mob Atlas

The canonical specification is [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]]. This expansion turns JetSetCraft from a movement system living in Minecraft into a street culture that lives **with** Minecraft: ultra-rare natural gang encounters, a physical Boombox challenge hub, mob-head gang targeting, custom gang music slots, reversible Gang Skates on untouched vanilla/mod-owned mobs, dynamic reputation and membership, renameable Friendly crews, adult and Junior/Baby Atlases, installed-mod mob discovery, competitive gang AI, Turf Wars, trick battles, Tag, races, dance battles, spray combat, themed rewards, and cinematic arrivals.

The key compatibility rule is permanent: **never replace the source entity merely to make it a gang member.**

## Convergence priorities

The next work should deepen presentation and prove real-client acceptance without weakening the current architecture:

1. Real client visual pass for all 68 validated animation clips, equipment transforms, HUD scaling, first/third person, and multiplayer tracking.
2. Articulated equipment rigs: independent wheel spin, steering, deck/bike compression, pegs, hover field response, and grind contact.
3. Authored sound set and material-aware mix while retaining Minecraft-native fallback audio.
4. Expanded graffiti authoring, palette/customization, and rights-aware creator catalog.
5. Dedicated adapters for extreme player-model/skeleton mods where the generic PlayerAnimator contract is insufficient.
6. Real Create/TACZ/Aether/Twilight interoperability worlds in CI or recorded acceptance runs.
7. Build the GangDefinition/GangMemberAttachment/Boombox vertical slice, then one flagship gang end-to-end before scaling the Atlas.
8. Add reputation, membership, Crew Naming Rights, rare encounters, and the first reusable competition modes.
9. Add the Junior/Baby Atlas and runtime installed-mod Mob Atlas without introducing hard dependencies.
10. Expand curated mod/dimension adapters only after generic non-invasive compatibility is proven.
11. Later Minecraft/NeoForge/Fabric ports only after the 1.20.1 behavior remains verified and no feature is silently reduced.

## Non-negotiable guardrails

- Never flatten legitimate momentum just to simplify the solver.
- Never require optional adventure/combat mods for base startup.
- Never seize weapon arms with ordinary ride locomotion.
- Never replace vanilla or mod-owned mobs with JetSetCraft copies for gang compatibility.
- Never key durable gang data from a mutable display name; use stable `gang_id` values.
- Never replace real geometry with registry-name guesses when an API exists.
- Never ship unfinished equipment models or fake controls.
- Never call a release verified without a fresh JAR, GameTests, dedicated-server readiness, and exact artifact identity.
