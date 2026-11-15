# Original Honkistas inline-control mechanics candidate

Isolated local candidate; no native compilation, execution, visual acceptance or publication claimed yet. No production encounter registration, skating animation, copied model, player-controller change or duplicate gear-render layer.

## Contract

`startSkateControl(input, heading, durationTicks)` accepts PUSH, COAST or BRAKE for 1–200 native ticks. It requires a live, grounded, dry, non-passenger original actor with AI enabled and actual physical INLINE gear from `MobStreetGear`. Invalid vectors, overlaps with an active walking route or a current lease are rejected. `setSkateInput` changes input without extending the lease. `cancelSkateControl` resets the synced input channel. Walking routes reject a simultaneous inline lease. All state is transient.

The exclusive movement goal suppresses normal pathfinding/strolling for the lease. The actor's travel method reads actual current velocity, applies bounded INLINE acceleration/cap/angular steering only for PUSH, adds nothing during COAST, and multiplies existing horizontal velocity by .72 for BRAKE. Y velocity is untouched. It calls vanilla travel exactly once with zero walking acceleration, retaining native collision, gravity and material friction. There is no cached momentum restoration, teleported position, forced grounding or no-gravity mode. Existing .027 push acceleration may produce modest speeds on stone; actual results must be measured rather than advertised as the .59 propulsion cap.

Support admission checks every cell under the conservative next-body sweep and requires resident chunks, sturdy floor and clear body/head collision. Checks are bounded even for abnormal external velocity. Lost contact, fluid state, missing gear or obstruction ends propulsion. **FAILED_CONTACT is not an emergency cliff stop**: existing velocity, gravity and external impulses remain under vanilla physics. Normal AI can resume afterward. Unsupported movement/steps/cliffs/rails/grinding/swimming are not supported skating behaviors.

The server synchronizes only input state for future client pose selection; a physical gear render is not evidence of skating animation or quality. The earlier finite upper-body beat cue remains independent.

## Proposed native evidence

`HonkistasSkateControlGameTests` uses disposable reversible elevated lanes and registered original actors, loaded-region tickets, actual grounding and no teleports after initial spawn.

- Paired stone actors push under equal conditions; measured equal native release velocities precede COAST versus BRAKE. An ice actor separately demonstrates normalized native retention rather than comparing unequal initial speeds as if equal.
- Printed evidence includes measured release speeds and actual coast/brake displacements.
- Bounds, concurrent walking rejection, gear prerequisite, cancellation, finite lease reset, real wall, removed-floor/native falling and independent beat cue are asserted.
- Terminal safety actors have AI disabled only after the result is observed, to exclude subsequent unrelated random stroll. Gravity and collision are not replaced.
- All actors, temporary terrain and tickets are cleaned up on success/fatal failure with bounded internal deadlines. Existing walking/gesture tests remain unchanged and must still pass full CI.

Before any skating claim: review exact-head native results, matched motion captures, lower-body push/glide/contact poses, physical skate alignment, material handling and braking readability. This candidate implements only the finite mechanics probe.
