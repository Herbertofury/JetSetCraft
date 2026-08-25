# Controls

All JetSetCraft keys are standard Forge key mappings and can be rebound from Minecraft's Controls menu.

| Default input | Action |
| --- | --- |
| Right-click ride gear | Equip that item into the dedicated ride slot and activate it |
| K | Toggle the equipped ride on or off |
| Shift + K | Unequip and return ride gear to inventory |
| H | Instantly show or hide the compact Boost/Flow HUD |
| Left Alt | Boost |
| R | Contextual trick: air, grind, or grounded stunt |
| Left Alt + R | Boost trick when enabled and boost is available |
| G | Seek/grab a grind target; in the air it can begin a wall ride |
| Space while grinding | Rail hop or transfer while preserving momentum |
| Space while wall-riding | Kick away from the wall; up to three kicks reset on ground contact |
| Sprint + Space + direction on ground | Intentional parkour kickoff |
| Space + direction beside a low ledge in air | Collision-checked ledge vault |
| C | Manual; also a Hip-Hop selector when starting dance |
| V | Brake/powerslide; also a Breaking selector when starting dance |
| B | Begin dance, switch dance family, or chain a new move |
| Shift + B | Stop dancing |
| Use spray can in air | Open the paged graffiti selector |
| Shift + use spray can on a block | Open the selector without painting |
| Use spray can on a block | Place the selected Tag, or spray a compact Free Paint splat on any face |
| Use paint balloon | Throw a dye-colored 1,000-ray bounded surface splash |

## Dance selection chords

The first neutral **B** press prioritizes Breaking. Direction and action modifiers select the other families without opening a menu:

| Chord | Dance family |
| --- | --- |
| B | Breaking first, then cycles families on later neutral presses |
| W + B | Toprock |
| S + B | House |
| D + B | Popping |
| A + B | Locking |
| C + B | Hip-Hop |
| V + B | Breaking |

Dance moves automatically chain when their animation phrase ends. Tap **B** again to immediately move into another phrase. Walking away, jumping, boosting, grinding, tricking, using an item, swinging a weapon, entering water/lava, or leaving the ground ends the dance cleanly.

## Movement authority

Ride acceleration follows actual directional input. Releasing movement keys allows Minecraft's ordinary ground travel and friction to settle the rider to a complete stop; stored momentum never invents camera-forward motion. Pressing Boost at a true standstill does not launch the player without a direction.

Entering water, lava, or the swimming pose immediately gives the full velocity vector and animation pose back to Minecraft. Swimming, diving, currents, bubble columns, and compatible modded fluid behavior remain available while ride gear stays safely equipped.

Ground acceleration is delivered as responsive skate-push pulses. Powerslides steer angularly and can earn a small release kick from a committed turn. Wall kicks enter a brief wind state that permits controlled airborne redirection without replacing Minecraft gravity or outside impulses.

## Context rules

**Air trick:** press R after at least a short airborne window. The server selects a move from the current ride style's vocabulary using direction, combo state, and variety history.

**Grind trick:** press R while attached to a rail/edge. The rider remains on the path and continues the combo.

**Ground stunt:** press R at low speed or while braking, manualing, or powersliding. The ride gear temporarily disappears into the loadout while the full-body breakdance move plays, then returns automatically.

**Perfect landing:** land a sufficiently long air sequence with low impact. Holding Manual during a controlled touchdown can protect a clean grade. Hard impacts still land but receive a lower grade.
