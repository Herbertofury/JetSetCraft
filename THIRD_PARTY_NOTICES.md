# Third-party notices

JetSetCraft is built against Minecraft Forge and optionally integrates with PlayerAnimator, Create, TACZ, MCDivers, Epic
Fight, Better Combat, The Aether, Twilight Forest, and other Forge ecosystems. Those projects are not bundled in
the JetSetCraft release JAR and retain their own licenses and trademarks.

Owner-supplied authorization/reference archives are preserved outside Git runtime resources under
`source_assets/`. They are not relicensed by JetSetCraft. The exact evidence or source permission governs each
archive; the repository ledger does not broaden those rights.

Generated runtime models, animations, textures, and procedural audio are original JetSetCraft project content
unless [docs/ASSET_PROVENANCE.md](docs/ASSET_PROVENANCE.md) records a different source. Minecraft and related
names/assets are property of their respective owners. JetSetCraft is not affiliated with Mojang Studios,
Microsoft, SEGA, or the optional-mod authors.

## Liberty Grind optional providers

Liberty Grind uses separately installed original provider releases; the JetSetCraft JAR does not contain their
classes, weapon models, textures, sounds or gun-pack archives. Its mission, campaign, ship, paint-strike and
containment code is original JetSetCraft code. The [exact pack guide](docs/LIBERTY_GRIND_PACK.md) records
download pages, versions and verification hashes.

- [MCDiver / MCDivers](https://www.curseforge.com/minecraft/mc-mods/mcdiver), by shiroaki193: the pinned Forge
  1.20.1 release is 1.0.3, file 9307472. Its publisher lists the [MIT license](https://www.curseforge.com/minecraft/mc-mods/mcdiver/license).
- [Helldivers:Liberty Day](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/files/9341504),
  published by zeta1918 with Koei's merged Helldivers gun pack: the pinned file is 9341504. Its publisher lists
  [CC BY-NC-SA 4.0](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/license).
- TaCZ remains a separately installed dependency with its own notices. JetSetCraft's adapters call the live
  installed provider and enforce ownership around mission-only entities; they do not relicense upstream content.

Helldivers names and the original game's reference concepts remain associated with their respective owners.
Liberty Grind is an unofficial Minecraft adaptation, with original JetSetCraft objectives and presentation.

JetSetCraft incorporates and adapts selected MIT-licensed code and assets from the projects below. The original
copyright notices and licenses are preserved in `third_party/`.

## Street Art

- Project: [BeeIsYou/street-art](https://github.com/BeeIsYou/street-art)
- Imported revision: `63771c98742b76e2949223215aa2a60f1b6386e9`
- Copyright: 2026 BeeIsYou
- License: MIT (`third_party/street-art/LICENSE`)
- Adapted material: rollerblade mesh and textures, stride/drift/wallrun/wind-state movement concepts, paint-balloon
  item/projectile behavior, Fibonacci-sphere splash exposure, paint textures, and surface splatter behavior.

## Dream Burst Spirit Vector

- Project: [HamaIndustries/spirit-vector](https://github.com/HamaIndustries/spirit-vector)
- Imported revision: `623e7df026788add9737ea508b65baf80f66623d`
- Copyright: 2024 hama Industries
- License: MIT (`third_party/spirit-vector/LICENSE`)
- Adapted material: explicit movement-state boundaries, grounded kickoff, ledge-vault detection, wall-plane reuse
  protection, coyote-time and wall-parkour concepts.

Both projects target newer Fabric/Quilt or NeoForge environments than JetSetCraft's Forge 1.20.1 baseline. Their
useful behavior was therefore ported into JetSetCraft's server-authoritative controller rather than copied as an
incompatible second runtime.
