# World Physics

JetSetCraft treats Minecraft mechanics as movement vocabulary. The server samples the real block, friction, fluid, effects, enchantments, impulses, rail state, and collision geometry instead of replacing the world with a fixed vehicle controller.

## Surface language

| World mechanic | JetSetCraft behavior |
| --- | --- |
| Ice | Higher coasting and speed |
| Packed ice | Stronger route speed |
| Blue ice | Extreme 2.15× default top-speed profile, intentionally evoking boats on blue ice |
| Frosted ice | Frost Walker-created routes remain meaningful |
| Slime | Preserves vertical impact into a configurable bounce |
| Honey | Drag, braking, and airborne side-contact behavior |
| Soul sand / soul soil | Slow by default; Soul Speed on skate footwear converts it into a route |
| Snow layers | Drag scales with snow depth |
| Cobweb / powder snow | Real movement traps instead of ignored decoration |
| Magma / cactus / campfire | Hazard-aware edge rejection and normal damage semantics |
| Water | JetSetCraft yields the complete movement/pose path to vanilla swimming, currents, bubble columns, equipped boots, and status effects |
| Lava | JetSetCraft yields movement to Minecraft's native lava behavior rather than substituting a ground controller |

## External impulses

JetSetCraft compares the previous solver-written velocity with the next server-tick velocity. Real external changes—explosions, pistons, knockback, moving contraptions, currents, and other mods—are captured into a short impulse-preservation window. Steering is temporarily reduced rather than allowing the ride solver to erase the impulse.

## Micro-terrain continuity

Collision-verified rises up to the configured step height can preserve a ride across slabs, stairs, snow, and similar small obstacles. Full blocks remain real obstacles. The system checks clearance and cooldowns; it does not enable generic step-height clipping.

## Datapack hooks

Modpack authors can extend:

- `jetsetcraft:boost_surfaces`
- `jetsetcraft:low_friction_surfaces`
- `jetsetcraft:bounce_surfaces`
- `jetsetcraft:sticky_surfaces`
- `jetsetcraft:brake_surfaces`
- `jetsetcraft:hazard_surfaces`
- `jetsetcraft:fluid_skimmable`
- `jetsetcraft:grindable`
- `jetsetcraft:wallrideable`
- `jetsetcraft:no_grind`

See [[Modpack Maker Guide|Modpack-Maker-Guide]] for optional-entry syntax.


## Owner requirement: selectable movement experiences

Players must be able to choose excellent vanilla-compatible Minecraft movement or the full Jet Set Radio / Bomb Rush Cyberfunk-inspired movement experience. This is a new design and acceptance requirement; the current mechanics above do not establish that both selectable profiles are already implemented or verified.

### Vanilla-compatible profile

Retain ordinary Minecraft walking, sprinting, jumping, sneaking, swimming, climbing, collision, knockback, equipment and other mods’ movement behavior. JetSetCraft must not leave its skating controller, camera, input interception, pose, friction or velocity overrides active when this profile is selected. Preserve crafting, building, combat and normal survival controls. Verify parity against the same modpack with JetSetCraft movement inactive; do not promise conflict-free behavior without testing.

### Expressive skating profile

Develop responsive, momentum-rich movement inspired by the reference games: acceleration, braking, carving, turning, air control, jump arcs, grinding, rail transfers, landings and trick chaining. Research which moves the games actually support rather than attributing unverified abilities to them. Keep Minecraft collision, external impulses, server authority and compatibility constraints explicit. Movement quality and fun require repeated native playtests, not merely matching numerical top speed.

### Choice, transitions and event fairness

- Provide a clear, persistent per-player setting and understandable activation/deactivation controls. Separate optional camera/assist settings from the movement profile. Exact defaults and bindings remain implementation decisions.
- Switching profiles must cleanly remove only JetSetCraft-owned state; preserve equipment, effects and outside impulses. Define safe handling while airborne, grinding, swimming, mounted, dead or changing dimensions. Do not erase momentum or launch a player accidentally during a switch.
- Course manifests declare required movement capabilities. Offer compatible routes/modes for vanilla-profile players; never silently force the skating controller to enter an event. If a challenge requires skating, disclose that before opt-in.
- Competitive events must declare allowed profiles/loadouts and switching rules to prevent speed or cooldown exploits while preserving a clear exit back to normal play.
- Test coexistence with TaCZ aiming/reload/recoil, controller/key bindings, camera mods, alternate movement mods, mounts, fluids and multiplayer corrections. Inactive integrations should have negligible steady-state cost and no unnecessary startup work.

## Development handoff: installed reference-game study

The owner requests that a future Codex or Claude development handoff include thorough study of their installed reference games, especially Bomb Rush Cyberfunk and Jet Set Radio, to understand and refine JetSetCraft movement and fun. Also study relevant runner/platformer and objective-mode references when installed and accessible. This is a handoff requirement; no new executor was launched and no installed-game inspection is claimed by this documentation update.

### Research procedure and deliverables

1. Inventory the actually available game installations, versions, DLC/mods, tools and permitted access. Preserve saves, settings and installations; work from read-only inspection or separate research copies where appropriate. Do not assume a game is installed merely because it was named.
2. Play and capture repeatable movement experiments: starts/stops, turn radii, reversal, slopes, jump timing and arcs, air steering, rail entry/exit, transfer forgiveness, camera behavior, landing recovery, trick cancellation and input buffering. Separate measured observations from estimates or hypotheses.
3. Inspect accessible configuration, data, models, animations and code where authorized and technically available to explain observations. Do not bypass DRM/access controls or assume third-party code/assets may be redistributed. Keep the resulting implementation original and document any dependency/license requirements.
4. Produce a reference comparison with exact game/version, test conditions, measurements/captures, inferred principles and their proposed Minecraft adaptations. Identify what creates flow, control, challenge and recovery rather than copying superficial animations.
5. Build a tunable movement test course, implement isolated changes, and compare side-by-side captures plus user playtests. Cover low/high frame rates, network latency, terrain, controller input and TaCZ compatibility. Preserve the vanilla-compatible profile throughout.
6. Deliver exact-build tests, startup/frame/tick/memory comparisons, known limitations and unresolved choices. Treat movement perfection as an iterative quality goal, never a claim based on static analysis alone.

### Acceptance backlog

- [ ] Inspect current controller and optional integrations before choosing profile architecture.
- [ ] Establish vanilla-profile baseline parity and prove full cleanup on profile transitions.
- [ ] Build the reference-game evidence dossier and original movement specification.
- [ ] Validate expressive skating feel and control with repeated native playtests.
- [ ] Test course capability gating, multiplayer fairness, persistence and TaCZ/non-TaCZ configurations.
- [ ] Improve performance and startup alongside the work without feature loss, arbitrary content caps or regressions.
