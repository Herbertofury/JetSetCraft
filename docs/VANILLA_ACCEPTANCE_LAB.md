# JetSetCraft Vanilla Physics Acceptance Lab

JetSetCraft's Minecraft-native movement rules are gameplay contracts, not passive design notes. The built-in server commands below exist so those contracts can be exercised in a real Forge world with the same authority, collision, redstone, enchantment and networking path used by normal play.

## Commands

- `/jetsetcraft status` — authoritative server snapshot of ride/loadout state, momentum, velocity, current Minecraft surface, effective movement enchantments, grind state, and current grind material profile.
- `/jetsetcraft set_momentum <speed>` — operator-only test aid for entering a section at a repeatable speed without changing the real movement solver.
- `/jetsetcraft build_vanilla_lab` — operator-only builder for a compact south-facing acceptance course near the player.

## Generated lanes

### Surface lane

Ordinary stone -> Ice -> Packed Ice -> Blue Ice -> Honey -> Soul Sand -> Mud -> Snow -> Slime -> Cobweb -> Powder Snow.

Acceptance expectations:

- Ice tiers produce a visible progression in cap, retention and steering demand.
- Blue Ice remains the deliberately extreme boat-on-ice fantasy; it is not normalized back to ground speed.
- Honey is an unmistakable brake/drag surface.
- Soul Sand is a slow route unless compatible Soul Speed on vanilla footwear or JetSetCraft skate footwear changes it.
- Mud, snow, cobweb and powder snow remain meaningful terrain instead of being erased by the momentum controller.
- Slime preserves horizontal momentum into a bounce and can continue a clean combo.

### Rail / redstone lane

Normal rail -> powered powered rail -> unpowered powered rail -> detector rail -> powered activator rail -> normal rail -> rising rail.

Acceptance expectations:

- Powered rail adds momentum while grinding/crossing it.
- The same rail without power is a real braking segment.
- Detector rail can drive adjacent vanilla redstone feedback from a JetSetCraft rider.
- Activator rail produces the configured ride action/pop rather than being treated as another generic boost.
- Slopes stay part of one grind path and preserve meaningful momentum.

### Fluid lane

Shallow source water plus Soul Sand and Magma source-water columns.

Acceptance expectations:

- Shallow water applies progressive drag instead of an invisible wall/reset.
- Vanilla water motion remains relevant.
- Soul Sand/Magma bubble behavior is allowed to contribute vertical impulse rather than being canceled by JetSetCraft.
- Depth Strider and Dolphin's Grace compose with the ride model instead of being double-applied.

### Impulse / hazard lane

Slime -> Honey -> Magma -> Soul Campfire -> Cactus.

Acceptance expectations:

- Slime and Honey remain mechanically distinct.
- Vanilla hazards remain dangerous; tricks do not grant hidden immunity.
- External impulses from vanilla mechanics are folded back into JetSetCraft momentum rather than flattened to the normal cruise cap.

## Dedicated skate enchantments

Inline and quad skates can participate in Minecraft's footwear language while living in JetSetCraft's hands-free ride slot:

- Frost Walker reuses vanilla `FrostWalkerEnchantment.onEntityMoved`.
- Soul Speed feeds the Soul Sand/Soul Soil movement profile.
- Depth Strider feeds water retention.
- Feather Falling contributes through vanilla protection math even though the skates are not stored in the vanilla boots slot. If boots and skates both carry Feather Falling, JetSetCraft takes the stronger level rather than stacking both levels.

The custom loadout is an integration surface; it is not permission to replace Minecraft's physical rules. JetSetCraft remains the authoritative trick/momentum/grind system while composing with those rules.
