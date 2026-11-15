# Original Honkistas actor: grounded route candidate

This candidate adds a server-only transient route command to the original actor, not an automatic gang event. The real native navigation goal accepts 1–16 waypoints within 32 blocks, bounded speed and 1–1200 native ticks; it exposes running, completed, path-failure, timeout and cancelled states. Invalid or overlapping commands cannot replace a running route. No actor teleport is used after its initial spawn.

The native GameTest checks actual registered entity identity, entity-ticking terrain, grounded displacement across two waypoints, invalid requests, cancellation, genuine server-time timeout and blocked destination. The client audit appends a temporary reversible stone lane in the opt-in disposable RivalRushAudit world, then captures standing, actual displaced walking and route completion. It requires the real renderer, ground contact and client displacement; final state also requires server completion. Terrain, player position/gravity, camera/HUD and world time are restored, including cleanup on audit shutdown.

These tests are pending exact-head CI and image review when first published. The route has no skate equipment, skate controller, dance choreography, reactive cloth/hair, production provider, reward or event victory logic. It is not a substitute for control/defeat eligibility, invitations, actor ownership/reload, rival victory, Honkistas progression or rare guests. Models remain original prototypes below the owner's full visual benchmark.

No reference media or third-party assets are copied.
