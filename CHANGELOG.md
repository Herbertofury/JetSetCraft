# Changelog

## 0.4.0-alpha.1 — development, 2026-11-01

- Added **Rival Rush**, an optional skating roguelite mode with a reusable flyer, crew selection, thirty-minute runs, voluntary Endless continuation, and an explicit return to Minecraft.
- Added a separate generated street dimension, temporary ride/paint equipment, Rush XP, mode vitality, score, and server-validated upgrade choices.
- Added collectible mob cards keyed by creature and crew, seven mastery thresholds, increasing duplicate rewards, and album milestones that unlock starting drafts only inside Rush.
- Added nine themed crew rosters, skater/tagger/bruiser/captain roles, delayed positional attack warnings, bounded encounter pressure, and rotating five-minute mixes.
- Connected actual painting, moving tricks/grinds/wall rides, and district Boombox dance stops to mode rewards. Music stops use the existing crew stingers.
- Added a saved return journal with acknowledged player saves, inventory/XP/ride restoration, ownership checks, and recovery paths for interrupted sessions.
- Fixed shared ride-capability invalidation across dimension transitions while preserving invalidation of stale handles.
- Added a paginated album, readable mode menus, independently adjustable Rush HUD, and a server enable/disable setting.
- Expanded the development gate to fifteen real Forge GameTests, normal dedicated-server shutdown verification, and a graphical-client audit of entry, four screens, and return with a nonempty inventory and vanilla XP.
- Added an eighteen-game research/design study that separates the implemented alpha from proposed build families, authored districts, musical challenges, and bespoke bosses. This development build does not replace the published v0.3.0 release; human pacing, long Endless sessions, and broader mob compatibility still need playtesting.

## 0.3.0 — 2026-08-24

- Added six complete ride styles, twenty-four tricks, twenty-eight dance moves, Flow/combo scoring, landing grades, and multiplayer cyphers.
- Added world-native movement composition for block surfaces, enchantments, rails/redstone, external impulses, arbitrary exposed edges, and optional Create 6.0.8 track geometry; active gear now yields completely to vanilla swimming and fluid motion.
- Added persistent server-authoritative player loadouts and same-entity Street Gear for compatible vanilla/modded mobs.
- Added data-driven mob-head gang resolution, physical Boombox sessions, event-only cast cleanup/anti-farm handling, and eighty original generated entrance stingers.
- Added a paged 139-entry graffiti selector, bounded 16 × 10 custom-paint editor, repainting, wall-support cleanup, and per-chunk limits.
- Merged Street Art's useful movement, leg-baked rollerblade model, 16-color paint-balloon assets/projectile loop, Fibonacci-sphere surface exposure, and free-paint ideas into the Forge 1.20.1 runtime under its MIT license.
- Merged Spirit Vector's useful grounded kickoff, collision-checked ledge-vault, wall-plane reuse, coyote-time, and explicit parkour-state ideas under its MIT license.
- Added a persistent Tag / Free Paint selector toggle, full 16-color palette, removable six-face splat decals, and dispenser-compatible paint balloons without removing the 139 tags or custom editor.
- Added rhythmic skate-stride acceleration, angular drift charge/release, three grounded-reset wall kicks, and 20-tick wind redirection while preserving neutral stop and full vanilla fluid control.
- Replaced the oversized opaque style panel with a compact vanilla-adjacent Boost/Flow display and instant `H` toggle.
- Corrected ride-model transforms and added hands-free BMX/scooter handlebar animation layers that disengage at rest or during item/weapon use.
- Removed camera-forward relaunch from stale momentum, restored a true stationary state, and made boost require a real direction or existing motion.
- Hardened client/server packet boundaries, hostile input/NBT sanitization, lost-input recovery, block-removal lifecycle, optional-mob spawn failures, and fake-player networking.
- Reconciled the valuable Git branch history and added a Gradle wrapper, read-only CI, nine real Forge GameTests, clean builds, dedicated-server verification, and an unattended real-client visual audit.

## 0.2.0-alpha.1

- Published the first Style Flow alpha and Forge 1.20.1 runtime evidence.

## 0.1.x

- Established the JetSetCraft movement, equipment, model, and repository foundations.
