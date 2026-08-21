# Troubleshooting

## The game rejects my connection

JetSetCraft's network protocol is versioned. Confirm the client and server use the same JAR. Delete neither world data nor configs to work around a version mismatch; install matching builds.

## Ride gear will not activate

- Confirm the item is a JetSetCraft ride item.
- Right-click it to equip into the dedicated slot.
- Press K to toggle.
- Run `/jetsetcraft status` and check `equipped`, `ride`, and `active`.
- Shift+K safely returns the item if the loadout is confused.

## I cannot start dance

Dance requires ground contact and stops during water/lava, passenger state, elytra flight, item use, or weapon swing. Release movement/action keys, stand still, and press B. Shift+B intentionally stops it. A server may disable dance.

## Ground stunts do not trigger

Ground stunts require R while grounded and either low momentum or a braking/manual/powerslide context. At full speed with no modifier, use an air or grind trick instead. Check `allowGroundStunts` on the server.

## Grinding attaches to the wrong thing

- Aim movement toward the intended path.
- Reduce nearby accidental candidates with `no_grind` tags.
- Mark authored surfaces with `grindable`.
- Confirm hazards and internal seams are not being forced by a broad datapack tag.
- For Create, confirm the tested Create 6.0.8 line is installed.

## Camera motion is uncomfortable

Set client `reducedMotion=true`. This disables JetSetCraft camera roll, speed/boost FOV pulses, hover bob, and rapid ride-gear stunt rotations. HUD and gameplay remain available.

## The asset generator cannot download graffiti

That is supported. The generator deletes partial downloads and creates four original offline tags. The extended owner-approved archive is used only after exact SHA-256 verification.

## A combat animation conflicts

Confirm the combat mod version, reproduce with a real weapon action, and check whether the overlay is detected. Ride clips are lower-body only; full-body dance/stunts should disappear during weapon activity. Extreme custom skeleton replacements may require a dedicated adapter.
