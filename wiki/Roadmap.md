# Roadmap

JetSetCraft `0.3.0` is the completed Forge 1.20.1 release surface documented in the README and changelog. Everything below is possible post-v0.3 direction, not a promise that the feature already exists.

## Possible later work

1. Expand live modpack compatibility coverage across more creature models, head providers, dimensions, combat systems, and rail ecosystems.
2. Add bounded territory, reputation, aliases, relationships, and adopted hangouts on top of stable `gang_id` data.
3. Add complete scored challenge modes—Turf War, timed lines, races, dance battles, graffiti contests, and Tag—using the existing bounded actor lifecycle.
4. Extend presentation with articulated equipment parts, more anatomy-specific choreography, richer Boombox visuals, and opt-in full-length resource-pack music.
5. Consider newer Minecraft versions or additional loaders only after behavior and evidence can be carried forward without feature reduction.

## Permanent guardrails

- Preserve legitimate momentum and ordinary Minecraft mechanics.
- Keep optional adventure, combat, head, model, and rail mods non-required.
- Keep ordinary ride animation off weapon arms/hands/head.
- Never replace source mobs to create gang members.
- Never guess ambiguous player-head identity from texture or display-name heuristics.
- Keep event casts bounded, chunk-safe, temporary, and ineligible for loot/XP/Street-Gear farming.
- Require a fresh JAR, validators, real Forge GameTests, dedicated-server readiness, and exact artifact identity for every release claim.
