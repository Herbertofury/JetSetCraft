# JetSetCraft Architecture

## Non-negotiable gameplay contracts

1. **Momentum is continuous.** Boosting, landing, grinding, wall riding and tricks may add/redirect/bleed momentum, but ordinary transitions must not flat-reset speed.
2. **Combat is composable.** A weapon action is not an implicit ride-state exit. Movement owns translation/lower-body presentation; weapon systems retain item input and upper-body animation authority.
3. **Server owns movement truth.** Inputs are sent to the server; authoritative ride state is synchronized to the local and tracking clients.
4. **World geometry is the playground.** Grinding understands both arbitrary collision-shape edges and authored rail networks. Vanilla/Forge rails use their real `RailShape`; Create tracks use Create's native track axes/normals and Bezier graph. Wall riding still uses real wall collision.
5. **Animation backend is replaceable.** Gameplay state does not depend on PlayerAnimator, GeckoLib, Epic Fight, TACZ, or YSM internals. Adapters consume state rather than owning it.

## State flow

`ClientEvents` -> `C2SInputPacket` -> `JetSetData` -> `JetSetMovement` -> player velocity/state -> `S2CStatePacket` -> `ClientRideState` -> camera/HUD/render/animation adapters.

## Combat layering

The authored `assets/jetsetcraft/player_animation/*.json` ride clips are validated to exclude `leftArm`, `rightArm`, `head`, `leftItem`, and `rightItem`. This preserves upper-body ownership for TACZ/Epic Fight/Better Combat/vanilla item animation. `tools/validate_assets.py` fails the checkpoint if a ride clip violates this contract.

## Movement modules

- `EdgeFinder`: searches nearby voxel collision boxes and scores top edges against velocity/distance.
- `VanillaRailFinder`: follows `BaseRailBlock#getRailDirection` so vanilla and Forge-compatible mod rails preserve straight, curved, and ascending geometry; datapack tags extend/blacklist unusual rails.
- `CreateRailProvider`: optional Create 6.0.8 adapter using `ITrackBlock` for local track axes and `TrackGraphBounds`/`BezierConnection` for exact long-curve sampling.
- `GrindFinder`: arbitrates rails/tracks versus incidental block edges and preserves the active rail kind through transfers/junction steering.
- `WallRideFinder`: detects adjacent wall surfaces and returns wall normal/tangent.
- `JetSetMovement`: acceleration, steering, air control, boost, grind continuation, rail junction steering, rail-hop transfers, rail tricks, wall rides, powerslides, tricks, manuals, combo state.
- `RideStyle`: data for inline, quad, board and BMX handling differences.

## Next rig stage

The high-detail static OBJ meshes are the current alpha visual baseline. The next renderer milestone is an articulated ride rig with named wheel/steering/deck bones so wheel spin, steering, suspension/compression, grinds, manuals and tricks can be animated independently while retaining the existing high-detail silhouette.
