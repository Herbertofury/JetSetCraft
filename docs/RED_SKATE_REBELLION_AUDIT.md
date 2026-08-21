# Red Skate Rebellion 0.0.2 — JetSetCraft Integration Audit

## Reference artifact

- File: `redskaterebellion-0.0.2.jar`
- SHA-256: `06ea09db5abbe82e9a6be3d7b8fa87946bf4a444a7cc8ca50890dfab6bd37d01`
- Target declared by JAR: Forge 47+, Minecraft 1.20.1–1.20.x

This is a user-supplied failed/prototype mod and is allowed to be mined for any implementation, architecture, mechanic or asset that is genuinely useful to JetSetCraft. It is **not** a requirement to preserve its limitations or quality ceiling.

## What the JAR actually contains

The useful implementation is mostly a dedicated player gear system rather than skating physics:

- player gear capability/provider and NBT persistence,
- `PlayerGearInventory` with dedicated gear slots,
- custom gear menu/screen and server-open packet,
- backpack and elbow-gear slot concepts,
- wearable/placeable skater backpack concept,
- ice-skate armor/model prototype.

It does **not** contain a mature skating momentum/trick/grind engine to replace JetSetCraft's own system.

## Adopt / adapt

### Dedicated loadout architecture

Useful principle: JetSetCraft ride equipment belongs in a dedicated loadout/equipment layer instead of requiring the player's hands. This fits JetSetCraft particularly well because TACZ guns, Epic Fight/Better Combat weapons, bows, spellbooks and ordinary items must remain usable while skating.

JetSetCraft should adapt the principle with its own production implementation rather than copy the prototype's exact slot/UI code. The ride state remains JetSetCraft-owned and server authoritative.

### Backpack concept

A skater backpack that can be worn and optionally placed in-world is thematically strong and useful. It is accepted as a future JetSetCraft content feature, provided it receives a production-quality model, texture, inventory behavior and animation attachment. The broken prototype model is not a shipping asset.

### Protective gear slots

Elbow/knee/helmet-style optional gear can become a clean future extension surface for cosmetic/protection equipment without stealing hand or boot armor slots. It must not be used as stat-bloat or a prerequisite for basic movement.

## Reject / replace

- Do not adopt any premise that JetSetCraft should avoid its own movement system. JetSetCraft's momentum/trick/grind/combat architecture remains the core.
- Do not ship the prototype ice-skate OBJ as a final model. The inspected standalone OBJ is only about 356 vertices and the JAR contains broken/missing model/texture references.
- Do not replace JetSetCraft's current high-detail skates, BMX, board or animation pipeline with Red Skate placeholder assets.
- Do not add a dead gear UI until its slots perform real JetSetCraft functions.

## Integration northpoint

**Use Red Skate in full wherever it is genuinely useful, while blending the good pieces into JetSetCraft instead of inheriting prototype constraints.**
