# JetSetCraft Style Flow System

Style Flow is the scoring and expression layer shared by every ride style and by no-gear street dance. It is deliberately server-authoritative so a combo means the same thing in single-player, LAN, and dedicated multiplayer.

## Vocabulary

`TrickCatalog` exposes 24 stable action IDs:

- eight aerial slots;
- eight grind slots;
- eight low-speed ground-stunt slots.

Names vary by equipment so the same input language feels appropriate on inline skates, quad skates, skateboard, BMX, hoverboard, or scooter. Stable IDs keep networking, persistence, animation lookup, tests, and future translation independent from English display text.

`DanceCatalog` exposes 28 moves in six families:

- Toprock: Indian Step, Cross Step, Salsa Step, Kick Step;
- Popping: Fresno, Robot, Tutting, Body Wave;
- House: Jack, Shuffle, Loose Legs, Skate Step;
- Breaking: Six-Step, Backspin, Windmill, Flare, Swipe, Halo, Headspin, Airflare;
- Hip-Hop: Running Man, Bart Simpson, Criss Cross, Reebok;
- Locking: Lock, Scooby Doo, Uncle Sam, Skeeter Rabbit.

## Context selection

The server interprets `R` from the current state:

- grinding -> grind trick;
- airborne -> aerial trick;
- grounded at low speed, braking, manually balancing, or powersliding -> ground stunt;
- boosting + valid trick -> boost trick with increased cost and reward.

The dance input uses movement modifiers for immediate family selection. A first neutral press prioritizes Breaking; neutral presses while already dancing cycle families. A completed phrase automatically selects the next move, so a cypher can continue without input spam.

## Variety, repetition, and Flow

Each trick and dance move has a stable bit in a variety mask. New actions receive a freshness reward. Immediate repeats reduce score while still remaining valid inputs. The combo multiplier grows from expressive variety rather than a single optimal spam loop.

Flow is a separate 0-100 expression meter. Tricks, boost tricks, clean landings, completed dance phrases, and multiplayer cyphers add Flow. Inactivity drains it. The HUD translates the meter into six ranks: WARM UP, FRESH, HYPE, WILD, LEGEND, and ALL CITY.

## Landings

Landing grade is determined from airtime, vertical impact, active trick state, and the combo context:

- Perfect: strong control and timing;
- Clean: successful continuation;
- Sketchy: the line survives with weaker reward.

Landing feedback is synchronized and brief. It never overrides fall damage, Feather Falling, hazards, or Minecraft's vertical authority.

## Cyphers

When cyphers are enabled, the server searches only within the configured radius for other non-spectator players whose JetSetCraft capability reports an active dance. Nearby dancers increase crew size and add bounded score, Flow, multiplier, and boost-recovery bonuses. There is no client-trusted crew count.

## Safety and cancellation

Dance cancels immediately on movement, jump, boost, grind, trick, item use, weapon swing, water/lava contact, loss of ground, passenger state, or elytra flight. `Shift+B` is an explicit cancel. Starting dance safely settles horizontal momentum rather than teleporting or moving the player. Full-body animation yields when weapon presentation needs ownership.


## Animation identity

Each of the 28 named dance moves owns a stable `dance_<id>` clip rather than sharing a small generic pool. The generated keyframes use distinct family language: upright cross-steps for Toprock, hard angular hits for Popping, jack/shuffle footwork for House, floor-power arcs for Breaking, groove/stride phrases for Hip-Hop, and sharp points/locks for Locking. All UUIDs are deterministic so rebuilds do not churn resource identity.
