# Original Honkistas beat-cue candidate

Draft candidate stacked on reviewed source commit `89c8a13`. At `037432f`, the native core and actual-client audit passed; the cue frame was inspected. The full provider matrix was not green. This is bounded walking-plus-cue evidence, not skating or production-event acceptance.

## Scope

The original theatrical courier can receive `startBeatCue()` on the server. One native integer entity-data field carries a finite 48-tick countdown to tracking clients. Overlapping requests are rejected without extending duration; `cancelBeatCue()` clears it; countdown continues with AI disabled. It is transient, not saved as lasting choreography. No target, damage, attack rewards or production event registration is added.

The model reads only synced state. Its bounded lift/pulse/return affects the right arm and forearm, blending over existing arm gait. Lower-body gait, navigation, velocity, coat-tail joints and entity identity remain unchanged. This is not skating, dancing, cloth physics or a cinematic-quality claim.

## Evidence gates

- New GameTest retains actual world registry, grounded movement, route completion and failure assertions. It adds overlap rejection, exact native countdown, movement during the cue, cancellation/restart and AI-disabled countdown.
- Fixture metadata is round-tripped through the vanilla entity-data packet codec into an unspawned receiver, including reset updates. This is codec coverage, **not evidence of a real client connection**.
- Native visual audit preserves captures 27–29. A new return route triggers the cue on the server, then capture 30 requires the actual client actor's synchronized active countdown, more than 0.5 block of observed return movement, native ground contact and renderer acknowledgement. It also waits for real route completion and client countdown reset before cleanup. No client-side cue/progress assignment is made.
- Workflow capture inventory includes `30-honkistas-synced-beat-cue` ; exact-head full CI plus pixels remain required before acceptance claims.

## Skating reuse investigation (read-only)

`MobStreetGear` already persists physical gear, resolves rig compatibility and synchronizes `S2CMobGearPacket` to `ClientMobGearState`. `MobRideGearLayer` renders conservative ground-contact item positions (including BIPED) without replacing source actors. These are reusable equipment/rendering pieces, not NPC skating locomotion. `RideAnimationController` is player-specific (`AbstractClientPlayer`, player-animation factories); `VanillaWorldPhysics` and associated movement physics bridge player state/capabilities and cannot simply be attached to this PathfinderMob. A native NPC skating controller needs explicit momentum/braking/steering and contact/obstacle tests plus a matching lower-body glide/push pose. This candidate deliberately retains walking.

A separate home-defense test diagnostic splits its existing compound assertion into cancellation, accepted result, objective credit and owned decal checks. It preserves all original conditions and adds held-item, phase, reach and sturdy-support context; no claimed behavior repair.

## Retained return-path failure

Initial 999edad and diagnostic 7b2e33 passed all 131 native tests and ten provider checks, but real-client return completion failed. Diagnostic core run 43742800048 records a reachable completed six-node native path 1.69 blocks before the requested endpoint, outside the bounded 1.5-block final approach. The controller now allows at most three real re-path requests per waypoint without changing the .45-block arrival threshold or original deadline. A slow reverse return is added to native cue coverage. Failed visual evidence remains available; capture existence alone is not full acceptance.

Subsequent diagnostics retained reachable consumed paths outside the original final-approach window even after bounded replans. The candidate permits at most a two-block native MoveControl approach only after loaded swept-body collision and floor-footprint validation. Completion remains within .45 horizontal/.75 vertical distance with actual ground contact and original deadline. Native tests now exercise supported clearance, missing floor, head obstruction and excessive approach distance. This is conservative flat-ground admission, not step/cliff handling or skating.

At c7f9482, all 131 native core tests and the full actual-client cue/return/reset audit passed. Actual frame30 was inspected and the raised arm is visible. Full-provider diagnostics isolated an independent home test race: accepted turf paint occurred before production lazy site grounding (100-tick retry). The fixture now waits boundedly for actual grounded/active state and recomputes its observation position; it does not set credit or terrain readiness, and preserves the 350-tick overall deadline and all preservation assertions. Independent source review confirmed the timing race. Exact-head CI remains required after this fixture correction.

## Provider-terrain fixture isolation

At `037432f`, adventure-all job 132916608120 observed the graffiti support replaced with natural gravel after the fixture removed its stone block (`entityTicks=101`, `removedSupportAt=40`). The lifecycle fixture now places its single support 16 blocks above the highest measured neighboring surface, verifying air before use. Native falling physics, the actual support-removal operation, real entity-tick checks, 40-tick removal bound and 100-tick test deadline remain unchanged. Exact-head CI is required. The same head's Alex's Caves build failed on Maven Central HTTP 429, not a Java assertion; no dependency versions or retry limits were changed.

The isolated fixture uses a single bounded native callback with explicit waiting states and idempotent failure cleanup. It restores only its still-owned stone and releases its ticket; it does not erase third-party replacement blocks. Success checks native `DISCARDED` before cleanup can discard any fixture entity.
