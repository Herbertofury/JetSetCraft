# Original Honkistas actor: grounded route candidate

This candidate adds a server-only transient route command to the original actor, not an automatic gang event. The real native navigation goal accepts 1–16 waypoints within 32 blocks, bounded speed and 1–1200 native ticks; it exposes running, completed, path-failure, timeout and cancelled states. Invalid or overlapping commands cannot replace a running route. No actor teleport is used after its initial spawn.

The native GameTest checks actual registered entity identity, entity-ticking terrain, grounded displacement across two waypoints, invalid requests, cancellation, genuine server-time timeout and blocked destination. The client audit appends a temporary reversible stone lane in the opt-in disposable RivalRushAudit world, then captures standing, actual displaced walking and route completion. It requires the real renderer, ground contact and client displacement; final state also requires server completion. Terrain, player position/gravity, camera/HUD and world time are restored, including cleanup on audit shutdown.

These tests are pending exact-head CI and image review when first published. The route has no skate equipment, skate controller, dance choreography, reactive cloth/hair, production provider, reward or event victory logic. It is not a substitute for control/defeat eligibility, invitations, actor ownership/reload, rival victory, Honkistas progression or rare guests. Models remain original prototypes below the owner's full visual benchmark.

No reference media or third-party assets are copied.

## Retained first failures

Initial `7b18fdae` failed with zero displacement. Diagnostic-only `a34c856` (core run `43733131183`, job `132869785651`) observed rejected navigation with null path while `onGround=false` at entity tick 2, despite clear target and stone support. The test fixture now waits for actual native physics ground contact under its unchanged overall deadline before issuing its grounded route. Production navigation and all displacement/completion assertions remain unchanged. No teleport or manually set ground flag is used.

The grounded retry `39e26ff` moved the real actor 2.069 blocks, then exposed a controller mismatch: native navigation reported a reachable completed path while the actor remained 0.968 blocks from the requested endpoint. The controller now uses native collision-aware MoveControl for a bounded final 1.5-block approach, preserving the 0.45-block arrival requirement and real timeout. Non-reaching paths still fail; no position is forced.

Exact-head 13ee904 passed all 130 core native tests, but the client audit timed out before its first grounded frame. Diagnostics observed the real renderer and correct stationary camera, while both actor sides remained off-ground at floor height. The audit had disabled AI before native settlement. The fixture now leaves native AI/physics active from initial spawn; all ground, renderer and displacement gates remain intact. The earlier failure is retained in core run 43736953757, job 132881556197. This change awaits native capture verification, not visual acceptance.
