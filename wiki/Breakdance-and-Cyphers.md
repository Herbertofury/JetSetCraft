# Breakdance and Cyphers

Dance is a first-class server-owned activity, not a client-only emote. It works with no ride gear equipped, contributes to the same combo/Flow economy, synchronizes to tracking clients, and forms multiplayer cyphers.

## Six dance families and 28 moves

| Family | Move vocabulary |
| --- | --- |
| Toprock | Indian Step, Cross Step, Salsa Step, Kick Step |
| Popping | Fresno, Robot, Tutting, Body Wave |
| House | Jack, Shuffle, Loose Legs, Skate Step |
| Breaking | Six-Step, Backspin, Windmill, Flare, Swipe, Halo, Headspin, Airflare |
| Hip-Hop | Running Man, Bart Simpson, Criss Cross, Reebok |
| Locking | Lock, Scooby Doo, Uncle Sam, Skeeter Rabbit |

The catalog maps these names onto twenty-eight distinct full-body animation phrases. The server advances the phrase automatically and chooses the next move from family, direction, and chain state. Ground stunts use eight separate full-body clips.

## Starting and chaining

Press **B** on the ground. The first neutral press prioritizes Breaking; directional/modifier chords select another family. A phrase automatically chains when its duration completes. Press B again to immediately switch family or move.

During dance, horizontal movement is damped and ride momentum is paused. Equipped gear remains safely stored but is hidden so the player is visibly back on their feet. Leaving dance restores the ride without item loss or a mode conversion.

## Multiplayer cyphers

Nearby dancing players within the server-configured radius form a cypher. Cypher size is synchronized and shown on the HUD. Additional dancers improve periodic score, multiplier, Flow, and boost rewards, up to a bounded group size.

The detection is server authoritative and ignores spectators. It does not require a party mod, scoreboard team, or special block.

## Combat safety

Dance and full-body ground stunts are action animations. They are not allowed to seize weapon animation indefinitely:

- Starting item use or swinging cancels dance on the server.
- The client suppresses the full-body Style layer whenever a weapon overlay is detected.
- Ordinary ride locomotion stays on the separate lower-body layer, preserving arms/hands/head for TACZ, Epic Fight, Better Combat, bows, shields, spellbooks, and vanilla item use.
