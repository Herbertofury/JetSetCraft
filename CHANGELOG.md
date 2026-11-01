# Changelog

## 0.4.0-alpha.3 — activities and arena circuits, 2026-11-01

- Added one activity catalogue for Random, Tag, King of the Hill, Showdown and Rival Rush. Empty-hand Boombox interaction opens the chooser; random invitations are reviewed before acceptance.
- Implemented actual owner-scoped street objectives, bounded event casts and cleanup. Unloads, removed anchors, bystander attacks and abandoned games cannot create completion rewards.
- Connected completed activities, Rush arenas, settled Rush runs and house dispatch jobs to the same Crew Supplies, crew XP, member training and gang-reputation account. Earnings before founding a house are retained and transferred once.
- Added a six-arena Rush circuit with three reward-led route choices, physical skating connections, visible route progress, a Return/Encore ending and preserved builds across circuit sets. Free streets retain their thirty-minute or Endless options.
- Added six vanilla arena recipes plus an optional Aether palette recipe. Data packs can set room radius, layout, authored block positions, objective duration, defeat target and bounded reward emphasis.
- Added charged reward caches for magnet, damage, critical chance/power, ability area, recovery and artifacts. Cache claims require genuine owned rival defeats; victory gathers nearby owned cards.
- Added critical-hit calculations, area scaling, cooldown upgrades and three artifact slots: Arc paint, Firework cap, Bass horn, Rail shockwave, Tempo drive, Soul mixtape and an optional native Iron's Firebolt relay.
- Extended provider CI to Aether, Iron's Spells, AoA and a combined adventure configuration alongside the existing Wave 2 configurations. Added real objective, circuit, provider-collision and dimension-return tests; the new client audit exercises the actual Boombox menu and first route choice.
- Merged through [PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41). [Base verification #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215) and [all eight provider configurations #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302) passed all 28 real GameTests and server smoke. The base client also produced ten inspected native captures. The [alpha.3 receipt and gallery](docs/verification/arena-circuit/README.md) records the identical tested/merged tree, exact JAR checksum, native provider scope and observed upstream warnings. Human pacing and long-session progression balance remain to be evaluated.

- Follow-up [PR #42](https://github.com/Herbertofury/JetSetCraft/pull/42) stabilizes only the disposable client-audit house-to-activity handoff and publishes the final player guides. [Base #282](https://github.com/Herbertofury/JetSetCraft/actions/runs/41357798877) passed all 28 GameTests, server smoke and ten client captures. The distributed JAR remains the exact nine-configuration PR #41 artifact; production gameplay sources and resources did not change.

## 0.4.0-alpha.2 — crew-house development, 2026-11-01

- Added a reusable **Crew Charter** and separate Home, Crew, Jobs and Facilities ledger for a house the player builds around an existing Boombox.
- Added persistent crew/member identities, two starter apprentices, named roles, member training, per-gang reputation, bounded reputation recruitment discounts and a virtual Crew Supplies wallet. Roster members are mission profiles in this slice; visible residents and followers remain later work.
- Added four manually managed plot tiers with radii 8/12/16/24 and member capacities 3/5/8/12. The footprint governs facilities and capacity, preserves placed blocks and checks overlap before charging; it is not a general land-protection system.
- Added courier, tagging, salvage and DJ soundcheck dispatch missions with frozen rewards, saved server-game-time deadlines, bounded concurrency and once-only claims. Ready missions do not expire or auto-repeat.
- Added physical workshop, practice-yard, mix-studio and recovery-corner facilities. Registration and upgrades inspect actual placed blocks. One selected, working facility can grant one starting Rush rank when entering from the home.
- Connected genuine Rush participation to crew XP, idle-member training, rival-gang reputation and bounded supplies while preserving normal inventory/XP return. Optional progression failures cannot block the return path.
- Added contextual client soundtrack support, track credits, fade-in/out, local on/off/next/status controls, two exact recording metadata entries and a local Ogg resource-pack importer. Sonic audio masters are not bundled yet.
- Passed the nineteen-GameTest acceptance gate, dedicated-server smoke and an eight-screen real-client audit in [Verify JetSetCraft #274](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751); [all four optional-provider configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) passed the same nineteen tests and server smoke. The [alpha.2 verification receipt](docs/verification/crew-house/README.md) records the tested source and binary separately from [historical alpha.1 evidence](docs/verification/rival-rush/README.md). These checks establish machine integration, not human pacing or progression balance.

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
