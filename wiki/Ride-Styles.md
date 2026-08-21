# Ride Styles

Every ride uses the same server-authoritative movement, trick, grind, wall-ride, world-physics, and loadout architecture. The differences are handling identities rather than isolated vehicle systems.

| Style | Identity | Best at | Tradeoff |
| --- | --- | --- | --- |
| Neo Inline Skates | Fast, direct street flow | Balanced speed, technical air lines, classic soul/royale grind vocabulary | Less steering than quad skates |
| Neon Quad Skates | Playful, dance-forward rolling | Tight steering, ground style, cross-steps, stable transitions | Lower raw top speed |
| Street Deck | Committed board lines | Strong grind multiplier, flips, slides, ledge creativity | More deliberate steering and air correction |
| Street BMX | Power and gap coverage | Highest raw cruise/boost range, large jumps, pegs and bike tricks | Lower grind multiplier and a larger visual footprint |
| Flux Hoverboard | Smooth futuristic flow | Air correction, magnetic-feeling grinds, strong coasting | Slightly below BMX's maximum boost ceiling |
| Neon Street Scooter | Nimble technical riding | Fast steering, tailwhips/barspins, compact transfers | Less raw speed than BMX and slightly less grind focus than boards |

## Dedicated loadout

Ride gear is stored as the real `ItemStack` in a persistent player capability. It does not consume a hand or the vanilla boots slot. This is why JetSetCraft can preserve combat input, equipment NBT, enchantments, death drops, save/load behavior, and future cosmetic upgrades without pretending the gear is a separate vehicle entity.

## Footwear enchantments

Inline and quad skates can accept Frost Walker, Soul Speed, Depth Strider, and Feather Falling through JetSetCraft's dedicated slot. The implementation composes with vanilla armor enchantments using max-not-sum behavior where appropriate, preventing accidental double stacking while retaining Minecraft's movement language.

## Visual equipment

All six styles have generated high-detail OBJ geometry. The hoverboard is a dedicated wheel-free model with field generators, luminous rails, and stabilizer fins; it no longer reuses the skateboard mesh. Scooter, BMX, board, skate, and spray-can models also exceed the project's production mesh quality floor.
